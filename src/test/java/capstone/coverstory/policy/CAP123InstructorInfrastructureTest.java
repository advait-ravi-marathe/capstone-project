package capstone.coverstory.policy;

import capstone.coverstory.policy.internal.PolicyHelpDemoGate;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CAP123InstructorInfrastructureTest {
    private JsonNode fixture() throws Exception {
        return new ObjectMapper().readTree(Files.readString(Path.of("docs/coverstory/fixtures/cap-123.json")));
    }
    @Test void fixtureHashAndExactOneOwnedPolicyTimelineCitation() throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of("docs/coverstory/fixtures/cap-123.json"));
        assertEquals("4f0faf16a133cc4e8377e4d48aa6155903bad9dcc9f304ab2eaaa4ba5d3af5da", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        JsonNode f = fixture(); var scenario = f.path("scenarios").get(0);
        var result = CAP123ReferenceOracle.answer(principal(scenario.path("principal")), evidence(scenario.path("evidence")));
        assertEquals(PolicyHelpDemoGate.State.ACTIVE_COVER, result.state());
        assertEquals(1, result.policies().size()); assertEquals("policy-sim-123", result.policies().get(0).id());
        assertEquals(List.of("ISSUANCE_CONFIRMED", "COVER_EFFECTIVE"), result.timeline().stream().map(PolicyHelpDemoGate.Event::label).toList());
        assertEquals("policy-terms-sim-v3", result.citations().get(0).version());
        assertTrue(result.wording().contains("Simulated"));
    }
    @Test void acceptedOfferAndPaymentWithoutIssuanceStayPendingAndSayNoCover() throws Exception {
        JsonNode scenario = fixture().path("scenarios").get(1);
        var result = CAP123ReferenceOracle.answer(principal(scenario.path("principal")), evidence(scenario.path("evidence")));
        assertEquals(PolicyHelpDemoGate.State.ISSUANCE_PENDING, result.state());
        assertTrue(result.wording().contains("does not mean cover is active"));
        assertTrue(result.policies().isEmpty()); assertTrue(result.timeline().isEmpty());
    }
    @Test void unsupportedOrForeignOwnerAbstainsOrDeniesWithoutWrites() throws Exception {
        JsonNode f = fixture();
        for (JsonNode s : f.path("scenarios").get(2).path("cases")) {
            var before = s.path("write_count").asInt();
            var result = CAP123ReferenceOracle.answer(principal(s.path("principal")), evidence(s.path("evidence")));
            assertEquals(s.path("expected_state").asText(), result.state().name());
            assertEquals(before, s.path("write_count").asInt());
            assertTrue(result.policies().isEmpty()); assertTrue(result.citations().isEmpty());
        }
    }
    private static PolicyHelpDemoGate.Principal principal(JsonNode n) {
        return new PolicyHelpDemoGate.Principal(n.path("id").asText(),
                PolicyHelpDemoGate.PrincipalKind.valueOf(n.path("kind").asText()),
                n.path("owner_id").isNull() ? null : n.path("owner_id").asText(),
                n.path("assigned_case_id").isNull() ? null : n.path("assigned_case_id").asText());
    }
    private static PolicyHelpDemoGate.Evidence evidence(JsonNode n) {
        var policies = new ArrayList<PolicyHelpDemoGate.Policy>();
        for (JsonNode p : n.path("policies")) policies.add(new PolicyHelpDemoGate.Policy(p.path("id").asText(), p.path("owner_id").asText(), p.path("application_id").asText(),
                nullable(p.path("issuance_reference")), nullable(p.path("effective_date")), nullable(p.path("terms_version"))));
        var events = new ArrayList<PolicyHelpDemoGate.Event>();
        for (JsonNode e : n.path("events")) events.add(new PolicyHelpDemoGate.Event(e.path("id").asText(), e.path("policy_id").asText(), e.path("occurred_at").asText(), e.path("label").asText()));
        JsonNode c = n.path("citation"); var citation = c.isMissingNode() || c.isNull() ? null : new PolicyHelpDemoGate.Citation(c.path("source_id").asText(), c.path("version").asText(), c.path("excerpt").asText());
        return new PolicyHelpDemoGate.Evidence(n.path("owner_id").asText(), n.path("question").asText(), n.path("supported_question").asBoolean(), n.path("offer_accepted").asBoolean(),
                n.path("payment_confirmed").asBoolean(), n.path("effective_cover_evidence").asBoolean(), List.copyOf(policies), List.copyOf(events), citation);
    }
    private static String nullable(JsonNode n) { return n == null || n.isNull() ? null : n.asText(); }
}
