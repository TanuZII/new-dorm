package th.ac.dusit.dorm.property;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import th.ac.dusit.dorm.property.persistence.RoomEntity;
import th.ac.dusit.dorm.property.persistence.RoomRepository;

@Service
@Transactional(readOnly = true)
public class RoomService {
    private final RoomRepository repository;

    public RoomService(RoomRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public RoomResponse create(CreateRoomRequest request) {
        String buildingCode = request.buildingCode().trim().toUpperCase();
        String number = request.number().trim().toUpperCase();
        if (repository.existsByBuildingCodeAndNumberIgnoreCase(buildingCode, number)) {
            throw new IllegalStateException("Room " + buildingCode + "-" + number + " already exists");
        }
        return RoomResponse.from(repository.save(
                new RoomEntity(buildingCode, number, request.floor(), request.capacity())));
    }

    public Page<RoomResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(RoomResponse::from);
    }
}

