package assignments.events;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Request binding is instructor-provided; learners implement the validation TODO. */
@RestController
@RequestMapping("/events")
public class EventsController {
    @PostMapping
    public ResponseEntity<Map<String, String>> receive(@RequestBody(required = false) EventRequest request) {
        // LEARNER TODO: validate request and every required field safely.
        // Missing, blank, or malformed input must return HTTP 400.
        // Valid input may return an acknowledgement; do not add auth or persistence.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of("status", "TODO"));
    }
}
