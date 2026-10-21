package assignments.ops;

import java.util.Map;

/** Learner policy for identifying the already packaged local artifact and migration set. */
public final class ReleaseManifestPolicy {
    public record ReleaseManifest(String sourceSha, String jarSha256, String javaVersion,
                                  String postgresServerVersion, String pgDumpVersion,
                                  Map<String, String> migrationSha256, boolean containsSecrets) {}

    public void validate(ReleaseManifest manifest, String actualJarSha256, Map<String, String> actualMigrations) {
        // LEARNER TODO Day 128: reject incomplete, mismatched, or secret-bearing release identity.
        throw new UnsupportedOperationException("Day 128 learner TODO: verify release identity");
    }
}
