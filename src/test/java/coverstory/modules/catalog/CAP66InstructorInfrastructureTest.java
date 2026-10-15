package coverstory.modules.catalog;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.api.CAP66Seam.ProbeRecord;
import coverstory.modules.catalog.api.CAP66Seam.ProbeSnapshot;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Offline fixture and oracle checks; no Kafka client, broker, or topic is used. */
class CAP66InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-66/synthetic-v1.json");

    @Test void fixtureHasThreePartitionsSixExpectedPublicationsAndFourCases() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_OFFLINE_GROUP_TRACE", root.path("classification").asText());
        assertFalse(root.path("actual_broker_run").asBoolean());
        assertEquals(List.of(0, 1, 2), JSON.convertValue(root.path("partitions"), new TypeReference<>() { }));
        assertEquals(6, root.path("expected_versions").size());
        assertEquals(4, root.path("cases").size());
        JsonNode stable = caseById(root, "CAP-66-CS-01");
        assertEquals(3, stable.path("snapshots").size());
        List<String> expected = strings(root.path("expected_versions")).stream().sorted().toList();
        assertEquals(expected, caseUnion(stable, "G"));
        assertEquals(expected, caseUnion(stable, "H"));
        JsonNode fourMembers = caseById(root, "CAP-66-CS-02");
        ProbeSnapshot idle = snapshots(fourMembers).stream().filter(row -> "G4".equals(row.memberId())).findFirst().orElseThrow();
        assertTrue(idle.assignedPartitions().isEmpty());
        assertTrue(idle.records().isEmpty(), "fourth member is a valid idle member with only three partitions");
    }

    @Test void independentOracleAcceptsAllSavedCasesAndRejectsOwnershipOffsetAndDeliveryDefects() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        List<String> expected = strings(root.path("expected_versions"));
        for (JsonNode row : root.path("cases")) assertTrue(referenceValid(snapshots(row), expected), row.path("id").asText());

        List<ProbeSnapshot> stable = new ArrayList<>(snapshots(caseById(root, "CAP-66-CS-01")));
        ProbeSnapshot g2 = stable.get(1);
        stable.set(1, withPartitions(g2, List.of(1, 2)));
        assertFalse(referenceValid(stable, expected), "two members in one stable snapshot cannot own the same partition");

        List<ProbeSnapshot> committed = new ArrayList<>(snapshots(caseById(root, "CAP-66-CS-03")));
        committed.set(0, withNextRead(committed.getFirst(), 41L));
        assertFalse(referenceValid(committed, expected), "resume after committed n must start at n+1");

        List<ProbeSnapshot> preCommit = new ArrayList<>(snapshots(caseById(root, "CAP-66-CS-04")));
        preCommit.set(1, withRedelivered(preCommit.get(1), false));
        assertFalse(referenceValid(preCommit, expected), "repeated pre-commit record must be reported as redelivery");
    }

    private static boolean referenceValid(List<ProbeSnapshot> rows, List<String> expected) {
        if (rows == null || rows.isEmpty() || expected == null || expected.isEmpty() || expected.stream().anyMatch(v -> v == null || v.isBlank())) return false;
        Map<String, Set<String>> ownersBySnapshot = new HashMap<>();
        Map<String, Set<Integer>> assignedBySnapshot = new HashMap<>();
        Map<String, Set<Integer>> recordsBySnapshot = new HashMap<>();
        Map<String, Set<String>> groupVersions = new HashMap<>();
        Map<String, Set<String>> members = new HashMap<>();
        Map<String, Long> offsets = new HashMap<>();
        Map<String, ProbeRecord> seenRecords = new HashMap<>();
        Map<String, Boolean> repeatReported = new HashMap<>();

        for (ProbeSnapshot row : rows) {
            if (row == null || blank(row.snapshotId()) || blank(row.groupId()) || blank(row.memberId())
                    || row.assignedPartitions() == null || row.records() == null) return false;
            String snapshotKey = row.snapshotId() + "\u0000" + row.groupId();
            String memberKey = snapshotKey + "\u0000" + row.memberId();
            if (!ownersBySnapshot.computeIfAbsent(snapshotKey, ignored -> new HashSet<>()).add(row.memberId())) return false;
            Set<Integer> assignments = assignedBySnapshot.computeIfAbsent(snapshotKey, ignored -> new HashSet<>());
            for (Integer partition : row.assignedPartitions()) if (partition == null || partition < 0 || !assignments.add(partition)) return false;
            members.computeIfAbsent(snapshotKey, ignored -> new HashSet<>()).add(row.memberId());

            if ((row.committedThroughOffset() == null) != (row.nextReadOffset() == null)) return false;
            if (row.committedThroughOffset() != null && row.nextReadOffset() != row.committedThroughOffset() + 1) return false;
            if (row.redelivered() && !row.restartBeforeCommit()) return false;

            for (ProbeRecord record : row.records()) {
                if (record == null || blank(record.version()) || !expected.contains(record.version()) || record.partition() < 0 || record.offset() < 0
                        || !row.assignedPartitions().contains(record.partition())) return false;
                recordsBySnapshot.computeIfAbsent(memberKey, ignored -> new HashSet<>()).add(record.partition());
                String recordIdentity = row.groupId() + "\u0000" + record.partition() + "\u0000" + record.offset();
                ProbeRecord previous = seenRecords.putIfAbsent(recordIdentity, record);
                if (previous != null) {
                    if (!previous.equals(record) || !row.redelivered() || !row.restartBeforeCommit()) return false;
                    repeatReported.put(recordIdentity, true);
                } else if (row.redelivered() && !row.restartBeforeCommit()) return false;
                groupVersions.computeIfAbsent(snapshotKey, ignored -> new HashSet<>()).add(record.version());
                String offsetKey = row.groupId() + "\u0000" + record.partition();
                Long prior = offsets.put(offsetKey, record.offset());
                if (prior != null && prior > record.offset() && !row.redelivered()) return false;
            }
        }
        for (String identity : seenRecords.keySet()) {
            ProbeRecord record = seenRecords.get(identity);
            boolean repeated = repeatReported.getOrDefault(identity, false);
            if (repeated && rows.stream().noneMatch(r -> r.redelivered() && r.records().contains(record))) return false;
        }
        return true;
    }

    private static List<String> caseUnion(JsonNode row, String group) {
        Set<String> versions = new HashSet<>();
        for (ProbeSnapshot snapshot : snapshots(row)) if (group.equals(snapshot.groupId()))
            for (ProbeRecord record : snapshot.records()) versions.add(record.version());
        return versions.stream().sorted().toList();
    }

    private static List<ProbeSnapshot> snapshots(JsonNode row) {
        return JSON.convertValue(row.path("snapshots"), new TypeReference<>() { });
    }

    private static List<String> strings(JsonNode array) { return JSON.convertValue(array, new TypeReference<>() { }); }

    private static JsonNode caseById(JsonNode root, String id) {
        for (JsonNode row : root.path("cases")) if (id.equals(row.path("id").asText())) return row;
        throw new IllegalArgumentException("Fixture case not found: " + id);
    }

    private static ProbeSnapshot withPartitions(ProbeSnapshot row, List<Integer> partitions) {
        return new ProbeSnapshot(row.snapshotId(), row.groupId(), row.memberId(), partitions, row.records(), row.committedThroughOffset(), row.nextReadOffset(), row.restartBeforeCommit(), row.redelivered());
    }

    private static ProbeSnapshot withNextRead(ProbeSnapshot row, long next) {
        return new ProbeSnapshot(row.snapshotId(), row.groupId(), row.memberId(), row.assignedPartitions(), row.records(), row.committedThroughOffset(), next, row.restartBeforeCommit(), row.redelivered());
    }

    private static ProbeSnapshot withRedelivered(ProbeSnapshot row, boolean redelivered) {
        return new ProbeSnapshot(row.snapshotId(), row.groupId(), row.memberId(), row.assignedPartitions(), row.records(), row.committedThroughOffset(), row.nextReadOffset(), row.restartBeforeCommit(), redelivered);
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
}
