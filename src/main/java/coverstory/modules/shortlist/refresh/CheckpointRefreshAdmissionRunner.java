package coverstory.modules.shortlist.refresh;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** CAP-645 learner seam. Admission is derived from evidence rows, never a caller-supplied PASS label. */
public final class CheckpointRefreshAdmissionRunner {
    public enum Layer { UI, API, DATABASE }
    public record Observation(String eventId, Layer layer, String candidateSha256, String ownerId, String observedValue) { }
    public record ExpectedEvent(String eventId, Map<Layer,String> values) {
        public ExpectedEvent { values=Map.copyOf(values); }
    }
    public record CandidateEvidence(String candidateSha256, String ownerId, Path evidenceFile, String evidenceSha256) { }
    public record Admission(String result, List<String> violations, String evidenceSha256,
                            int checkedLayerObservations, String candidateSha256) {
        public Admission { violations=List.copyOf(violations); }
    }

    public Admission evaluate(CandidateEvidence evidence, List<ExpectedEvent> schedule) {
        // TODO(CAP-645): verify evidence integrity, candidate/owner binding, one
        // observation per UI/API/DATABASE layer and exact scheduled outcomes.
        throw new UnsupportedOperationException("TODO(CAP-645): derive admission from candidate-bound cross-layer evidence");
    }

    static String sha256(Path path) throws IOException {
        try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))); }
        catch(NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
}
