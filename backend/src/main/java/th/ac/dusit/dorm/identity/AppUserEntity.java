package th.ac.dusit.dorm.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
@Table(name = "app_users")
public class AppUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 160)
    private String displayName;

    @Column(length = 160)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @ManyToMany
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> assignedRoles = new LinkedHashSet<>();

    protected AppUserEntity() {
    }

    public AppUserEntity(String username, String passwordHash, String displayName, UserRole role) {
        this(username, passwordHash, displayName, null, role);
    }

    public AppUserEntity(
            String username,
            String passwordHash,
            String displayName,
            String email,
            UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return active; }
    public int getFailedAttempts() { return failedAttempts; }
    public Instant getLockedUntil() { return lockedUntil; }

    public void recordFailure(int maximumFailures, int lockMinutes, Instant now) {
        failedAttempts++;
        if (failedAttempts >= maximumFailures) {
            lockedUntil = now.plusSeconds(lockMinutes * 60L);
        }
    }

    public void recordSuccess() {
        failedAttempts = 0;
        lockedUntil = null;
    }

    public void changeStatus(boolean active) {
        this.active = active;
        if (active) {
            recordSuccess();
        }
    }

    public void resetPassword(String passwordHash) {
        this.passwordHash = passwordHash;
        recordSuccess();
    }
}
