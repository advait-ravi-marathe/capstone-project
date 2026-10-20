package assignments.ops;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class ReleaseRehearsalPolicyAcceptanceTest {
    @Test void day127RejectsUnownedOrRemoteTargets() {
        var policy = new RehearsalTargetPolicy();
        var source = new RehearsalTargetPolicy.FixtureHandle("run-1", "127.0.0.1", "source", true);
        var target = new RehearsalTargetPolicy.FixtureHandle("run-1", "127.0.0.1", "target", true);
        assertDoesNotThrow(() -> policy.validate(source, target, false));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(source,
                new RehearsalTargetPolicy.FixtureHandle("other", "localhost", "unowned", false), false));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(source,
                new RehearsalTargetPolicy.FixtureHandle("run-1", "example.com", "remote", true), false));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(source, target, true));
    }

    @Test void day128ChecksArtifactAndMigrationIdentity() {
        var policy = new ReleaseManifestPolicy();
        var manifest = new ReleaseManifestPolicy.ReleaseManifest("abc123", "a".repeat(64), "21.0.8",
                "18.6", "18.6", Map.of("V001.sql", "b".repeat(64)), false);
        assertDoesNotThrow(() -> policy.validate(manifest, "a".repeat(64), Map.of("V001.sql", "b".repeat(64))));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(manifest, "c".repeat(64), manifest.migrationSha256()));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(new ReleaseManifestPolicy.ReleaseManifest(
                "abc123", manifest.jarSha256(), "21", "18.6", "18.6", manifest.migrationSha256(), true), manifest.jarSha256(), manifest.migrationSha256()));
    }

    @Test void day129ComparesMeaningNotOnlyCounts() {
        var policy = new RehearsalEvidencePolicy();
        String full = "policy=restore-complete|receipt.payload=ISSUED:2026-12-01|received_at=11:01Z|work.ack=12:00Z|hold.rev=3|history=1,2,3|V001..V005";
        assertDoesNotThrow(() -> policy.requireRestoreMatch(full, full));
        assertThrows(IllegalStateException.class, () -> policy.requireRestoreMatch(full,
                "policy=restore-complete|receipt.payload=ISSUED:2026-12-01|received_at=11:02Z|work.ack=12:00Z|hold.rev=3|history=1,2,3|V001..V005"));
    }

    @Test void day130ChoosesOnlyTheFixedCustomArchiveCommand() {
        var policy = new BackupCommandPolicy();
        assertEquals(List.of("--format=custom", "--no-password"), policy.arguments("source", "custom", List.of()));
        assertThrows(IllegalArgumentException.class, () -> policy.arguments("source", "plain", List.of()));
        assertThrows(IllegalArgumentException.class, () -> policy.arguments("source", "custom", List.of("--clean", "--create")));
    }

    @Test void day131RestoresFreshTargetFailClosed() {
        var policy = new RestoreCommandPolicy();
        var args = policy.arguments("source", "restore_00000000000000000000000000000001", "a".repeat(64), "a".repeat(64), true,
                List.of("--single-transaction", "--no-owner", "--no-acl"));
        assertEquals(List.of("--single-transaction", "--no-owner", "--no-acl"), args);
        assertThrows(IllegalArgumentException.class, () -> policy.arguments("source", "source", "a".repeat(64), "a".repeat(64), true, List.of()));
        assertThrows(IllegalArgumentException.class, () -> policy.arguments("source", "restore_00000000000000000000000000000001", "a".repeat(64), "a".repeat(64), false, List.of("--clean")));
        assertThrows(IllegalArgumentException.class, () -> policy.arguments("source", "restore_00000000000000000000000000000001", "b".repeat(64), "a".repeat(64), true, List.of()));
    }

    @Test void day132RequiresDataAndApplicationEvidence() {
        var policy = new RehearsalEvidencePolicy();
        var good = new RehearsalEvidencePolicy.PhaseEvidence(true, true, true, true, "full-A", "full-A");
        assertDoesNotThrow(() -> policy.requireActivationEvidence(good));
        assertThrows(IllegalStateException.class, () -> policy.requireActivationEvidence(
                new RehearsalEvidencePolicy.PhaseEvidence(true, false, true, true, "full-A", "missing-history")));
    }

    @Test void day134FailedCandidateCannotDisplaceKnownGood() {
        var policy = new ActivationDecisionPolicy();
        assertEquals(ActivationDecisionPolicy.Selection.CANDIDATE, policy.choose(true, true, true, true, true));
        assertEquals(ActivationDecisionPolicy.Selection.CURRENT, policy.choose(true, true, true, false, true));
        assertEquals(ActivationDecisionPolicy.Selection.CURRENT, policy.choose(true, true, true, true, false));
    }

    @Test void day135BlocksRollbackAfterDivergentWrites() {
        var policy = new RollbackDecisionPolicy();
        assertTrue(policy.mayReturnToSource(true, true, true));
        assertFalse(policy.mayReturnToSource(true, true, false));
        assertFalse(policy.mayReturnToSource(true, false, true));
        assertFalse(policy.mayReturnToSource(false, true, true));
    }

    @Test void day136RequiresOwnedCleanupEvidence() {
        var policy = new RehearsalEvidencePolicy();
        var completed = new RehearsalEvidencePolicy.PhaseEvidence(true, true, true, true, "source", "source");
        assertDoesNotThrow(() -> policy.requireSuccessfulCleanup(completed, true));
        assertThrows(IllegalStateException.class, () -> policy.requireSuccessfulCleanup(
                new RehearsalEvidencePolicy.PhaseEvidence(true, true, true, false, "source", "source"), true));
        assertThrows(IllegalStateException.class, () -> policy.requireSuccessfulCleanup(completed, false));
    }

    @Test void day139FullLocalRehearsalLeavesCloudUnperformed() {
        var policy = new RehearsalEvidencePolicy();
        assertDoesNotThrow(() -> policy.requireOfflineReleaseGate(true, true, true, true, true, false));
        assertThrows(IllegalArgumentException.class, () -> policy.requireOfflineReleaseGate(true, true, true, true, true, true));
        assertThrows(IllegalStateException.class, () -> policy.requireOfflineReleaseGate(true, false, true, true, true, false));
        assertThrows(IllegalStateException.class, () -> policy.requireOfflineReleaseGate(true, true, true, true, false, false));
    }
}
