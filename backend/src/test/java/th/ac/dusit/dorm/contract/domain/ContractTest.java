package th.ac.dusit.dorm.contract.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ContractTest {

    @Test
    void activatesOnlyAfterWaitingContractIsConfirmed() {
        var contract = new Contract();
        contract.sendForConfirmation();

        contract.confirm("tenant-001", "sha256-value");

        assertThat(contract.status()).isEqualTo(ContractStatus.ACTIVE);
        assertThat(contract.confirmedBy()).isEqualTo("tenant-001");
        assertThat(contract.documentHash()).isEqualTo("sha256-value");
    }

    @Test
    void draftContractCannotBeConfirmed() {
        var contract = new Contract();

        assertThatThrownBy(() -> contract.confirm("tenant-001", "sha256-value"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Contract is not waiting for confirmation");
    }
}

