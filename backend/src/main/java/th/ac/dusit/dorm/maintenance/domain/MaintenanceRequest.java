package th.ac.dusit.dorm.maintenance.domain;

public final class MaintenanceRequest {
    private MaintenanceStatus status = MaintenanceStatus.OPEN;

    public void start() {
        require(MaintenanceStatus.OPEN, "Only open work can be started");
        status = MaintenanceStatus.IN_PROGRESS;
    }

    public void waitForPart() {
        require(MaintenanceStatus.IN_PROGRESS, "Only work in progress can wait for a part");
        status = MaintenanceStatus.WAITING_PART;
    }

    public void resume() {
        require(MaintenanceStatus.WAITING_PART, "Only work waiting for a part can resume");
        status = MaintenanceStatus.IN_PROGRESS;
    }

    public void complete() {
        require(MaintenanceStatus.IN_PROGRESS, "Only work in progress can be completed");
        status = MaintenanceStatus.COMPLETED;
    }

    public void close() {
        require(MaintenanceStatus.COMPLETED, "Only completed work can be closed");
        status = MaintenanceStatus.CLOSED;
    }

    public MaintenanceStatus status() {
        return status;
    }

    private void require(MaintenanceStatus required, String message) {
        if (status != required) {
            throw new IllegalStateException(message);
        }
    }
}
