package capstone.coverstory.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** CAP-101 learner seam. Context construction is local and never calls a model. */
public final class BoundAssistanceContext {
    private BoundAssistanceContext() {}

    public enum Visibility { PUBLIC, PRIVATE }
    public enum Status { READY, INPUT_TOO_LARGE, ABSTAIN }

    public record Principal(String ownerId) {}

    /** A chunk keeps its content behind an accessor so unauthorized payloads need not be read. */
    public static final class RankedChunk {
        private final String id;
        private final String ownerId;
        private final Visibility visibility;
        private final String citation;
        private final Content content;

        public RankedChunk(String id, String ownerId, Visibility visibility, String citation, Content content) {
            this.id = Objects.requireNonNull(id);
            this.ownerId = ownerId;
            this.visibility = Objects.requireNonNull(visibility);
            this.citation = Objects.requireNonNull(citation);
            this.content = Objects.requireNonNull(content);
        }
        public String id() { return id; }
        public String ownerId() { return ownerId; }
        public Visibility visibility() { return visibility; }
        public String citation() { return citation; }
        public String readContent() { return content.read(); }
    }

    @FunctionalInterface public interface Content { String read(); }
    @FunctionalInterface public interface TokenCounter { int count(String chunkId, String content); }

    public record RankedChunks(List<RankedChunk> ranked, TokenCounter tokenCounter) {
        public RankedChunks { ranked = List.copyOf(ranked); Objects.requireNonNull(tokenCounter); }
    }
    public record InputBudget(int baseTokens, int maxTokens) {}
    /** Kept separate from input budgeting; it is not subtracted from the input allowance. */
    public record OutputBudget(int reservedTokens) {}
    public record ContextPlan(Status status, List<String> includedIds, List<String> omittedIds,
                              List<String> citations, int inputTokens, int outputReservation,
                              boolean manualPath) {
        public ContextPlan {
            includedIds = List.copyOf(includedIds); omittedIds = List.copyOf(omittedIds);
            citations = List.copyOf(citations);
        }
    }

    public static ContextPlan buildContext(Principal principal, RankedChunks ranked,
                                           InputBudget input, OutputBudget output) {
        // LEARNER TODO: validate the base allowance, authorize before reading or counting
        // content (public sources are allowed; private sources require owner match), keep
        // chunks whole, preserve rank order and citations, omit foreign IDs from output,
        // and abstain when no authorized chunk can form a context. Do not call a model.
        throw new UnsupportedOperationException("CAP-101 learner TODO: build bounded authorized context");
    }
}
