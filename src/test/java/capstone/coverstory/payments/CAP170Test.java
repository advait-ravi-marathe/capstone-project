package capstone.coverstory.payments;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP170Test {
    private final ValidatePaymentIdentities learner = new ValidatePaymentIdentities();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test void acceptsEpochResetAndIndependentOwnerAndEventStreams() throws Exception {
        for (String id : List.of("CAP-170-CS-01", "CAP-170-CS-07", "CAP-170-CS-08")) {
            var item = caseById(id);
            var result = learner.validateAttemptLedger(rows(item));
            assertEquals(AttemptLedgerReport.Status.valueOf(item.path("expected").asText()), result.status());
            assertEquals(item.path("accepted_rows").asInt(), result.acceptedRows());
        }
    }

    @Test void detectsDuplicateAndOutOfOrderAttempts() throws Exception {
        for (String id : List.of("CAP-170-CS-02", "CAP-170-CS-03", "CAP-170-CS-04")) {
            var item = caseById(id);
            assertEquals(AttemptLedgerReport.Status.valueOf(item.path("expected").asText()),
                    learner.validateAttemptLedger(rows(item)).status());
        }
    }

    @Test void rejectsNullOrNegativeIdentityDomain() throws Exception {
        for (String id : List.of("CAP-170-CS-05", "CAP-170-CS-06")) {
            var item = caseById(id);
            assertEquals(AttemptLedgerReport.Status.INVALID_DOMAIN,
                    learner.validateAttemptLedger(rows(item)).status());
        }
    }

    private JsonNode caseById(String id) throws Exception {
        JsonNode cases = mapper.readTree(Files.readString(Path.of("fixtures/coverstory/CAP-170/synthetic-v1.json"))).path("cases");
        for (JsonNode item : cases) if (id.equals(item.path("id").asText())) return item;
        throw new AssertionError("missing case " + id);
    }

    private List<ReconciliationAttempt> rows(JsonNode item) {
        List<ReconciliationAttempt> result = new ArrayList<>();
        for (JsonNode row : item.path("rows")) {
            result.add(new ReconciliationAttempt(row.path("owner").asText(null), row.path("event").asText(null),
                    row.has("epoch") && !row.path("epoch").isNull() ? row.path("epoch").longValue() : null,
                    row.has("attempt") && !row.path("attempt").isNull() ? row.path("attempt").intValue() : null,
                    row.path("terminal").asBoolean()));
        }
        return result;
    }
}
