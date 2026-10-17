package coverstory.modules.shortlist.refresh;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Shared immutable value shapes for branch-safe shortlist refresh. Public IDs are references, not authority. */
public final class CheckpointTypes {
    private CheckpointTypes() { }

    public record SourceVersion(UUID productId, String versionId, String sourceSha256) { }
    public record Slot(String slotId, String scenarioId, SourceVersion source) { }
    public record Checkpoint(UUID checkpointId, UUID shortlistId, UUID ownerId, long revision,
                             String scenarioId, List<Slot> slots) {
        public Checkpoint { slots = slots == null ? null : List.copyOf(slots); }
    }
    public record Branch(long baseRevision, List<Slot> selectedSlots, List<String> explicitlyRemovedSlotIds) {
        public Branch {
            selectedSlots = selectedSlots == null ? null : List.copyOf(selectedSlots);
            explicitlyRemovedSlotIds = explicitlyRemovedSlotIds == null ? null : List.copyOf(explicitlyRemovedSlotIds);
        }
    }
    public enum LookupStatus { AVAILABLE, ABSENT, UNAVAILABLE }
    public record SourceLookup(LookupStatus status, SourceVersion source) { }
    public record CatalogSnapshot(long generation, Map<String, SourceLookup> bySlotId) {
        public CatalogSnapshot { bySlotId = bySlotId == null ? null : Map.copyOf(bySlotId); }
    }
    public enum Outcome { RESOLVED, CONFLICT, UNAVAILABLE, INVALID }
    public record Conflict(String slotId, SourceVersion base, SourceVersion local,
                           SourceVersion upstream, String reason) { }
    public record RefreshResult(Outcome outcome, long baseRevision, long catalogGeneration,
                               List<Slot> proposedSlots, List<Conflict> conflicts,
                               List<String> unavailableSlotIds, String previewFingerprint,
                               List<String> violations) {
        public RefreshResult {
            proposedSlots = proposedSlots == null ? null : List.copyOf(proposedSlots);
            conflicts = conflicts == null ? null : List.copyOf(conflicts);
            unavailableSlotIds = unavailableSlotIds == null ? null : List.copyOf(unavailableSlotIds);
            violations = violations == null ? null : List.copyOf(violations);
        }
    }
}
