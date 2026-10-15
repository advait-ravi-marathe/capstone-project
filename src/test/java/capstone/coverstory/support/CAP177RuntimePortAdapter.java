package capstone.coverstory.support;

import coverstory.modules.support.api.LifecycleEvidenceReadPort;
import capstone.coverstory.support.internal.BoundedLifecycleEvidenceEnvelope.EvidenceRow;
import java.util.List;
import java.util.UUID;

/** Synthetic lifecycle adapter for an isolated HTTP runtime test. No cancellation capability is exposed. */
public final class CAP177RuntimePortAdapter implements LifecycleEvidenceReadPort {
    public static final UUID OWNER_A = UUID.fromString("17700000-0000-4000-8000-000000000001");
    public static final UUID APPLICATION_A = UUID.fromString("17700000-0000-4000-8000-000000000011");
    private static final List<EvidenceRow> ROWS = List.of(
            new EvidenceRow("r1", "SUBMISSION", "v1", "Offer accepted"),
            new EvidenceRow("r2", "PAYMENT", "p1", "Payment recorded"),
            new EvidenceRow("r3", "DISPATCH_ACK", "d1", "Simulated dispatch acknowledged 🚚"));

    @Override public EvidenceResult read(UUID owner, UUID applicationId, int maxRows, int maxUtf8Bytes) {
        if (!OWNER_A.equals(owner) || !APPLICATION_A.equals(applicationId))
            return new EvidenceResult(0, 0, false, List.of(), 0, "Evidence not found.");
        List<capstone.coverstory.support.internal.BoundedLifecycleEvidenceEnvelope.EvidenceRow> authorized = ROWS.stream()
                .map(r -> new capstone.coverstory.support.internal.BoundedLifecycleEvidenceEnvelope.EvidenceRow(
                        r.id(), r.kind(), r.sourceVersion(), r.summary())).toList();
        CAP177ReferenceOracle.Result result;
        try { result = CAP177ReferenceOracle.build(authorized, maxRows, maxUtf8Bytes); }
        catch (capstone.coverstory.support.internal.BoundedLifecycleEvidenceEnvelope.TooSmallException tooSmall) {
            throw new LifecycleEvidenceReadPort.TooSmallException();
        }
        int bytes = CAP177ReferenceOracle.serialize(result.envelope()).length;
        return new EvidenceResult(result.envelope().includedCount(), result.envelope().availableCount(),
                result.envelope().truncated(), result.envelope().rows().stream().map(r ->
                        new LifecycleEvidenceReadPort.EvidenceRow(r.id(), r.kind(), r.sourceVersion(), r.summary())).toList(),
                bytes, result.safeConclusion());
    }
}
