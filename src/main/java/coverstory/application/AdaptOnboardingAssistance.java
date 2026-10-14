package coverstory.application;

import java.util.List;
import java.util.Objects;

/** CAP-91 bounded, read-only boundary for optional synthetic onboarding help. */
public final class AdaptOnboardingAssistance {
    public record AuthorizedQuestion(String ownerId, String text) { }
    public record EvidenceChunk(String sourceId, String version, String text) { }
    public record Citation(String sourceId, String version, int start, int end) { }
    public record Usage(int inputTokens, int outputTokens) { }
    public record ModelDraft(String answer, String suggestedPin, List<Citation> citations, Usage usage) {
        public ModelDraft { citations = citations == null ? null : List.copyOf(citations); }
    }
    public record ManualDraft(String pin, String disclosureText) { }
    public record Suggestion(String pin, boolean confirmed) { }
    public enum Outcome { SUGGESTED, TIMEOUT, MALFORMED }
    public record Result(Outcome outcome, String answer, List<Citation> citations, Usage usage,
                         Suggestion suggestion, ManualDraft manualDraft) { }

    @FunctionalInterface
    public interface OnboardingAssistPort {
        ModelDraft suggest(AuthorizedQuestion question, List<EvidenceChunk> permittedEvidence) throws AssistTimeout;
    }
    public static final class AssistTimeout extends Exception {
        public AssistTimeout(String message) { super(message); }
    }

    private final OnboardingAssistPort port;
    public AdaptOnboardingAssistance(OnboardingAssistPort port) { this.port = Objects.requireNonNull(port); }

    /**
     * Learner seam: map one optional port response to an unconfirmed, source-grounded result.
     * This method has no Application write port by design; manual draft remains authoritative.
     */
    public Result suggest(AuthorizedQuestion question, List<EvidenceChunk> permittedEvidence, ManualDraft manualDraft) {
        // LEARNER TODO: call once; map timeout to TIMEOUT and incomplete/unlinked responses to
        // MALFORMED; preserve exact citations and usage on success; never confirm or mutate a draft.
        throw new UnsupportedOperationException("CAP-91 learner TODO: map optional onboarding assistance");
    }
}
