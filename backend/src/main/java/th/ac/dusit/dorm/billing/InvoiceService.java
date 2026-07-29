package th.ac.dusit.dorm.billing;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import th.ac.dusit.dorm.billing.persistence.InvoiceEntity;
import th.ac.dusit.dorm.billing.persistence.InvoiceRepository;
import th.ac.dusit.dorm.common.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
public class InvoiceService {
    private final InvoiceRepository repository;

    public InvoiceService(InvoiceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public InvoiceResponse create(CreateInvoiceRequest request) {
        String number = request.invoiceNumber().trim().toUpperCase();
        if (repository.existsByInvoiceNumber(number)) {
            throw new IllegalStateException("Invoice " + number + " already exists");
        }
        var invoice = new InvoiceEntity(number, request.tenantId(), request.issueDate(),
                request.dueDate(), request.totalAmount());
        return InvoiceResponse.from(repository.save(invoice));
    }

    @Transactional
    public InvoiceResponse recordPayment(Long id, BigDecimal amount) {
        var invoice = find(id);
        invoice.recordPayment(amount);
        return InvoiceResponse.from(invoice);
    }

    @Transactional
    public InvoiceResponse voidInvoice(Long id, String reason) {
        var invoice = find(id);
        invoice.voidInvoice(reason);
        return InvoiceResponse.from(invoice);
    }

    public Page<InvoiceResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(InvoiceResponse::from);
    }

    private InvoiceEntity find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }
}

