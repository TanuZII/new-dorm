package th.ac.dusit.dorm.masterdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class MasterDataServiceTest {

    @Autowired
    private MasterDataService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM master_data_items");
    }

    @Test
    void normalizesCodesAndRejectsOverlappingEffectiveDates() {
        var first = service.create(
                MasterDataType.TENANT_TYPE,
                request(" student ", "นักศึกษา", null,
                        LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30)),
                "admin",
                "127.0.0.1");

        assertThat(first.code()).isEqualTo("STUDENT");
        assertThatThrownBy(() -> service.create(
                MasterDataType.TENANT_TYPE,
                request("STUDENT", "นักศึกษาภาคฤดูร้อน", null,
                        LocalDate.of(2026, 6, 30), LocalDate.of(2026, 12, 31)),
                "admin",
                "127.0.0.1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("overlap");

        var next = service.create(
                MasterDataType.TENANT_TYPE,
                request("student", "นักศึกษา", null,
                        LocalDate.of(2026, 7, 1), null),
                "admin",
                "127.0.0.1");
        assertThat(next.id()).isNotEqualTo(first.id());
    }

    @Test
    void requiresTheConfiguredParentType() {
        var country = service.create(
                MasterDataType.COUNTRY,
                request("TH", "ประเทศไทย", null, LocalDate.of(2020, 1, 1), null),
                "admin",
                "127.0.0.1");
        var province = service.create(
                MasterDataType.PROVINCE,
                request("BKK", "กรุงเทพมหานคร", country.id(), LocalDate.of(2020, 1, 1), null),
                "admin",
                "127.0.0.1");

        assertThat(province.parentId()).isEqualTo(country.id());
        assertThatThrownBy(() -> service.create(
                MasterDataType.DISTRICT,
                request("DUSIT", "ดุสิต", country.id(), LocalDate.of(2020, 1, 1), null),
                "admin",
                "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PROVINCE");
    }

    @Test
    void deactivationRequiresAndRecordsAReason() {
        var created = service.create(
                MasterDataType.FEE_TYPE,
                request("OTHER", "ค่าบริการอื่น", null, LocalDate.of(2026, 1, 1), null),
                "admin",
                "127.0.0.1");

        assertThatThrownBy(() -> service.changeStatus(
                MasterDataType.FEE_TYPE,
                created.id(),
                new ChangeMasterDataStatusRequest(false, " ", created.version()),
                "admin",
                "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reason");

        var deactivated = service.changeStatus(
                MasterDataType.FEE_TYPE,
                created.id(),
                new ChangeMasterDataStatusRequest(false, "ยกเลิกรายการเดิม", created.version()),
                "admin",
                "127.0.0.1");
        assertThat(deactivated.active()).isFalse();
        assertThat(deactivated.deactivationReason()).isEqualTo("ยกเลิกรายการเดิม");
        assertThat(deactivated.version()).isEqualTo(created.version() + 1);
    }

    @Test
    void updateReturnsTheIncrementedOptimisticVersion() {
        var created = service.create(
                MasterDataType.CONTRACT_TYPE,
                request("YEARLY", "รายปี", null, LocalDate.of(2026, 1, 1), null),
                "admin",
                "127.0.0.1");

        var updated = service.update(
                MasterDataType.CONTRACT_TYPE,
                created.id(),
                new UpdateMasterDataRequest(
                        " yearly ", "สัญญารายปี", "Yearly contract", null,
                        LocalDate.of(2026, 1, 1), null, created.version()),
                "admin",
                "127.0.0.1");

        assertThat(updated.nameTh()).isEqualTo("สัญญารายปี");
        assertThat(updated.code()).isEqualTo("YEARLY");
        assertThat(updated.version()).isEqualTo(created.version() + 1);
    }

    private CreateMasterDataRequest request(
            String code,
            String nameTh,
            Long parentId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo) {
        return new CreateMasterDataRequest(
                code, nameTh, null, parentId, effectiveFrom, effectiveTo);
    }
}
