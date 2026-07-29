package th.ac.dusit.dorm.billing;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {
    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_STAFF','FINANCE','APPROVER')")
    public Page<InvoiceResponse> findAll(Pageable pageable) { return service.findAll(pageable); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody CreateInvoiceRequest request) {
        var created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/invoices/" + created.id())).body(created);
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public InvoiceResponse recordPayment(@PathVariable Long id, @Valid @RequestBody PaymentAmountRequest request) {
        return service.recordPayment(id, request.amount());
    }

    @PatchMapping("/{id}/void")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public InvoiceResponse voidInvoice(@PathVariable Long id, @RequestBody Map<String, String> request) {
        return service.voidInvoice(id, request.get("reason"));
    }
}
