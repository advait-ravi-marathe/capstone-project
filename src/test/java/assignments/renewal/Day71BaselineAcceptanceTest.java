package assignments.renewal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class Day71BaselineAcceptanceTest {
    @Test void recordsTheBoundedSynchronousBaselineWithoutAddingInfrastructure() throws Exception {
        String properties = Files.readString(Path.of("src/main/resources/application-local.properties"));
        assertTrue(properties.contains("spring.datasource.hikari.maximum-pool-size=4"));
        assertTrue(properties.contains("spring.datasource.hikari.connection-timeout=1000"));
        String store = Files.readString(Path.of("src/main/java/assignments/insurance/JdbcPolicyRenewalReadStore.java"));
        assertTrue(store.contains("LEARNER TODO Day 48"), "Day 48 query ownership stays with the learner");
        assertFalse(Files.readString(Path.of("src/main/java/assignments/renewal/BoundedPolicyRenewalReadStore.java"))
                .contains("CompletableFuture"));
    }
}
