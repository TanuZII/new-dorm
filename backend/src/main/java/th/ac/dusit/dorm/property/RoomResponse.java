package th.ac.dusit.dorm.property;

import th.ac.dusit.dorm.property.persistence.RoomEntity;

public record RoomResponse(
        Long id,
        String buildingCode,
        String number,
        int floor,
        int capacity,
        int occupiedBeds,
        int availableBeds,
        String status) {

    static RoomResponse from(RoomEntity room) {
        return new RoomResponse(
                room.getId(),
                room.getBuildingCode(),
                room.getNumber(),
                room.getFloor(),
                room.getCapacity(),
                room.getOccupiedBeds(),
                room.getCapacity() - room.getOccupiedBeds(),
                room.getStatus().name());
    }
}

