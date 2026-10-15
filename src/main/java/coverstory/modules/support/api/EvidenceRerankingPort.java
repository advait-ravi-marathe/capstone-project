package coverstory.modules.support.api;

import java.util.List;
import java.util.UUID;

/** Support-owned read seam: implementations may reorder only candidates already authorized upstream. */
public interface EvidenceRerankingPort {
    RankingResult evaluate(UUID authenticatedOwnerId, String fixedScenarioId);

    record RankingResult(String status, List<String> candidateIds, int elapsedMillis,
                         boolean timeoutRecorded, int rerankCalls,
                         double recallAt2, double meanReciprocalRank) {
        public RankingResult { candidateIds = List.copyOf(candidateIds); }
    }
}
