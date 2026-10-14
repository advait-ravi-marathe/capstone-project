package capstone.coverstory.application;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static capstone.coverstory.application.BoundAssistanceContext.*;

/** Learner contract. Failures at the explicit TODO are expected on the starter candidate. */
class CAP101AcceptanceTest {
    private final Principal ownerA = new Principal("owner-A");
    private final TokenCounter counter = (id, text) -> switch (id) {
        case "ai1" -> 500; case "ar1" -> 400; case "a3" -> 700; case "exact" -> 300;
        case "overflow" -> 1; case "br1" -> 90; default -> throw new IllegalArgumentException(id);
    };
    private RankedChunk c(String id, String owner, String text, int tokens) {
        return new RankedChunk(id, owner, Visibility.PRIVATE, "[" + id + "]", () -> text);
    }

    @Test void choosesOnlyWholeAuthorizedChunksInRankOrderWithCitations() {
        assertThrows(UnsupportedOperationException.class, () -> buildContext(ownerA,
            new RankedChunks(List.of(c("ai1", "owner-A", "A1", 500), c("ar1", "owner-A", "A2", 400), c("a3", "owner-A", "A3", 700)), counter),
            new InputBudget(300, 1500), new OutputBudget(256)));
    }
    @Test void oversizedBaseFailsBeforeReadingAnyChunk() {
        assertThrows(UnsupportedOperationException.class, () -> buildContext(ownerA,
            new RankedChunks(List.of(c("ai1", "owner-A", "A1", 500)), counter), new InputBudget(1501, 1500), new OutputBudget(256)));
    }
    @Test void foreignPrivateChunkIsExcludedBeforeTokenCounting() {
        assertThrows(UnsupportedOperationException.class, () -> buildContext(ownerA,
            new RankedChunks(List.of(c("br1", "owner-B", "B_PRIVATE_CANARY", 90), c("ai1", "owner-A", "A1", 500)), counter),
            new InputBudget(300, 1500), new OutputBudget(256)));
    }
    @Test void exactCapIsAllowedAndOverflowIsOmittedAsAWhole() {
        assertThrows(UnsupportedOperationException.class, () -> buildContext(ownerA,
            new RankedChunks(List.of(c("ai1", "owner-A", "A1", 500), c("ar1", "owner-A", "A2", 400), c("exact", "owner-A", "E", 300), c("overflow", "owner-A", "O", 1)), counter),
            new InputBudget(300, 1500), new OutputBudget(256)));
    }
    @Test void noAuthorizedOrFittingChunksRequiresAbstentionAndManualPath() {
        assertThrows(UnsupportedOperationException.class, () -> buildContext(ownerA,
            new RankedChunks(List.of(c("br1", "owner-B", "B_PRIVATE_CANARY", 90)), counter), new InputBudget(300, 1500), new OutputBudget(256)));
    }
}
