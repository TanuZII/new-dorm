package th.ac.dusit.dorm.masterdata;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/master-data/{type}")
public class MasterDataController {

    private final MasterDataService service;

    public MasterDataController(MasterDataService service) {
        this.service = service;
    }

    @GetMapping
    public Page<MasterDataResponse> findAll(
            @PathVariable MasterDataType type,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveOn,
            @RequestParam(required = false) Long parentId,
            @PageableDefault(size = 20, sort = "code") Pageable pageable) {
        return service.findAll(type, query, active, effectiveOn, parentId, pageable);
    }

    @PostMapping
    public ResponseEntity<MasterDataResponse> create(
            @PathVariable MasterDataType type,
            @Valid @RequestBody CreateMasterDataRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        var created = service.create(
                type, request, authentication.getName(), servletRequest.getRemoteAddr());
        return ResponseEntity
                .created(URI.create("/api/v1/master-data/" + type + "/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    public MasterDataResponse update(
            @PathVariable MasterDataType type,
            @PathVariable long id,
            @Valid @RequestBody UpdateMasterDataRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        return service.update(type, id, request, authentication.getName(), servletRequest.getRemoteAddr());
    }

    @PatchMapping("/{id}/status")
    public MasterDataResponse changeStatus(
            @PathVariable MasterDataType type,
            @PathVariable long id,
            @Valid @RequestBody ChangeMasterDataStatusRequest request,
            Authentication authentication,
            HttpServletRequest servletRequest) {
        return service.changeStatus(
                type, id, request, authentication.getName(), servletRequest.getRemoteAddr());
    }
}
