package th.ac.dusit.dorm.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "permissions")
public class PermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(name = "resource_name", nullable = false, length = 40)
    private String resourceName;

    @Column(name = "action_name", nullable = false, length = 30)
    private String actionName;

    @Column(length = 500)
    private String description;

    protected PermissionEntity() {
    }

    public PermissionEntity(String code, String resourceName, String actionName) {
        this.code = code;
        this.resourceName = resourceName;
        this.actionName = actionName;
    }

    public String getCode() {
        return code;
    }
}
