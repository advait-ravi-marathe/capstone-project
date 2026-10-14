package coverstory.application;

import java.util.List;
import java.util.Set;

/** CAP-97 bounded evaluation seam. It scores supplied synthetic outputs; it never calls a model. */
public final class CompareGroundedAssistanceWithGuessing {
    public record Question(String id, String ownerId, boolean answerable, String supportedAnswer,
                           Set<String> permittedSourceIds) { }

    public record Source(String id, String ownerId, String kind, Set<String> supportsQuestionIds) { }

    /** A null answer denotes abstention. Keep every answer and citation in the report input. */
    public record Explanation(String questionId, String answer, List<String> citationSourceIds) { }

    public record ModeOutput(String mode, List<Explanation> explanations) { }

    public record ModeReport(int supportedCorrect, int totalQuestions, int unsafeGuesses,
                             int correctOrSafeAbstentions, int validCitations,
                             int citationOpportunities, int leakageCount, boolean safetyPassed,
                             List<String> unsupportedQuestionIds, List<String> unsafeGuessQuestionIds,
                             List<String> leakingQuestionIds) { }

    public record ComparisonReport(ModeReport modelOnly, ModeReport sourceGrounded,
                                   String scope, boolean superiorityClaimAllowed) { }

    public ComparisonReport scoreModes(List<Question> questions, List<Source> sources,
                                       ModeOutput modelOnly, ModeOutput sourceGrounded) {
        // LEARNER TODO: score exact synthetic answerability/citation labels, preserve every
        // unsafe guess, and fail safety when any citation crosses owner boundaries.
        throw new UnsupportedOperationException("CAP-97 learner TODO: implement scoreModes");
    }
}
