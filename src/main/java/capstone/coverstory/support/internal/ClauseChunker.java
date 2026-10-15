package capstone.coverstory.support.internal;

import java.util.List;

/** Learner seam for CAP-120. Offsets are half-open UTF-8 byte offsets. */
public final class ClauseChunker {
    private ClauseChunker() { }

    public record Span(int startByte, int endByte) { }
    public record Config(int sizeTokens, int overlapTokens) { }
    public record Chunk(int startByte, int endByte, int tokenCount) { }
    public record Result(List<Chunk> chunks, int contextTokens, double clauseRecall) { }

    public static Result chunk(String source, List<Span> goldClauses, Config config) {
        // TODO(CAP-120): validate all byte spans against the UTF-8 source, reject
        // overlap/invalid ranges, build deterministic token windows, and measure
        // clause recall and duplicated context cost. Preserve source byte offsets.
        throw new UnsupportedOperationException("TODO(CAP-120): implement clause-preserving chunk metrics");
    }
}
