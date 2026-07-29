package th.ac.dusit.dorm.imports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.nio.file.Files;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import th.ac.dusit.dorm.platform.DormProperties;

@SpringBootTest
@AutoConfigureMockMvc
class MasterDataImportIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired DormProperties properties;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM import_errors");
        jdbcTemplate.update("DELETE FROM import_sessions");
        jdbcTemplate.update("DELETE FROM master_data_items");
    }

    @Test
    void previewValidatesEveryRowWithoutInsertingBusinessData() throws Exception {
        byte[] workbook = workbook(
                row("FEE_TYPE", "WATER", "ค่าน้ำ", "Water", "", "2026-01-01", ""),
                row("UNKNOWN", "BROKEN", "ข้อมูลผิด", "", "", "2026-01-01", ""));

        mockMvc.perform(multipart("/api/v1/imports/master-data/preview")
                        .file(file(workbook))
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.sha256").value(org.hamcrest.Matchers.matchesPattern("[a-f0-9]{64}")))
                .andExpect(jsonPath("$.totalRows").value(2))
                .andExpect(jsonPath("$.validRows").value(1))
                .andExpect(jsonPath("$.invalidRows").value(1))
                .andExpect(jsonPath("$.errors[0].rowNumber").value(3));

        assertThat(countMasterData()).isZero();
    }

    @Test
    void previewRejectsOverlappingCodesInsideTheSameWorkbook() throws Exception {
        byte[] workbook = workbook(
                row("FEE_TYPE", "WATER", "ค่าน้ำ", "", "", "2026-01-01", "2026-12-31"),
                row("FEE_TYPE", "water", "ค่าน้ำใหม่", "", "", "2026-06-01", ""));

        mockMvc.perform(multipart("/api/v1/imports/master-data/preview")
                        .file(file(workbook))
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validRows").value(1))
                .andExpect(jsonPath("$.invalidRows").value(1))
                .andExpect(jsonPath("$.errors[0].code").value("OVERLAPPING_EFFECTIVE_DATES"));

        assertThat(countMasterData()).isZero();
    }

    @Test
    void confirmIsAtomicWhenWorkbookContainsAnInvalidRow() throws Exception {
        String token = preview(workbook(
                row("FEE_TYPE", "RENT", "ค่าเช่า", "Rent", "", "2026-01-01", ""),
                row("FEE_TYPE", "", "ไม่ควรถูกบันทึก", "", "", "2026-01-01", "")));

        mockMvc.perform(post("/api/v1/imports/{token}/confirm", token)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATE"));

        assertThat(countMasterData()).isZero();
    }

    @Test
    void validWorkbookCanBeConfirmedExactlyOnce() throws Exception {
        String token = preview(workbook(
                row("FEE_TYPE", "rent", "ค่าเช่า", "Rent", "", "2026-01-01", "")));

        mockMvc.perform(post("/api/v1/imports/{token}/confirm", token)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedRows").value(1));

        mockMvc.perform(post("/api/v1/imports/{token}/confirm", token)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isConflict());

        assertThat(countMasterData()).isOne();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT item_code FROM master_data_items", String.class)).isEqualTo("RENT");
    }

    @Test
    void expiredPreviewCannotBeConfirmed() throws Exception {
        String token = preview(workbook(
                row("FEE_TYPE", "WATER", "ค่าน้ำ", "Water", "", "2026-01-01", "")));
        jdbcTemplate.update(
                "UPDATE import_sessions SET expires_at = ? WHERE token = ?",
                LocalDateTime.now().minusMinutes(1), token);

        mockMvc.perform(post("/api/v1/imports/{token}/confirm", token)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("expired")));

        assertThat(countMasterData()).isZero();
    }

    @Test
    void changedStoredFileCannotBeConfirmed() throws Exception {
        String token = preview(workbook(
                row("FEE_TYPE", "WATER", "ค่าน้ำ", "Water", "", "2026-01-01", "")));
        String storagePath = jdbcTemplate.queryForObject(
                "SELECT storage_path FROM import_sessions WHERE token = ?", String.class, token);
        Files.write(properties.storagePath().resolve(storagePath), new byte[] {'P', 'K', 3, 4, 0});

        mockMvc.perform(post("/api/v1/imports/{token}/confirm", token)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("hash")));

        assertThat(countMasterData()).isZero();
    }

    @Test
    void rejectsFilesThatOnlyClaimToBeXlsx() throws Exception {
        mockMvc.perform(multipart("/api/v1/imports/master-data/preview")
                        .file(file("not a workbook".getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        assertThat(countMasterData()).isZero();
    }

    @Test
    void validationErrorsAreDownloadableAsAFormattedWorkbook() throws Exception {
        String token = preview(workbook(
                row("NOT_A_TYPE", "X", "ข้อมูลผิด", "", "", "2026-01-01", "")));

        byte[] result = mockMvc.perform(get("/api/v1/imports/{token}/errors.xlsx", token)
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment")))
                .andReturn().getResponse().getContentAsByteArray();

        try (var workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(result))) {
            var sheet = workbook.getSheetAt(0);
            assertThat(sheet.getPaneInformation()).isNotNull();
            assertThat(sheet.getPaneInformation().isFreezePane()).isTrue();
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("Row");
            assertThat(sheet.getRow(1).getCell(0).getNumericCellValue()).isEqualTo(2);
        }
    }

    @Test
    void staffCannotPreviewImports() throws Exception {
        mockMvc.perform(multipart("/api/v1/imports/master-data/preview")
                        .file(file(workbook(row("FEE_TYPE", "WATER", "ค่าน้ำ", "", "", "2026-01-01", ""))))
                        .with(user("staff").roles("DORM_STAFF"))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    private String preview(byte[] bytes) throws Exception {
        String json = mockMvc.perform(multipart("/api/v1/imports/master-data/preview")
                        .file(file(bytes))
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(json, "$.token");
    }

    private long countMasterData() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM master_data_items", Long.class);
    }

    private MockMultipartFile file(byte[] bytes) {
        return new MockMultipartFile(
                "file", "master-data.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);
    }

    private String[] row(String... values) {
        return values;
    }

    private byte[] workbook(String[]... rows) throws Exception {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Master Data");
            String[] headers = {"type", "code", "nameTh", "nameEn", "parentId", "effectiveFrom", "effectiveTo"};
            var header = sheet.createRow(0);
            for (int column = 0; column < headers.length; column++) {
                header.createCell(column).setCellValue(headers[column]);
            }
            for (int index = 0; index < rows.length; index++) {
                var sheetRow = sheet.createRow(index + 1);
                for (int column = 0; column < rows[index].length; column++) {
                    sheetRow.createCell(column).setCellValue(rows[index][column]);
                }
            }
            workbook.write(output);
            return output.toByteArray();
        }
    }
}
