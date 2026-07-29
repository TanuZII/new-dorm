package th.ac.dusit.dorm.masterdata;

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
import java.time.LocalDate;

@Entity
@Table(
        name = "master_data_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_master_data_type_code_from",
                columnNames = {"data_type", "item_code", "effective_from"}))
public class MasterDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 40)
    private MasterDataType type;

    @Column(name = "item_code", nullable = false, length = 40)
    private String code;

    @Column(name = "name_th", nullable = false, length = 200)
    private String nameTh;

    @Column(name = "name_en", length = 200)
    private String nameEn;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "deactivation_reason", length = 500)
    private String deactivationReason;

    @Version
    @Column(nullable = false)
    private Long version;

    protected MasterDataEntity() {
    }

    MasterDataEntity(
            MasterDataType type,
            String code,
            String nameTh,
            String nameEn,
            Long parentId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo) {
        this.type = type;
        this.code = code;
        this.nameTh = nameTh;
        this.nameEn = nameEn;
        this.parentId = parentId;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.active = true;
    }

    void update(
            String code,
            String nameTh,
            String nameEn,
            Long parentId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo) {
        this.code = code;
        this.nameTh = nameTh;
        this.nameEn = nameEn;
        this.parentId = parentId;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
    }

    void changeStatus(boolean active, String reason) {
        this.active = active;
        this.deactivationReason = active ? null : reason;
    }

    public Long getId() {
        return id;
    }

    public MasterDataType getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public String getNameTh() {
        return nameTh;
    }

    public String getNameEn() {
        return nameEn;
    }

    public Long getParentId() {
        return parentId;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public boolean isActive() {
        return active;
    }

    public String getDeactivationReason() {
        return deactivationReason;
    }

    public Long getVersion() {
        return version;
    }
}
