package coverstory.modules.catalog.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** CAP-55 offline partition-trace contract; it never connects to Kafka. */
public final class CAP55Seam {
    public record CatalogPublicationEnvelope(@JsonProperty("source_id") String sourceId, String version, int sequence) { }

    /** Every observation records provenance as well as the synthetic delivery tuple. */
    public record RecordedObservation(@JsonProperty("evidence_source") String evidenceSource,
                                      @JsonProperty("fixture_version") String fixtureVersion,
                                      @JsonProperty("claims_actual_broker_run") boolean claimsActualBrokerRun,
                                      @JsonProperty("source_id") String sourceId,
                                      String version, Integer sequence, Integer partition, Long offset) { }

    public record ProbeReport(boolean valid, String code, int observationCount) { }

    public ProbeReport validatePartitionTrace(List<CatalogPublicationEnvelope> publications,
                                              List<RecordedObservation> observations) {
        // LEARNER TODO: validate complete offline provenance, stable source-key
        // locality, and increasing sequence offsets; never invent missing data
        // or claim this synthetic trace is a real broker run.
        throw new UnsupportedOperationException("CAP-55 learner TODO: validate offline partition trace");
    }
}
