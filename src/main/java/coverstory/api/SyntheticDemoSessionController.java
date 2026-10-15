package coverstory.api;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/** Local walkthrough account switching only. Selectors are fixed synthetic fixtures, not authentication. */
@RestController
@Profile("synthetic-demo")
@ConditionalOnProperty(name="coverstory.synthetic-demo.identity.enabled", havingValue="true")
public final class SyntheticDemoSessionController {
    public record Selection(String account) { }

    @PostMapping("/api/demo/session")
    public ResponseEntity<?> select(@RequestBody(required=false) Selection body,HttpServletResponse response) {
        if(body==null || !("A".equals(body.account()) || "B".equals(body.account())))
            return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(Map.of("code","INVALID_DEMO_ACCOUNT"));
        UUID owner="A".equals(body.account()) ? SyntheticDemoSessionFilter.OWNER_A : SyntheticDemoSessionFilter.OWNER_B;
        ResponseCookie cookie=ResponseCookie.from("coverstory_demo_owner",body.account()).httpOnly(true).path("/")
                .sameSite("Strict").maxAge(1800).build();
        response.addHeader("Set-Cookie",cookie.toString());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(Map.of("ownerId",owner.toString(),"classification","SYNTHETIC_DEMO_ONLY"));
    }

    @PostMapping("/api/demo/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie=ResponseCookie.from("coverstory_demo_owner","").httpOnly(true).path("/")
                .sameSite("Strict").maxAge(0).build();
        response.addHeader("Set-Cookie",cookie.toString());
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
