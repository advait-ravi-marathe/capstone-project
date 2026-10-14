package coverstory.application;

@FunctionalInterface
public interface ExceptionToProblemDetail {
    CoverStoryProblemDetail mapException(KnownException ex, RequestContext ctx);
}
