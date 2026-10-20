package assignments.insurance;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Thin instructor-owned HTTP adapter; validation and selection behavior remain learner-owned. */
@RestController
@RequestMapping("/api/quote-selections")
public final class QuoteSelectionController {
    private final QuoteSelectionService service;
    private final QuoteSelectionRequestValidation requestValidation;

    public QuoteSelectionController(QuoteSelectionPolicy policy) {
        this(policy, new QuoteSelectionRequestValidator());
    }

    @Autowired
    public QuoteSelectionController(QuoteSelectionService service,
                                    QuoteSelectionRequestValidation requestValidation) {
        this.service = service;
        this.requestValidation = requestValidation;
    }

    @PostMapping
    public List<QuoteSelectionKey> select(@RequestBody QuoteSelectionRequest request) {
        requestValidation.validate(request);
        return service.select(request.selections());
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> invalidInput() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("INVALID_REQUEST", "Request validation failed"));
    }
}
