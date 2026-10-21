package assignments.learning;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Instructor-owned exception-translation example with a separate error contract. */
class Day03SafeErrorsReferenceTest {
    public record Problem(String problem) { }

    static final class ShelfBusy extends RuntimeException { }

    @RestController
    static final class ShelfController {
        @GetMapping("/teaching/shelf/{id}")
        String read(@PathVariable("id") String id) {
            if (id.equals("busy")) {
                throw new ShelfBusy();
            }
            throw new IllegalStateException("private database password-like fixture");
        }
    }

    @RestControllerAdvice
    static final class SafeAdvice {
        @ExceptionHandler(ShelfBusy.class)
        ResponseEntity<Problem> busy() {
            return ResponseEntity.status(409).body(new Problem("TRY_LATER"));
        }

        @ExceptionHandler(Exception.class)
        ResponseEntity<Problem> unexpected(Exception ignored) {
            return ResponseEntity.status(500).body(new Problem("UNAVAILABLE"));
        }
    }

    @Test
    void expectedAndUnexpectedFailuresHaveSafeDifferentOutcomes() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new ShelfController())
                .setControllerAdvice(new SafeAdvice()).build();

        mvc.perform(get("/teaching/shelf/busy"))
                .andExpect(status().isConflict())
                .andExpect(content().string("{\"problem\":\"TRY_LATER\"}"));
        mvc.perform(get("/teaching/shelf/other"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"problem\":\"UNAVAILABLE\"}"));
    }

    @Test
    void aNewIdentifierGetsTheSameSanitizedUnexpectedOutcome() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new ShelfController())
                .setControllerAdvice(new SafeAdvice()).build();

        mvc.perform(get("/teaching/shelf/new-id"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"problem\":\"UNAVAILABLE\"}"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("private database"))));
    }
}
