package assignments.renewal;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class Day71BaselineAcceptanceTest {
    @Test void recordsTheBoundedSynchronousBaselineWithoutAddingInfrastructure() throws Exception {
        String properties = Files.readString(Path.of("src/main/resources/application-local.properties"));
        assertTrue(properties.contains("spring.datasource.hikari.maximum-pool-size=4"));
        assertTrue(properties.contains("spring.datasource.hikari.connection-timeout=1000"));
        String store = Files.readString(Path.of("src/main/java/assignments/insurance/JdbcPolicyRenewalReadStore.java"));
        assertTrue(store.contains("class JdbcPolicyRenewalReadStore"), "the existing owner-scoped adapter remains the extension point");
        assertFalse(Files.readString(Path.of("src/main/java/assignments/renewal/BoundedPolicyRenewalReadStore.java"))
                .contains("CompletableFuture"));
    }
}
