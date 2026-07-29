package th.ac.dusit.dorm.maintenance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MaintenanceRequestTest {

    @Test
    void followsRepairWorkflowBeforeClosing() {
        var request = new MaintenanceRequest();

        request.start();
        request.complete();
        request.close();

        assertThat(request.status()).isEqualTo(MaintenanceStatus.CLOSED);
    }

    @Test
    void openRequestCannotBeClosedImmediately() {
        var request = new MaintenanceRequest();

        assertThatThrownBy(request::close)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only completed work can be closed");
    }
}
