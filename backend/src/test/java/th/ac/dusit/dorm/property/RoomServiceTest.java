package th.ac.dusit.dorm.property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import th.ac.dusit.dorm.property.persistence.RoomEntity;
import th.ac.dusit.dorm.property.persistence.RoomRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository repository;

    @Test
    void createsRoomWithNormalizedNumberAndAvailableBeds() {
        var service = new RoomService(repository);
        when(repository.existsByBuildingCodeAndNumberIgnoreCase("P1", "201")).thenReturn(false);
        when(repository.save(any(RoomEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.create(new CreateRoomRequest(" P1 ", " 201 ", 2, 2));

        assertThat(created.buildingCode()).isEqualTo("P1");
        assertThat(created.number()).isEqualTo("201");
        assertThat(created.availableBeds()).isEqualTo(2);
        assertThat(created.status()).isEqualTo("AVAILABLE");
    }

    @Test
    void rejectsDuplicateRoomNumberInsideBuilding() {
        var service = new RoomService(repository);
        when(repository.existsByBuildingCodeAndNumberIgnoreCase("P1", "201")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateRoomRequest("P1", "201", 2, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Room P1-201 already exists");
        verify(repository, never()).save(any());
    }
}
