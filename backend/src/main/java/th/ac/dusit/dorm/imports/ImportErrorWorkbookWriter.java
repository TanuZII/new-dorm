package th.ac.dusit.dorm.imports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
class ImportErrorWorkbookWriter {
    byte[] write(List<ImportErrorEntity> errors) {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Import Errors");
            var headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            var headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);

            String[] headers = {"Row", "Field", "Rejected Value", "Error Code", "Message"};
            var header = sheet.createRow(0);
            for (int column = 0; column < headers.length; column++) {
                var cell = header.createCell(column);
                cell.setCellValue(headers[column]);
                cell.setCellStyle(headerStyle);
            }
            for (int index = 0; index < errors.size(); index++) {
                var error = errors.get(index);
                var row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue(error.getRowNumber());
                row.createCell(1).setCellValue(error.getField());
                row.createCell(2).setCellValue(safe(error.getRejectedValue()));
                row.createCell(3).setCellValue(error.getErrorCode());
                row.createCell(4).setCellValue(error.getMessage());
            }
            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0, errors.size()), 0, 4));
            int[] widths = {10, 20, 32, 24, 60};
            for (int column = 0; column < widths.length; column++) {
                sheet.setColumnWidth(column, widths[column] * 256);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate error workbook", exception);
        }
    }

    private String safe(String value) {
        if (value == null) return "";
        return value.matches("^[=+@-].*") ? "'" + value : value;
    }
}
