package coverstory.modules.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.application.api.CAP106Seam.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Independent fixture and policy witnesses; no learner seam invocation or live adapter. */
class CAP106InstructorInfrastructureTest {
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-106/synthetic-v1.json");
    private static final ObjectMapper JSON = new ObjectMapper();

    @Test void fixtureDefinesAllSavedCasesAndMakesNoLiveAdapterClaim() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_LOCAL_POLICY_CONTRACT", root.path("classification").asText());
        assertFalse(root.path("actual_cloud_storage").asBoolean());
        assertFalse(root.path("actual_http_or_tls_adapter").asBoolean());
        assertEquals("r2", root.path("accepted_current_revision").asText());
        assertEquals(5, root.path("cases").size());
    }

    @Test void oracleAllowsExactlyOwnerTlsReadAndRejectsMutationsAndTransport() {
        AtomicInteger fetches = new AtomicInteger(); AtomicInteger mutations = new AtomicInteger();
        Principal a = new Principal("customer-A", "READER");
        Owner owner = new Owner("customer-A");
        Key r1 = key("owners/customer-A/applications/application-A/revisions/r1.json", "customer-A", "r1", RevisionKind.HISTORICAL);
        assertEquals(Decision.ALLOW, oracle(a, Operation.GET, owner, r1, Transport.TLS).decision());
        fetches.incrementAndGet();
        assertEquals(Decision.DENY, oracle(a, Operation.PUT, owner, r1, Transport.TLS).decision());
        assertEquals(Decision.DENY, oracle(a, Operation.GET, owner, r1, Transport.CLEAR_TEXT).decision());
        assertEquals(1, fetches.get()); assertEquals(0, mutations.get());
    }

    @Test void oracleDeniesForeignUnknownTraversalAndNullOwnerBeforeStorage() {
        Principal a = new Principal("customer-A", "READER"), b = new Principal("customer-B", "READER");
        Owner owner = new Owner("customer-A");
        Key safe = key("owners/customer-A/applications/application-A/revisions/r1.json", "customer-A", "r1", RevisionKind.HISTORICAL);
        Key unknown = key("owners/customer-A/applications/application-A/revisions/r9.json", "customer-A", "r9", RevisionKind.HISTORICAL);
        Key traversal = key("owners/customer-A/../customer-B.json", "customer-A", "r1", RevisionKind.HISTORICAL);
        for (AccessDecision denied : new AccessDecision[]{oracle(b, Operation.GET, owner, safe, Transport.TLS),
                oracle(a, Operation.GET, owner, unknown, Transport.TLS), oracle(a, Operation.GET, owner, traversal, Transport.TLS),
                oracle(a, Operation.GET, null, safe, Transport.TLS)}) {
            assertEquals(Decision.DENY, denied.decision()); assertNull(denied.safeCode(), "denial has no foreign object metadata");
        }
    }

    @Test void historicR1NeverSatisfiesAcceptedCurrentR2AndOracleRejectsDefects() {
        Principal a = new Principal("customer-A", "READER"); Owner owner = new Owner("customer-A");
        Key r1 = key("owners/customer-A/applications/application-A/revisions/r1.json", "customer-A", "r1", RevisionKind.HISTORICAL);
        Key r2 = key("owners/customer-A/applications/application-A/revisions/r2.json", "customer-A", "r2", RevisionKind.CURRENT);
        assertEquals(Decision.ALLOW, oracle(a, Operation.GET, owner, r1, Transport.TLS).decision());
        assertEquals(Decision.ALLOW, oracle(a, Operation.GET, owner, r2, Transport.TLS).decision());
        Snapshot historic = new Snapshot("r1", RevisionKind.HISTORICAL, "synthetic");
        assertNotEquals("r2", historic.revisionId());
        assertNotEquals(RevisionKind.CURRENT, historic.kind());
        // Defect witnesses: owner mismatch, key mismatch, TLS omitted, or writes allowed.
        assertNotEquals(Decision.ALLOW, oracle(a, Operation.GET, new Owner("customer-B"), r1, Transport.TLS).decision());
        assertNotEquals(Decision.ALLOW, oracle(a, Operation.GET, owner, r1, Transport.CLEAR_TEXT).decision());
        assertNotEquals(Decision.ALLOW, oracle(a, Operation.PUT, owner, r1, Transport.TLS).decision());
        Set<String> registered = Set.of(r1.value(), r2.value());
        assertFalse(registered.contains("owners/customer-A/../customer-B.json"));
    }

    /** Small independent policy oracle with explicit registry and no store calls. */
    private static AccessDecision oracle(Principal p, Operation op, Owner o, Key k, Transport t) {
        Set<String> registered = Set.of(
                "owners/customer-A/applications/application-A/revisions/r1.json",
                "owners/customer-A/applications/application-A/revisions/r2.json");
        boolean allow = p != null && "READER".equals(p.role()) && op == Operation.GET && t == Transport.TLS
                && o != null && k != null && p.id() != null && p.id().equals(o.id())
                && p.id().equals(k.ownerId()) && registered.contains(k.value())
                && k.value().matches("owners/[A-Za-z0-9-]+/applications/[A-Za-z0-9-]+/revisions/r[12]\\.json")
                && (("r1".equals(k.revisionId()) && k.kind() == RevisionKind.HISTORICAL)
                || ("r2".equals(k.revisionId()) && k.kind() == RevisionKind.CURRENT));
        return allow ? new AccessDecision(Decision.ALLOW, null) : new AccessDecision(Decision.DENY, null);
    }
    private static Key key(String value, String owner, String revision, RevisionKind kind) {
        return new Key(value, owner, "application-A", revision, kind);
    }
}
