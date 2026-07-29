package th.ac.dusit.dorm.imports;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "import_errors")
class ImportErrorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "import_session_id", nullable = false)
    private Long importSessionId;

    @Column(name = "source_row_number", nullable = false)
    private int rowNumber;

    @Column(name = "field_name", nullable = false, length = 80)
    private String field;

    @Column(name = "rejected_value", length = 500)
    private String rejectedValue;

    @Column(name = "error_code", nullable = false, length = 80)
    private String errorCode;

    @Column(nullable = false, length = 500)
    private String message;

    protected ImportErrorEntity() {
    }

    ImportErrorEntity(Long sessionId, ImportRowError error) {
        this.importSessionId = sessionId;
        this.rowNumber = error.rowNumber();
        this.field = error.field();
        this.rejectedValue = error.rejectedValue();
        this.errorCode = error.code();
        this.message = error.message();
    }

    int getRowNumber() { return rowNumber; }
    String getField() { return field; }
    String getRejectedValue() { return rejectedValue; }
    String getErrorCode() { return errorCode; }
    String getMessage() { return message; }
}
