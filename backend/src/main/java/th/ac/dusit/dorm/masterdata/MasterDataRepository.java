package th.ac.dusit.dorm.masterdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface MasterDataRepository extends
        JpaRepository<MasterDataEntity, Long>,
        JpaSpecificationExecutor<MasterDataEntity> {

    @Query("""
            select count(item) from MasterDataEntity item
            where item.type = :type
              and upper(item.code) = upper(:code)
              and (:excludeId is null or item.id <> :excludeId)
              and (:effectiveTo is null or item.effectiveFrom <= :effectiveTo)
              and (item.effectiveTo is null or item.effectiveTo >= :effectiveFrom)
            """)
    long countOverlapping(
            @Param("type") MasterDataType type,
            @Param("code") String code,
            @Param("excludeId") Long excludeId,
            @Param("effectiveFrom") LocalDate effectiveFrom,
            @Param("effectiveTo") LocalDate effectiveTo);
}
