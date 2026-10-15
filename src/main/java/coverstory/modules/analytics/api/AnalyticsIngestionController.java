package coverstory.modules.analytics.api;

import coverstory.modules.analytics.AnalyticsIngestionPort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;

/** Small HTTP boundary: authentication, hard byte cap, safe codes, and Analytics-port delegation. */
@RestController
public final class AnalyticsIngestionController {
    private static final int MAX_BYTES = 8192;
    private final AnalyticsIngestionPort analytics;
    public AnalyticsIngestionController(AnalyticsIngestionPort analytics) { this.analytics = analytics; }

    @PostMapping(path="/api/v1/analytics/events", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SafeReceipt> ingest(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null || principal.getName() == null || principal.getName().isBlank())
            return ResponseEntity.status(401).body(new SafeReceipt("AUTH_REQUIRED", 0));
        byte[] body;
        try { body = readBounded(request); }
        catch (IOException failure) { return ResponseEntity.badRequest().body(new SafeReceipt("BODY_UNREADABLE", 0)); }
        if (body.length > MAX_BYTES) return ResponseEntity.status(413).body(new SafeReceipt("BODY_TOO_LARGE", 0));
        AnalyticsIngestionPort.Receipt result;
        try { result = analytics.accept(body, principal.getName()); }
        catch (RuntimeException failure) { return ResponseEntity.status(503).body(new SafeReceipt("ANALYTICS_UNAVAILABLE", 0)); }
        return ResponseEntity.status(result.status()).body(new SafeReceipt(result.code(), result.acceptedCount()));
    }

    private static byte[] readBounded(HttpServletRequest request) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(MAX_BYTES + 1);
        byte[] chunk = new byte[1024]; int remaining = MAX_BYTES + 1;
        while (remaining > 0) { int read=request.getInputStream().read(chunk,0,Math.min(chunk.length,remaining)); if(read<0)break;out.write(chunk,0,read);remaining-=read; }
        return out.toByteArray();
    }
    public record SafeReceipt(String code, int acceptedCount) { }
}
