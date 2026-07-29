package th.ac.dusit.dorm.identity;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "roles")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name_th", nullable = false, length = 120)
    private String nameTh;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Version
    private long version;

    @ManyToMany
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<PermissionEntity> permissions = new LinkedHashSet<>();

    protected RoleEntity() {
    }

    public RoleEntity(String code, String nameTh) {
        this.code = code;
        this.nameTh = nameTh;
    }

    public String getCode() {
        return code;
    }

    public boolean isActive() {
        return active;
    }

    public Set<PermissionEntity> getPermissions() {
        return Set.copyOf(permissions);
    }

    public String getNameTh() {
        return nameTh;
    }

    public String getDescription() {
        return description;
    }

    public long getVersion() {
        return version;
    }

    public void replacePermissions(Set<PermissionEntity> replacements) {
        permissions.clear();
        permissions.addAll(replacements);
    }
}
