package coverstory.application;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Thin HTTP adapter. It owns no business behavior and never logs exception contents. */
@RestControllerAdvice
public final class CoverStoryExceptionHandler {
    private final ExceptionToProblemDetail mapper;

    public CoverStoryExceptionHandler(ExceptionToProblemDetail mapper) {
        this.mapper = mapper;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handle(Exception exception, HttpServletRequest request) {
        KnownException safeCategory = exception instanceof KnownException known
                ? known
                : exception instanceof HttpMessageNotReadableException
                    ? new KnownException.MalformedRequest()
                    : new KnownException.UnexpectedFailure();
        RequestContext context = RequestContext.from(request);
        CoverStoryProblemDetail problem;
        try {
            problem = mapper.mapException(safeCategory, context);
        } catch (RuntimeException mappingFailure) {
            // Fail closed if the learner adapter itself fails. Never let a
            // framework error path serialize the original exception or cause.
            problem = new CoverStoryProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "An unexpected error occurred.", Map.of(), context.effectiveRequestId(), false);
        }
        return ResponseEntity.status(problem.status()).body(problem.toProblemDetail());
    }
}
