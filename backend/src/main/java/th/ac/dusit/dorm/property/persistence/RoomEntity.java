package th.ac.dusit.dorm.property.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import th.ac.dusit.dorm.property.domain.RoomStatus;

@Entity
@Table(name = "rooms", uniqueConstraints =
        @UniqueConstraint(name = "uk_room_building_number", columnNames = {"building_code", "number"}))
public class RoomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "building_code", nullable = false, length = 20)
    private String buildingCode;

    @Column(nullable = false, length = 20)
    private String number;

    @Column(nullable = false)
    private int floor;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "occupied_beds", nullable = false)
    private int occupiedBeds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @Version
    private long version;

    protected RoomEntity() {
    }

    public RoomEntity(String buildingCode, String number, int floor, int capacity) {
        this.buildingCode = buildingCode;
        this.number = number;
        this.floor = floor;
        this.capacity = capacity;
        this.status = RoomStatus.AVAILABLE;
    }

    public Long getId() { return id; }
    public String getBuildingCode() { return buildingCode; }
    public String getNumber() { return number; }
    public int getFloor() { return floor; }
    public int getCapacity() { return capacity; }
    public int getOccupiedBeds() { return occupiedBeds; }
    public RoomStatus getStatus() { return status; }
}

