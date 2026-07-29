package th.ac.dusit.dorm.contract.domain;

public final class Contract {
    private ContractStatus status = ContractStatus.DRAFT;
    private String confirmedBy;
    private String documentHash;

    public void sendForConfirmation() {
        if (status != ContractStatus.DRAFT) {
            throw new IllegalStateException("Only a draft contract can be sent");
        }
        status = ContractStatus.WAITING_CONFIRMATION;
    }

    public void confirm(String username, String hash) {
        if (status != ContractStatus.WAITING_CONFIRMATION) {
            throw new IllegalStateException("Contract is not waiting for confirmation");
        }
        if (username == null || username.isBlank() || hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Confirmation identity and document hash are required");
        }
        confirmedBy = username;
        documentHash = hash;
        status = ContractStatus.ACTIVE;
    }

    public ContractStatus status() {
        return status;
    }

    public String confirmedBy() {
        return confirmedBy;
    }

    public String documentHash() {
        return documentHash;
    }
}

