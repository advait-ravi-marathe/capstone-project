package assignments.insurance;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Learner-owned mapping from application failures to safe HTTP error responses. */
@RestControllerAdvice
public final class ApiExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handle(Exception exception) {
        // LEARNER TODO: map ResourceNotFoundException to 404/RESOURCE_NOT_FOUND,
        // VersionConflictException to 409/VERSION_CONFLICT, and every other failure
        // to a sanitized 500/INTERNAL_ERROR. Never expose exception messages or traces.
        throw new UnsupportedOperationException("Day 3 learner TODO: map API failures");
    }
}
