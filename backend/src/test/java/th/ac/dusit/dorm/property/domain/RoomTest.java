package th.ac.dusit.dorm.property.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RoomTest {

    @Test
    void reportsAvailableBedsFromCapacityAndOccupancy() {
        var room = new Room("P1-201", 2);

        room.occupyBed();

        assertThat(room.availableBeds()).isEqualTo(1);
        assertThat(room.status()).isEqualTo(RoomStatus.OCCUPIED);
    }

    @Test
    void preventsOccupancyBeyondRoomCapacity() {
        var room = new Room("P1-201", 1);
        room.occupyBed();

        assertThatThrownBy(room::occupyBed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Room has no available bed");
    }

    @Test
    void damagedRoomCannotAcceptOccupants() {
        var room = new Room("P1-201", 2);
        room.markDamaged();

        assertThatThrownBy(room::occupyBed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Room is not ready for occupancy");
    }
}

