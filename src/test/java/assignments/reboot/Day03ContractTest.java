package assignments.reboot;

import assignments.insurance.ApiError;
import assignments.insurance.ApiExceptionHandler;
import assignments.insurance.ResourceNotFoundException;
import assignments.insurance.VersionConflictException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Day 3 tests exercise only the exception handler with fixed exception fixtures. */
@Tag("learnerAcceptance")
class Day03ContractTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void mapsUnknownResourceToNonDisclosingNotFound() {
        ResponseEntity<ApiError> response = handler.handle(new ResourceNotFoundException());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(new ApiError("RESOURCE_NOT_FOUND", "Resource was not found"), response.getBody());
    }

    @Test
    void mapsStaleVersionToConflict() {
        ResponseEntity<ApiError> response = handler.handle(new VersionConflictException());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(new ApiError("VERSION_CONFLICT", "The resource version conflicts"), response.getBody());
    }

    @Test
    void unexpectedFailureIsSanitized() {
        ResponseEntity<ApiError> response = handler.handle(new IllegalStateException("secret SQL and stack detail"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(new ApiError("INTERNAL_ERROR", "The request could not be completed"), response.getBody());
    }
}
