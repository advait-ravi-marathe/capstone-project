package coverstory.application;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Map;

/** Test-only exception sources; they do not implement draft or payment behavior. */
@RestController
final class CAP83MockController {
    @PostMapping(value = "/api/v1/test/malformed", consumes = MediaType.APPLICATION_JSON_VALUE)
    void malformed(@RequestBody Map<String, Object> ignoredPayload) {
        throw new KnownException.MalformedRequest();
    }

    @GetMapping("/api/v1/test/absent")
    void absent() { throw new KnownException.ResourceNotFound(); }

    @GetMapping("/api/v1/test/foreign")
    void foreign() { throw new KnownException.ResourceNotFound(); }

    @PutMapping("/api/v1/test/stale")
    void stale() { throw new KnownException.StaleVersion(); }

    @GetMapping("/api/v1/test/sql")
    void sql() throws SQLException { throw new SQLException("SECRET_SENTINEL"); }

    @GetMapping("/api/v1/test/session")
    void session() { throw new KnownException.SessionRequired(); }

    @GetMapping("/api/v1/test/quote-conflict")
    void quoteConflict() { throw new KnownException.QuoteConflict(); }

    @GetMapping("/api/v1/test/idempotency-conflict")
    void idempotencyConflict() { throw new KnownException.IdempotencyConflict(); }

    @GetMapping("/api/v1/test/incomplete")
    void incomplete() {
        throw new KnownException.IncompleteFields(Map.of(
                KnownException.Field.FULL_NAME, KnownException.FieldProblem.REQUIRED));
    }
}
