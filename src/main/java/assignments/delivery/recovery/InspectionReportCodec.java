package assignments.delivery.recovery;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;

/** One framed, size-bounded JSON report. Process diagnostics are not part of the report. */
public final class InspectionReportCodec {
    public static final String PREFIX = "COVERSTORY_INSPECTION_REPORT ";
    public static final int MAX_DOCUMENT_BYTES = 64 * 1024;
    public static final int MAX_DEPTH = 16;
    private final ObjectMapper mapper;

    public InspectionReportCodec(ObjectMapper mapper) { this.mapper = Objects.requireNonNull(mapper); }

    public String encode(InspectionReport report) throws IOException {
        // LEARNER TODO Day 143: serialize exactly one bounded JSON record after complete mapping;
        // output it only after serialization succeeds so failures cannot emit a partial success frame.
        throw new UnsupportedOperationException("Day 143 learner TODO: emit one framed report");
    }

    public InspectionReport decodeSingleFrame(String processOutput, InspectionReport.RequestedKey requestedKey) throws IOException {
        // LEARNER TODO Day 144: require exactly one prefix frame, enforce byte/depth limits,
        // duplicate-member rejection, supported version/types/key and classifier consistency.
        throw new UnsupportedOperationException("Day 144 learner TODO: validate one bound report frame");
    }

    private JsonNode readStrict(String json) throws IOException {
        // Instructor-supplied parser hook; use strict duplicate detection at the token layer.
        return mapper.readTree(json);
    }
}
