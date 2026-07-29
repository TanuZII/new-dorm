package th.ac.dusit.dorm.tenant.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;
import th.ac.dusit.dorm.tenant.TenantAddressRequest;
import th.ac.dusit.dorm.tenant.TenantContactRequest;
import th.ac.dusit.dorm.tenant.TenantType;

@Entity
@Table(name = "tenants")
public class TenantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "tenant_code", nullable = false, unique = true, length = 40) private String tenantCode;
    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_type", nullable = false, length = 30) private TenantType tenantType;
    @Column(name = "institutional_id", unique = true, length = 40) private String institutionalId;
    @Column(name = "citizen_id", length = 20) private String citizenId;
    @Column(name = "first_name", nullable = false, length = 100) private String firstName;
    @Column(name = "last_name", nullable = false, length = 100) private String lastName;
    @Column(length = 160) private String email;
    @Column(length = 30) private String phone;
    @Column(nullable = false) private boolean active = true;
    @Version private Long version;
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id") @BatchSize(size = 50)
    private final List<TenantAddressEntity> addresses = new ArrayList<>();
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id") @BatchSize(size = 50)
    private final List<TenantContactEntity> contacts = new ArrayList<>();

    protected TenantEntity() {
    }

    public TenantEntity(
            String tenantCode, TenantType tenantType, String institutionalId, String citizenId,
            String firstName, String lastName, String email, String phone) {
        this.tenantCode = tenantCode;
        updateDetails(tenantType, institutionalId, citizenId, firstName, lastName, email, phone);
    }

    public void update(
            TenantType tenantType, String institutionalId, String citizenId,
            String firstName, String lastName, String email, String phone,
            List<TenantAddressRequest> addressRequests, List<TenantContactRequest> contactRequests) {
        updateDetails(tenantType, institutionalId, citizenId, firstName, lastName, email, phone);
        addresses.clear();
        addressRequests.forEach(request -> addresses.add(new TenantAddressEntity(this, request)));
        contacts.clear();
        contactRequests.forEach(request -> contacts.add(new TenantContactEntity(this, request)));
    }

    public void changeStatus(boolean active) { this.active = active; }
    public Long getId() { return id; }
    public String getTenantCode() { return tenantCode; }
    public TenantType getTenantType() { return tenantType; }
    public String getInstitutionalId() { return institutionalId; }
    public String getCitizenId() { return citizenId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isActive() { return active; }
    public Long getVersion() { return version == null ? 0L : version; }
    public List<TenantAddressEntity> getAddresses() { return List.copyOf(addresses); }
    public List<TenantContactEntity> getContacts() { return List.copyOf(contacts); }

    private void updateDetails(
            TenantType tenantType, String institutionalId, String citizenId,
            String firstName, String lastName, String email, String phone) {
        this.tenantType = tenantType;
        this.institutionalId = normalize(institutionalId);
        this.citizenId = normalize(citizenId);
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.email = normalize(email);
        this.phone = normalize(phone);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
