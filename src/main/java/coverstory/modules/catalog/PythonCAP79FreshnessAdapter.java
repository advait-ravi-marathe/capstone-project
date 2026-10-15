package coverstory.modules.catalog;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.api.CatalogFreshnessPort;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Local instructor bridge which calls the actual Python learner seam with a fixed synthetic fixture. */
public final class PythonCAP79FreshnessAdapter implements CatalogFreshnessPort {
    private final String python;
    private final Path repositoryRoot;
    private final Path script;
    private final ObjectMapper json;

    public PythonCAP79FreshnessAdapter(String python, Path repositoryRoot, Path script, ObjectMapper json) {
        this.python=python; this.repositoryRoot=repositoryRoot; this.script=script; this.json=json;
    }

    @Override public Map<String,Object> currentResult() {
        try {
            Process process = new ProcessBuilder(python, script.toString()).directory(repositoryRoot.toFile())
                    .redirectErrorStream(true).start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) { process.destroyForcibly(); throw new IllegalStateException("freshness adapter timed out"); }
            byte[] bytes = process.getInputStream().readNBytes(65537);
            if (bytes.length > 65536) throw new IllegalStateException("freshness adapter response exceeded limit");
            String output = new String(bytes, StandardCharsets.UTF_8).trim();
            if (process.exitValue() != 0) {
                Map<String,Object> issue = json.readValue(output, new TypeReference<>() {});
                if ("LEARNER_TODO".equals(issue.get("code")))
                    throw new UnsupportedOperationException("CAP-79 learner freshness seam is not implemented");
                throw new IllegalStateException("freshness adapter failed");
            }
            return json.readValue(output, new TypeReference<>() {});
        } catch (UnsupportedOperationException expected) { throw expected; }
        catch (Exception unavailable) { throw new IllegalStateException("freshness adapter unavailable"); }
    }
}
