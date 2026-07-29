package th.ac.dusit.dorm.property.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<RoomEntity, Long> {
    boolean existsByBuildingCodeAndNumberIgnoreCase(String buildingCode, String number);

    long countByStatus(th.ac.dusit.dorm.property.domain.RoomStatus status);
}

