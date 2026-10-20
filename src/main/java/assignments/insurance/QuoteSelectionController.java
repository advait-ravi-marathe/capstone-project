package assignments.insurance;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Thin provided API adapter; policy behavior remains the learner's responsibility. */
@RestController
@RequestMapping("/api/quote-selections")
public final class QuoteSelectionController {
    private final QuoteSelectionPolicy policy;

    public QuoteSelectionController(QuoteSelectionPolicy policy) {
        this.policy = policy;
    }

    @PostMapping
    public List<QuoteSelectionKey> select(@RequestBody QuoteSelectionRequest request) {
        if (request == null || request.selections() == null) {
            throw new IllegalArgumentException("selections is required");
        }
        return policy.uniqueSelections(request.selections());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> invalidInput() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
