package th.ac.dusit.dorm.property.domain;

public final class Room {
    private final String number;
    private final int capacity;
    private int occupiedBeds;
    private RoomStatus status = RoomStatus.AVAILABLE;

    public Room(String number, int capacity) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Room number is required");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Room capacity must be greater than zero");
        }
        this.number = number.trim();
        this.capacity = capacity;
    }

    public void occupyBed() {
        if (status == RoomStatus.DAMAGED || status == RoomStatus.INACTIVE) {
            throw new IllegalStateException("Room is not ready for occupancy");
        }
        if (availableBeds() == 0) {
            throw new IllegalStateException("Room has no available bed");
        }
        occupiedBeds++;
        status = RoomStatus.OCCUPIED;
    }

    public void releaseBed() {
        if (occupiedBeds == 0) {
            throw new IllegalStateException("Room has no occupied bed");
        }
        occupiedBeds--;
        status = occupiedBeds == 0 ? RoomStatus.AVAILABLE : RoomStatus.OCCUPIED;
    }

    public void markDamaged() {
        if (occupiedBeds > 0) {
            throw new IllegalStateException("Occupied room cannot be marked damaged");
        }
        status = RoomStatus.DAMAGED;
    }

    public int availableBeds() {
        return capacity - occupiedBeds;
    }

    public RoomStatus status() {
        return status;
    }

    public String number() {
        return number;
    }
}

