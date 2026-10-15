package capstone.coverstory.application.internal;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CAP117InstructorOracleTest {
    private static final String H="f96df155cd21849d54e42885cdb9432a9182fcc894f1b4ce794df7eae828d91c";
    private static final Instant CHECK=Instant.parse("2027-01-04T12:00:00Z"), EXPIRY=Instant.parse("2027-01-20T00:00:00Z");
    private final CAP117ReferenceManifestVerifier verifier=new CAP117ReferenceManifestVerifier();
    private final ObjectMapper mapper=new ObjectMapper();
    private JsonNode fixture() throws Exception {return mapper.readTree(Files.readString(Path.of("docs/coverstory/fixtures/cap-117.json")));}
    private List<CAP117ReferenceManifestVerifier.Fact> facts() throws Exception {
        List<CAP117ReferenceManifestVerifier.Fact> facts=new ArrayList<>();
        for(JsonNode n:fixture().path("artifacts")) facts.add(new CAP117ReferenceManifestVerifier.Fact(n.path("artifact_id").asText(),n.path("artifact_type").asText(),n.path("sha256").asText(),n.path("revision").asText(),Files.isRegularFile(Path.of(n.path("path").asText()))));
        return facts;
    }
    private CAP117ReferenceManifestVerifier.Input input(String accepted,String current,Instant checked,List<CAP117ReferenceManifestVerifier.Fact> facts,String narrative){return new CAP117ReferenceManifestVerifier.Input("o2",accepted,current,checked,EXPIRY,facts,narrative);}
    @Test void frozenEvidenceHashesMatchTheActualPriorDayArtifacts() throws Exception {
        for(JsonNode n:fixture().path("artifacts")) {byte[] bytes=Files.readAllBytes(Path.of(n.path("path").asText()));String actual=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));assertEquals(n.path("sha256").asText(),actual);}
        assertEquals(H,fixture().path("accepted_offer_content_sha256").asText());
        assertEquals("VERIFIED_REVIEW_REQUIRED",verifier.verify(input(H,H,CHECK,facts(),"Synthetic evidence only.")).status());
    }
    @Test void currentCompleteBundleRemainsReviewRequiredAndDoesNotClaimIssuance() throws Exception {var r=verifier.verify(input(H,H,CHECK,facts(),"Synthetic evidence only."));assertEquals("VERIFIED_REVIEW_REQUIRED",r.status());assertEquals("RECONCILE_BALANCE",r.nextStep());assertTrue(r.issues().isEmpty());}
    @Test void expiryAndChangedRevisionBlockBundle() throws Exception {assertTrue(verifier.verify(input(H,H,EXPIRY,facts(),"Synthetic evidence only.")).issues().contains("OFFER_EXPIRED"));assertTrue(verifier.verify(input("0".repeat(64),H,CHECK,facts(),"Synthetic evidence only.")).issues().contains("ACCEPTED_REVISION_MISMATCH"));}
    @Test void artifactIntegrityAndPresenceAreRequired() throws Exception {var changed=facts().stream().map(f->f.id().equals("CAP-115-result")?new CAP117ReferenceManifestVerifier.Fact(f.id(),f.type(),"bad",f.revision(),true):f).toList();assertTrue(verifier.verify(input(H,H,CHECK,changed,"Synthetic evidence only.")).issues().stream().anyMatch(i->i.startsWith("ARTIFACT_HASH_MISMATCH")));var missing=facts().stream().map(f->f.id().equals("CAP-116-result")?new CAP117ReferenceManifestVerifier.Fact(f.id(),f.type(),f.expectedSha256(),f.revision(),false):f).toList();assertTrue(verifier.verify(input(H,H,CHECK,missing,"Synthetic evidence only.")).issues().stream().anyMatch(i->i.startsWith("ARTIFACT_MISSING")));}
    @Test void unsupportedInsurerApprovalClaimInvalidatesBundle() throws Exception {var r=verifier.verify(input(H,H,CHECK,facts(),"Insurer approved and issued active cover."));assertEquals("INVALID",r.status());assertTrue(r.issues().contains("UNSUPPORTED_INSURER_APPROVAL_CLAIM"));}
}
