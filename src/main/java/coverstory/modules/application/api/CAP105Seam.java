package coverstory.modules.application.api;

import java.util.List;
import java.util.Objects;

/** Learner seam for a bounded checkpoint over evidence-backed demonstrations. */
public final class CAP105Seam {
    public enum EvidenceKind { LINKED_EXECUTION_OUTPUT, SCREENSHOT_ONLY, NONE }
    public enum Status { VERIFIED, UNVERIFIED, BLOCKED, NOT_RUN }

    public record Demonstration(String id, String scope, String command, String commit,
                                EvidenceKind evidenceKind, String evidenceLink, String observedOutput,
                                boolean passed) { }
    public record Defect(String id, String description, boolean critical) { }
    public record Finding(String demonstrationId, Status status, String evidenceLink,
                          String scope, String reason) { }
    public record CheckpointReport(List<Finding> findings, List<Defect> allDefects,
                                   List<String> selectedPriorityIds,
                                   List<String> unscheduledDefectIds,
                                   boolean complete, boolean harnessTrusted,
                                   boolean serviceAvailable) { }

    /**
     * Evaluate exactly three supplied demonstrations. Preserve every defect, select at
     * most two critical priorities, and never promote blocked/unrun work to a pass.
     */
    public CheckpointReport reviewEvidenceIndex(List<Demonstration> demonstrations,
                                                List<Defect> defects,
                                                List<String> selectedPriorityIds,
                                                boolean harnessTrusted,
                                                boolean serviceAvailable) {
        // TODO(CAP-105): validate evidence provenance, classify each demo, and reconcile
        // the complete defect list with an explicit top-two selection.
        throw new UnsupportedOperationException("TODO(CAP-105): reviewEvidenceIndex");
    }
}
