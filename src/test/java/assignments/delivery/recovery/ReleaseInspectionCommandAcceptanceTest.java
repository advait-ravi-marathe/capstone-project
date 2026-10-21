package assignments.delivery.recovery;

import assignments.delivery.IssuanceSourceKey;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class ReleaseInspectionCommandAcceptanceTest {
    private final ReleaseInspectionRequestPolicy requests = new ReleaseInspectionRequestPolicy();

    @Test void day145RejectsMixedOrMalformedModesBeforeAnyDatabaseContext() {
        assertThrows(IllegalArgumentException.class, () -> requests.parse(new String[]{"--coverstory.release.inspect"}));
        assertThrows(IllegalArgumentException.class, () -> requests.parse(args("policy-a", "1", "--coverstory.recovery.action=retry")));
        assertThrows(IllegalArgumentException.class, () -> requests.parse(args("policy-a", "0")));
        assertThrows(IllegalArgumentException.class, () -> requests.parse(args("policy-a", "1", "--coverstory.release.unknown=x")));
    }

    @Test void day145AcceptsOnlyOneExplicitExactKeyRequest() {
        var request = requests.parse(args("policy-a", "1"));
        assertEquals(new IssuanceSourceKey("policy-a", 1), request.key());
        assertThrows(IllegalArgumentException.class, () -> requests.parse(new String[]{"--coverstory.release.inspect",
                "--coverstory.release.policy-id=policy-a", "--coverstory.release.policy-id=policy-b",
                "--coverstory.release.source-version=1"}));
    }

    @Test void day146ReportCommandNeverUsesMutationOrDeliveryMethods() throws Exception {
        String source = java.nio.file.Files.readString(java.nio.file.Path.of("src/main/java/assignments/delivery/recovery/ReleaseInspectionCommandRunner.java"));
        assertTrue(source.contains("recovery.inspect(request.key())"));
        assertFalse(source.contains("recovery.retryOne("));
        assertFalse(source.contains("recovery.quarantine("));
        assertFalse(source.contains("recovery.release("));
        assertFalse(source.contains("captureBatch("));
    }

    private static String[] args(String id, String version, String... extra) {
        var all = new java.util.ArrayList<String>(java.util.List.of("--coverstory.release.inspect",
                "--coverstory.release.policy-id="+id, "--coverstory.release.source-version="+version));
        all.addAll(java.util.List.of(extra));
        return all.toArray(String[]::new);
    }
}
