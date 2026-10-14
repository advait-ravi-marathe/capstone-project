package coverstory.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/** Session remains unavailable until a trusted authentication adapter is supplied. */
@RestController
@RequestMapping("/api")
public class StarterApiController {
    @GetMapping("/session")
    public ResponseEntity<Void> session() {
        // A future auth adapter must replace this stub and resolve owner identity server-side.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
