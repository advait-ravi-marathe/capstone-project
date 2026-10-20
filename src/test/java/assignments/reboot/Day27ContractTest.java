package assignments.reboot;

import assignments.insurance.QuoteSelectionInput;
import assignments.insurance.QuoteSelectionKey;
import assignments.insurance.QuoteSelectionPolicy;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Saved boundary reproducer for a cross-journey composite-identity regression. */
@Tag("learnerAcceptance")
class Day27ContractTest {
    @Test
    void savedReproducerKeepsSameVersionUnderDifferentJourneysDistinct() throws Exception {
        JsonNode fixture;
        try (InputStream in = getClass().getResourceAsStream("/fixtures/reboot/day-27/composite-identity-regression.json")) {
            if (in == null) throw new IllegalStateException("Missing Day 27 saved reproducer");
            fixture = new ObjectMapper().readTree(in);
        }
        List<QuoteSelectionInput> input = new ArrayList<>();
        fixture.get("selections").forEach(node -> input.add(new QuoteSelectionInput(
                node.get("journeyId").asString(), node.get("quoteVersionId").asString())));
        List<QuoteSelectionKey> actual = new QuoteSelectionPolicy().uniqueSelections(input);
        List<QuoteSelectionKey> expected = new ArrayList<>();
        fixture.get("expected").forEach(node -> expected.add(new QuoteSelectionKey(
                node.get("journeyId").asString(), node.get("quoteVersionId").asString())));
        assertEquals(expected, actual);
    }
}
