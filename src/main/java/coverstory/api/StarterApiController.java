package coverstory.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;


/** Session remains unavailable until a trusted authentication adapter is supplied. */
@RestController
@RequestMapping("/api")
public class StarterApiController {
    @GetMapping("/session")
    public ResponseEntity<?> session(HttpServletRequest request) {
        // Only a trusted servlet/Spring authentication adapter may provide this principal.
        var principal = request.getUserPrincipal();
        if (principal == null || principal.getName() == null || principal.getName().isBlank())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).cacheControl(org.springframework.http.CacheControl.noStore()).build();
        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noStore())
                .body(Map.of("ownerId", principal.getName()));
    }

}
