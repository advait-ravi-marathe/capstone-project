package dev.coverstory.drills;

import java.util.Comparator;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PilotControllerAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalidRequest(MethodArgumentNotValidException exception) {
        String field = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField()).min(Comparator.naturalOrder()).orElse("request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("code", "invalid_request", "field", field));
    }
}
