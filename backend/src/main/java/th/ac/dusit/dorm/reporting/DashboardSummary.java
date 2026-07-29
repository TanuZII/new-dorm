package th.ac.dusit.dorm.reporting;

public record DashboardSummary(
        long totalRooms,
        long availableBeds,
        long overdueInvoices,
        long openMaintenanceRequests,
        long outstandingAmount) {
}

