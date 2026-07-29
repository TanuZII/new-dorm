package th.ac.dusit.dorm.property;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {
    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_STAFF','MAINTENANCE')")
    public Page<RoomResponse> findAll(Pageable pageable) {
        return service.findAll(pageable);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DORM_STAFF')")
    public ResponseEntity<RoomResponse> create(@Valid @RequestBody CreateRoomRequest request) {
        var created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/rooms/" + created.id())).body(created);
    }
}
