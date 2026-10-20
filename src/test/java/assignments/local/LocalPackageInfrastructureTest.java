package assignments.local;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalPackageInfrastructureTest {
    @Test
    void day67PackageIncludesNormalUiAndNoTestRoutes() throws Exception {
        Path packageScript = Path.of("scripts/package-local-demo.sh");
        Path launchScript = Path.of("scripts/local-demo.sh");
        assertTrue(Files.isExecutable(packageScript));
        assertTrue(Files.isExecutable(launchScript));
        assertTrue(Files.readString(Path.of("pom.xml")).contains("<targetPath>static</targetPath>"));
        assertTrue(Files.isRegularFile(Path.of("frontend/local/index.html")));
        String entry = Files.readString(Path.of("frontend/src/local-main.tsx"));
        assertFalse(entry.contains("__e2e"));
        assertFalse(entry.contains("/test/"));
        String docker = Files.readString(Path.of("compose.local.yaml"));
        assertTrue(docker.contains("image: postgres:18.6"));
        assertTrue(docker.contains("127.0.0.1::5432"));
        assertTrue(docker.contains("/var/lib/postgresql"));
        assertFalse(docker.contains("volumes:"));
        assertTrue(Files.readString(launchScript).contains("down --volumes --remove-orphans"));
    }
}
