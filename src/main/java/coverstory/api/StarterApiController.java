package coverstory.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Explicitly unconfigured API endpoints; no authentication or catalog logic is implied. */
@RestController
@RequestMapping("/api")
public class StarterApiController {
    @GetMapping("/session")
    public ResponseEntity<Void> session() {
        // A future auth adapter must replace this stub and resolve owner identity server-side.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/catalog")
    public ResponseEntity<Map<String, String>> catalog() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of("status", "catalog_business_logic_not_implemented"));
    }
}
