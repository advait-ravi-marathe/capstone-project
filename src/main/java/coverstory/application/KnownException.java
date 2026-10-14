package coverstory.application;

import java.util.Map;

/** Typed, safe exception categories shared by prebuilt draft/payment handlers. */
public abstract class KnownException extends RuntimeException {
    private KnownException() { }

    public static final class MalformedRequest extends KnownException { }
    public static final class SessionRequired extends KnownException { }
    public static final class ResourceNotFound extends KnownException { }
    public static final class QuoteConflict extends KnownException { }
    public static final class IdempotencyConflict extends KnownException { }
    public static final class StaleVersion extends KnownException { }
    public static final class UnexpectedFailure extends KnownException { }

    public enum Field { FULL_NAME("fullName"), EMAIL("email"), CONSENT("consent");
        private final String wireName;
        Field(String wireName) { this.wireName = wireName; }
        public String wireName() { return wireName; }
    }

    public enum FieldProblem { REQUIRED, INVALID_FORMAT }

    public static final class IncompleteFields extends KnownException {
        private final Map<Field, FieldProblem> fields;

        public IncompleteFields(Map<Field, FieldProblem> fields) {
            this.fields = Map.copyOf(fields);
        }

        public Map<Field, FieldProblem> fields() { return fields; }
    }
}
