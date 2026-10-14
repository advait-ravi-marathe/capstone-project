package coverstory.application;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Pattern;

/** Independent instructor oracle; no application/deployment side effects. */
final class CAP99ReferenceEvaluator {
    private static final Pattern SHA = Pattern.compile("[a-fA-F0-9]{40}");
    private static final Pattern HASH = Pattern.compile("[a-fA-F0-9]{64}");

    static CAP99Seam.ReadinessDecision evaluate(List<CAP99Seam.GateEvidence> input) {
        var byGate = new EnumMap<CAP99Seam.Gate, CAP99Seam.GateEvidence>(CAP99Seam.Gate.class);
        var findings = new ArrayList<String>();
        boolean blocked = false, unverified = false;
        if (input == null) return new CAP99Seam.ReadinessDecision(CAP99Seam.Decision.UNVERIFIED,
                List.of("all six gate records are absent"));
        for (var evidence : input) {
            if (evidence == null || evidence.gate() == null) {
                findings.add("invalid gate record"); unverified = true; continue;
            }
            if (byGate.putIfAbsent(evidence.gate(), evidence) != null) {
                findings.add("duplicate gate: " + evidence.gate()); blocked = true;
            }
        }
        for (var gate : CAP99Seam.Gate.values()) {
            var e = byGate.get(gate);
            if (e == null) {
                String reason = gate == CAP99Seam.Gate.MIGRATION_COMPATIBILITY_RESTORE
                        ? "migration evidence missing (CAP-98 prerequisite)"
                        : "missing gate evidence: " + gate;
                findings.add(reason); blocked = true; continue;
            }
            if (e.status() == CAP99Seam.EvidenceStatus.FAIL) {
                findings.add(gate == CAP99Seam.Gate.OWNERSHIP
                        ? "cross-owner failure: " + safeIssue(e.issue())
                        : "gate failed: " + gate + ": " + safeIssue(e.issue()));
                blocked = true; continue;
            }
            if (e.status() != CAP99Seam.EvidenceStatus.VERIFIED || !hasActualReceipt(e.receipt())) {
                findings.add("UNVERIFIED evidence: " + gate);
                unverified = true;
            }
        }
        if (blocked) return new CAP99Seam.ReadinessDecision(CAP99Seam.Decision.BLOCKED, findings);
        if (unverified) return new CAP99Seam.ReadinessDecision(CAP99Seam.Decision.UNVERIFIED, findings);
        return new CAP99Seam.ReadinessDecision(CAP99Seam.Decision.READY_FOR_REVIEW,
                List.of("all six actual gates verified; local review only"));
    }

    private static boolean hasActualReceipt(CAP99Seam.Receipt r) {
        return r != null && r.kind() == CAP99Seam.EvidenceKind.TEST_OUTPUT
                && nonblank(r.command()) && nonblank(r.observedOutput())
                && r.observedOutput().contains("PASS")
                && r.sourceCommit() != null && SHA.matcher(r.sourceCommit()).matches()
                && r.fixtureSha256() != null && HASH.matcher(r.fixtureSha256()).matches();
    }
    private static boolean nonblank(String s) { return s != null && !s.isBlank(); }
    private static String safeIssue(String issue) {
        if (issue == null || issue.isBlank()) return "unspecified";
        return issue.replaceAll("(?i)(password|token|secret|api[_-]?key)=\\S+", "$1=[REDACTED]");
    }
}
