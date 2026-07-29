package th.ac.dusit.dorm.billing.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    boolean existsByInvoiceNumber(String invoiceNumber);
}

