package th.ac.dusit.dorm.tenant.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import th.ac.dusit.dorm.tenant.TenantContactRequest;

@Entity
@Table(name = "tenant_contacts")
public class TenantContactEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "tenant_id") private TenantEntity tenant;
    @Column(name = "contact_type", nullable = false, length = 20) private String contactType;
    @Column(name = "full_name", nullable = false, length = 200) private String fullName;
    @Column(name = "relationship_name", length = 100) private String relationshipName;
    @Column(nullable = false, length = 30) private String phone;
    @Column(length = 160) private String email;
    @Column(name = "primary_contact", nullable = false) private boolean primaryContact;
    @Version private Long version;

    protected TenantContactEntity() {
    }

    TenantContactEntity(TenantEntity tenant, TenantContactRequest request) {
        this.tenant = tenant;
        this.contactType = request.contactType();
        this.fullName = request.fullName().trim();
        this.relationshipName = normalize(request.relationshipName());
        this.phone = request.phone().trim();
        this.email = normalize(request.email());
        this.primaryContact = request.primaryContact();
    }

    public String getContactType() { return contactType; }
    public String getFullName() { return fullName; }
    public String getRelationshipName() { return relationshipName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public boolean isPrimaryContact() { return primaryContact; }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
