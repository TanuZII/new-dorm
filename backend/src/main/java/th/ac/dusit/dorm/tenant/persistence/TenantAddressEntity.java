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
import th.ac.dusit.dorm.tenant.TenantAddressRequest;

@Entity
@Table(name = "tenant_addresses")
public class TenantAddressEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "tenant_id") private TenantEntity tenant;
    @Column(name = "address_type", nullable = false, length = 20) private String addressType;
    @Column(name = "address_line", nullable = false, length = 500) private String addressLine;
    @Column(name = "subdistrict_code", length = 40) private String subdistrictCode;
    @Column(name = "district_code", length = 40) private String districtCode;
    @Column(name = "province_code", length = 40) private String provinceCode;
    @Column(name = "postal_code", length = 10) private String postalCode;
    @Column(name = "country_code", nullable = false, length = 40) private String countryCode;
    @Version private Long version;

    protected TenantAddressEntity() {
    }

    TenantAddressEntity(TenantEntity tenant, TenantAddressRequest request) {
        this.tenant = tenant;
        this.addressType = request.addressType();
        this.addressLine = request.addressLine().trim();
        this.subdistrictCode = normalize(request.subdistrictCode());
        this.districtCode = normalize(request.districtCode());
        this.provinceCode = normalize(request.provinceCode());
        this.postalCode = normalize(request.postalCode());
        this.countryCode = request.countryCode().trim().toUpperCase();
    }

    public String getAddressType() { return addressType; }
    public String getAddressLine() { return addressLine; }
    public String getSubdistrictCode() { return subdistrictCode; }
    public String getDistrictCode() { return districtCode; }
    public String getProvinceCode() { return provinceCode; }
    public String getPostalCode() { return postalCode; }
    public String getCountryCode() { return countryCode; }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
