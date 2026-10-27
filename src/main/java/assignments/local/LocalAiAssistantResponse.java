package assignments.local;

import java.util.List;

/** Response DTO for the local AI training route; execution mode is always explicit. */
public record LocalAiAssistantResponse(
        String answer,
        List<Citation> citations,
        boolean abstained,
        String executionMode,
        boolean providerRequestMade,
        boolean liveQualityEvaluated) {
    public record Citation(String documentId, String version, String section) { }
}
