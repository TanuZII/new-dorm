package th.ac.dusit.dorm.masterdata;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MasterDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MasterDataService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MasterDataResponse water;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM master_data_items");
        water = service.create(
                MasterDataType.FEE_TYPE,
                new CreateMasterDataRequest(
                        "WATER", "ค่าน้ำ", null, null,
                        LocalDate.of(2026, 1, 1), null),
                "admin",
                "127.0.0.1");
        service.create(
                MasterDataType.FEE_TYPE,
                new CreateMasterDataRequest(
                        "RENT", "ค่าเช่า", null, null,
                        LocalDate.of(2026, 1, 1), null),
                "admin",
                "127.0.0.1");
    }

    @Test
    void authenticatedStaffCanFilterAndPageMasterData() throws Exception {
        mockMvc.perform(get("/api/v1/master-data/FEE_TYPE")
                        .param("query", "ค่า")
                        .param("active", "true")
                        .param("effectiveOn", "2026-07-29")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "code,asc")
                        .with(user("staff").roles("DORM_STAFF")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].code").value("RENT"));
    }

    @Test
    void nonAdminCannotCreateMasterData() throws Exception {
        mockMvc.perform(post("/api/v1/master-data/FEE_TYPE")
                        .with(user("staff").roles("DORM_STAFF"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "PENALTY",
                                  "nameTh": "ค่าปรับ",
                                  "effectiveFrom": "2026-01-01"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void staleUpdateReturnsTheConcurrentModificationEnvelope() throws Exception {
        mockMvc.perform(put("/api/v1/master-data/FEE_TYPE/{id}", water.id())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "WATER",
                                  "nameTh": "ค่าน้ำใหม่",
                                  "effectiveFrom": "2026-01-01",
                                  "version": 99
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONCURRENT_MODIFICATION"));
    }

    @Test
    void unsupportedMasterDataTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/master-data/UNKNOWN")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
