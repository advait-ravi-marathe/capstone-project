package coverstory.modules.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.api.CAP66Seam;
import coverstory.modules.catalog.api.CAP66Seam.ProbeRecord;
import coverstory.modules.catalog.api.CAP66Seam.ProbeSnapshot;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP66AcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-66/synthetic-v1.json");
    private final CAP66Seam seam = new CAP66Seam();

    @Test void stableGroupsIndependentlyCoverSixAndMembersDoNotShareAssignments() throws Exception {
        JsonNode row = caseById("CAP-66-CS-01");
        var report = seam.observe(snapshots(row), expectedVersions());
        assertTrue(report.valid());
        assertEquals(2, report.groups().size());
        assertEquals(6, group(report, "G").uniqueVersionCount());
        assertEquals(6, group(report, "H").uniqueVersionCount());
        assertTrue(group(report, "G").assignmentsDisjoint());
    }

    @Test void fourthMemberMayBeIdleWhenThreePartitionsHaveThreeOwners() throws Exception {
        var report = seam.observe(snapshots(caseById("CAP-66-CS-02")), expectedVersions());
        assertTrue(report.valid());
        assertEquals(List.of("G4"), group(report, "G").idleMembers());
    }

    @Test void committedOffsetResumesAtNextOffset() throws Exception {
        var report = seam.observe(snapshots(caseById("CAP-66-CS-03")), expectedVersions());
        assertTrue(report.valid());
        assertEquals(41L, report.restarts().getFirst().committedThroughOffset());
        assertEquals(42L, report.restarts().getFirst().nextReadOffset());
    }

    @Test void restartBeforeCommitReportsRedeliveryWithoutExactlyOnceClaim() throws Exception {
        var report = seam.observe(snapshots(caseById("CAP-66-CS-04")), expectedVersions());
        assertTrue(report.valid());
        assertTrue(report.restarts().stream().anyMatch(CAP66Seam.RestartReport::redelivered));
        assertFalse(report.restarts().stream().anyMatch(r -> r.deliveryClaim().contains("EXACTLY_ONCE")));
    }

    private static CAP66Seam.GroupReport group(CAP66Seam.ObservationReport report, String id) {
        return report.groups().stream().filter(row -> id.equals(row.groupId())).findFirst().orElseThrow();
    }

    private static JsonNode caseById(String id) throws Exception {
        for (JsonNode row : JSON.readTree(Files.readString(FIXTURE)).path("cases"))
            if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }

    private static List<String> expectedVersions() throws Exception {
        return JSON.convertValue(JSON.readTree(Files.readString(FIXTURE)).path("expected_versions"), new TypeReference<>() { });
    }

    private static List<ProbeSnapshot> snapshots(JsonNode row) {
        return JSON.convertValue(row.path("snapshots"), new TypeReference<>() { });
    }
}
