package assignments.ops;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class Day127To140InfrastructureTest {
    @Test void realPostgres186ClientToolsAreAvailableAndPinned() throws Exception {
        assertTrue(Postgres18ToolHarness.toolVersion("pg_dump").contains("18.6"));
        assertTrue(Postgres18ToolHarness.toolVersion("pg_restore").contains("18.6"));
    }

    @Test void offlineAwsDesignIsNotADeployableTemplate() throws Exception {
        Path design = Path.of("docs/reboot/continuation-127-140/aws-lab-design.json");
        assertTrue(Files.isRegularFile(design));
        String json = Files.readString(design);
        assertTrue(json.contains("\"cloud_execution_authorized\": false"));
        assertTrue(json.contains("\"status\": \"offline_plan_only\""));
        assertFalse(json.contains("AccessKey"));
        assertFalse(json.contains("SecretAccessKey"));
        assertFalse(json.contains("CloudFormation"), "offline plan must not smuggle in deployable infrastructure");
    }

    @Test void day136ToolWatchdogCleansOnlyItsContainerAndPreservesSentinel() throws Exception {
        Postgres18ToolHarness.timeoutCleanupPreservesUnrelatedSentinel();
    }
}
