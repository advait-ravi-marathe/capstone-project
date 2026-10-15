package capstone.coverstory.support;

import capstone.coverstory.support.internal.EvidenceToolBudget;
import capstone.coverstory.support.internal.EvidenceToolBudget.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Independent deterministic model of the saved bounded read-only loop contract. */
final class CAP126ReferenceOracle {
    private CAP126ReferenceOracle() { }
    static Investigation investigate(List<Proposal> proposals, ReadOnlyEvidencePort port, int deadlineMillis) {
        if (deadlineMillis < 0) throw new IllegalArgumentException("deadlineMillis");
        List<TraceEntry> trace = new ArrayList<>();
        Set<String> signatures = new HashSet<>();
        int elapsed = 0;
        for (Proposal proposal : proposals) {
            String signature = signature(proposal);
            if (!signatures.add(signature)) return new Investigation(trace, Stop.REPEATED_CALL, "INSUFFICIENT_EVIDENCE");
            if (trace.size() == 3) return new Investigation(trace, Stop.STEP_LIMIT, "INSUFFICIENT_EVIDENCE");
            if (elapsed >= deadlineMillis) return new Investigation(trace, Stop.DEADLINE, "INSUFFICIENT_EVIDENCE");
            ReadResult result = port.read(proposal);
            trace.add(new TraceEntry(proposal, result));
            elapsed += Math.max(0, result.elapsedMillis());
            if (result.dispatch() == Dispatch.UNKNOWN) {
                return new Investigation(trace, Stop.RECONCILIATION_REQUIRED, "INSUFFICIENT_EVIDENCE");
            }
            if (elapsed >= deadlineMillis) return new Investigation(trace, Stop.DEADLINE, "INSUFFICIENT_EVIDENCE");
        }
        return new Investigation(trace, Stop.EXHAUSTED, trace.isEmpty() ? "INSUFFICIENT_EVIDENCE" : "EVIDENCE_RECORDED");
    }

    static String signature(Proposal p) {
        return String.join("\u001f", norm(p.tool()), norm(p.ownerScope()), norm(p.journeyId()), norm(p.query()));
    }
    private static String norm(String value) { return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT); }
}
