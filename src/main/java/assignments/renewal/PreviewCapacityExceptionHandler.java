package assignments.renewal;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Narrow safe mapping for only preview-capacity failures. */
@RestControllerAdvice(basePackages = "assignments.renewal")
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class PreviewCapacityExceptionHandler {
    @ExceptionHandler(PreviewBusyException.class)
    public ResponseEntity<Map<String, String>> busy(PreviewBusyException ignored) {
        // LEARNER TODO Day 78: return only the fixed PREVIEW_BUSY 503 envelope.
        throw new UnsupportedOperationException("Day 78 learner TODO: map busy capacity safely");
    }

    @ExceptionHandler(PreviewUnavailableException.class)
    public ResponseEntity<Map<String, String>> unavailable(PreviewUnavailableException ignored) {
        // LEARNER TODO Day 78: return only the fixed PREVIEW_UNAVAILABLE 503 envelope.
        throw new UnsupportedOperationException("Day 78 learner TODO: map unavailable safely");
    }

    @ExceptionHandler(PreviewReadInterruptedException.class)
    public ResponseEntity<Map<String, String>> readInterrupted(PreviewReadInterruptedException ignored) {
        // LEARNER TODO Day 92: return the fixed safe 503 envelope; do not disclose SQL or policy data.
        throw new UnsupportedOperationException("Day 92 learner TODO: map interrupted read safely");
    }
}
