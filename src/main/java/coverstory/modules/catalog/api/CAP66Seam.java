package coverstory.modules.catalog.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Offline consumer-group report contract. This type never connects to Kafka. */
public final class CAP66Seam {
    public record ProbeRecord(String version, int partition, long offset) { }

    /** One member's recorded view in one bounded probe snapshot. */
    public record ProbeSnapshot(@JsonProperty("snapshot_id") String snapshotId,
                                @JsonProperty("group_id") String groupId,
                                @JsonProperty("member_id") String memberId,
                                @JsonProperty("assigned_partitions") List<Integer> assignedPartitions,
                                List<ProbeRecord> records,
                                @JsonProperty("committed_through_offset") Long committedThroughOffset,
                                @JsonProperty("next_read_offset") Long nextReadOffset,
                                @JsonProperty("restart_before_commit") boolean restartBeforeCommit,
                                boolean redelivered) { }

    public record GroupReport(String groupId, List<String> observedVersions,
                              int uniqueVersionCount, List<String> idleMembers,
                              boolean assignmentsDisjoint, boolean complete) { }

    public record RestartReport(String snapshotId, Long committedThroughOffset,
                                Long nextReadOffset, boolean redelivered,
                                String deliveryClaim) { }

    public record ObservationReport(List<GroupReport> groups, List<RestartReport> restarts,
                                    boolean valid, List<String> issues) { }

    /**
     * Summarize supplied recorded snapshots against the expected publication versions.
     * Learner TODO: validate snapshot membership/ownership, group-local coverage,
     * idle members, committed-next-offset, and pre-commit redelivery without
     * claiming exactly-once delivery.
     */
    public ObservationReport observe(List<ProbeSnapshot> recordedProbeSnapshots,
                                     List<String> expectedVersions) {
        throw new UnsupportedOperationException("CAP-66 learner TODO: observe recorded group probes");
    }
}
