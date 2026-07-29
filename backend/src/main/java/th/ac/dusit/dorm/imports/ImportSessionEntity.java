package th.ac.dusit.dorm.imports;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "import_sessions")
class ImportSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @Column(name = "import_type", nullable = false, length = 40)
    private String importType;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(nullable = false, length = 64)
    private String sha256;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImportStatus status;

    @Column(name = "total_rows", nullable = false)
    private int totalRows;

    @Column(name = "valid_rows", nullable = false)
    private int validRows;

    @Column(name = "invalid_rows", nullable = false)
    private int invalidRows;

    @Column(name = "created_by", nullable = false, length = 80)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    protected ImportSessionEntity() {
    }

    ImportSessionEntity(
            String token, String originalName, String storagePath, String sha256,
            ImportStatus status, int totalRows, int validRows, int invalidRows,
            String createdBy, Instant createdAt, Instant expiresAt) {
        this.token = token;
        this.importType = "MASTER_DATA";
        this.originalName = originalName;
        this.storagePath = storagePath;
        this.sha256 = sha256;
        this.status = status;
        this.totalRows = totalRows;
        this.validRows = validRows;
        this.invalidRows = invalidRows;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    void confirm(Instant confirmedAt) {
        this.status = ImportStatus.CONFIRMED;
        this.confirmedAt = confirmedAt;
    }

    Long getId() { return id; }
    String getToken() { return token; }
    String getOriginalName() { return originalName; }
    String getStoragePath() { return storagePath; }
    String getSha256() { return sha256; }
    ImportStatus getStatus() { return status; }
    int getTotalRows() { return totalRows; }
    int getValidRows() { return validRows; }
    int getInvalidRows() { return invalidRows; }
    Instant getExpiresAt() { return expiresAt; }
}
