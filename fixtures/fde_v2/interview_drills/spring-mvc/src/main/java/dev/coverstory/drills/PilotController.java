package dev.coverstory.drills;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pilot")
public class PilotController {
    private final PilotPreviewService service;

    public PilotController(PilotPreviewService service) {
        this.service = service;
    }

    @PostMapping("/preview")
    public PilotPreview preview(@RequestHeader("X-Tenant-ID") String tenantId,
                                @Valid @RequestBody PilotRequest request) {
        return service.preview(tenantId, request);
    }
}
