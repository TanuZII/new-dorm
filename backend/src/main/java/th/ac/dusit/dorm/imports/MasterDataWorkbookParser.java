package th.ac.dusit.dorm.imports;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import th.ac.dusit.dorm.masterdata.MasterDataImportItem;
import th.ac.dusit.dorm.masterdata.MasterDataType;

@Component
class MasterDataWorkbookParser {
    private static final List<String> HEADERS = List.of(
            "type", "code", "nameTh", "nameEn", "parentId", "effectiveFrom", "effectiveTo");
    ParsedMasterDataWorkbook parse(byte[] bytes) {
        try {
            List<String[]> sheetRows = readFirstSheet(bytes);
            if (sheetRows.isEmpty()) throw new IllegalArgumentException("Header row is required");
            validateHeader(sheetRows.getFirst());
            var rows = new ArrayList<MasterDataImportItem>();
            var errors = new ArrayList<ImportRowError>();
            int totalRows = 0;
            for (int index = 1; index < sheetRows.size(); index++) {
                var row = sheetRows.get(index);
                if (isBlank(row)) continue;
                totalRows++;
                int errorCount = errors.size();
                String typeText = row[0];
                String code = row[1];
                String nameTh = row[2];
                String nameEn = nullable(row[3]);
                String parentText = row[4];
                String fromText = row[5];
                String toText = row[6];

                MasterDataType type = enumValue(typeText, index + 1, errors);
                required(code, "code", index + 1, errors);
                required(nameTh, "nameTh", index + 1, errors);
                maximum(code, "code", 40, index + 1, errors);
                maximum(nameTh, "nameTh", 200, index + 1, errors);
                maximum(nameEn, "nameEn", 200, index + 1, errors);
                Long parentId = longValue(parentText, "parentId", index + 1, errors);
                LocalDate effectiveFrom = dateValue(fromText, "effectiveFrom", true, index + 1, errors);
                LocalDate effectiveTo = dateValue(toText, "effectiveTo", false, index + 1, errors);
                if (effectiveFrom != null && effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
                    errors.add(error(index + 1, "effectiveTo", toText, "INVALID_DATE_RANGE",
                            "effectiveTo must be on or after effectiveFrom"));
                }
                if (errors.size() == errorCount) {
                    var item = new MasterDataImportItem(
                            type, code.trim(), nameTh.trim(), nameEn, parentId, effectiveFrom, effectiveTo);
                    boolean overlaps = rows.stream().anyMatch(existing -> overlaps(existing, item));
                    if (overlaps) {
                        errors.add(error(index + 1, "code", code, "OVERLAPPING_EFFECTIVE_DATES",
                                "Effective dates overlap another row in this workbook"));
                    } else {
                        rows.add(item);
                    }
                }
            }
            if (totalRows == 0) {
                throw new IllegalArgumentException("Workbook must contain at least one data row");
            }
            return new ParsedMasterDataWorkbook(totalRows, rows, errors);
        } catch (RuntimeException exception) {
            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }
            throw new IllegalArgumentException("Unable to read XLSX workbook", exception);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Unable to read XLSX workbook", exception);
        }
    }

    private List<String[]> readFirstSheet(byte[] bytes) throws Exception {
        var rows = new ArrayList<String[]>();
        try (var pkg = OPCPackage.open(new ByteArrayInputStream(bytes))) {
            var reader = new XSSFReader(pkg);
            var sheets = reader.getSheetsData();
            if (!sheets.hasNext()) throw new IllegalArgumentException("Workbook must contain a worksheet");
            try (InputStream sheet = sheets.next()) {
                var xmlReader = XMLHelper.newXMLReader();
                var handler = new XSSFSheetXMLHandler(
                        reader.getStylesTable(),
                        null,
                        reader.getSharedStringsTable(),
                        new RowCollector(rows),
                        new DataFormatter(Locale.ROOT),
                        false);
                xmlReader.setContentHandler(handler);
                xmlReader.parse(new InputSource(sheet));
            }
        }
        return rows;
    }

    private void validateHeader(String[] row) {
        for (int column = 0; column < HEADERS.size(); column++) {
            if (!HEADERS.get(column).equals(row[column])) {
                throw new IllegalArgumentException("Invalid header at column " + (column + 1)
                        + "; expected " + HEADERS.get(column));
            }
        }
    }

    private boolean isBlank(String[] row) {
        return java.util.Arrays.stream(row).allMatch(String::isBlank);
    }

    private MasterDataType enumValue(String value, int row, List<ImportRowError> errors) {
        try {
            return MasterDataType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            errors.add(error(row, "type", value, "INVALID_TYPE", "Unsupported master data type"));
            return null;
        }
    }

    private void required(String value, String field, int row, List<ImportRowError> errors) {
        if (value.isBlank()) errors.add(error(row, field, value, "REQUIRED", field + " is required"));
    }

    private void maximum(String value, String field, int maximum, int row, List<ImportRowError> errors) {
        if (value != null && value.length() > maximum) {
            errors.add(error(row, field, value, "TOO_LONG", field + " must not exceed " + maximum + " characters"));
        }
    }

    private boolean overlaps(MasterDataImportItem first, MasterDataImportItem second) {
        if (first.type() != second.type() || !first.code().equalsIgnoreCase(second.code())) return false;
        boolean firstStartsBeforeSecondEnds = second.effectiveTo() == null
                || !first.effectiveFrom().isAfter(second.effectiveTo());
        boolean secondStartsBeforeFirstEnds = first.effectiveTo() == null
                || !second.effectiveFrom().isAfter(first.effectiveTo());
        return firstStartsBeforeSecondEnds && secondStartsBeforeFirstEnds;
    }

    private Long longValue(String value, String field, int row, List<ImportRowError> errors) {
        if (value.isBlank()) return null;
        try {
            return Long.valueOf(value.endsWith(".0") ? value.substring(0, value.length() - 2) : value);
        } catch (NumberFormatException exception) {
            errors.add(error(row, field, value, "INVALID_NUMBER", field + " must be a whole number"));
            return null;
        }
    }

    private LocalDate dateValue(
            String value, String field, boolean required, int row, List<ImportRowError> errors) {
        if (value.isBlank()) {
            if (required) errors.add(error(row, field, value, "REQUIRED", field + " is required"));
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            errors.add(error(row, field, value, "INVALID_DATE", field + " must use yyyy-MM-dd"));
            return null;
        }
    }

    private ImportRowError error(int row, String field, String value, String code, String message) {
        String rejected = value.isEmpty() ? null : value.substring(0, Math.min(value.length(), 500));
        return new ImportRowError(row, field, rejected, code, message);
    }

    private String nullable(String value) {
        return value.isBlank() ? null : value;
    }

    record ParsedMasterDataWorkbook(
            int totalRows,
            List<MasterDataImportItem> validItems,
            List<ImportRowError> errors) {
    }

    private static final class RowCollector implements XSSFSheetXMLHandler.SheetContentsHandler {
        private final List<String[]> rows;
        private String[] current;

        private RowCollector(List<String[]> rows) {
            this.rows = rows;
        }

        @Override
        public void startRow(int rowNum) {
            while (rows.size() < rowNum) rows.add(blankRow());
            current = blankRow();
        }

        @Override
        public void endRow(int rowNum) {
            rows.add(current);
        }

        @Override
        public void cell(String cellReference, String formattedValue, XSSFComment comment) {
            int column = new CellReference(cellReference).getCol();
            if (column < current.length) current[column] = formattedValue == null ? "" : formattedValue.trim();
        }

        private String[] blankRow() {
            var row = new String[HEADERS.size()];
            java.util.Arrays.fill(row, "");
            return row;
        }
    }
}
