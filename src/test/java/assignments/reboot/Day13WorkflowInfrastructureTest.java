package assignments.reboot;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks CI exists, runs the ordinary commands, and does not suppress failures. */
class Day13WorkflowInfrastructureTest {
    @Test
    void workflowRunsBuildAndTestCommandsWithFailurePropagation() throws Exception {
        String workflow = Files.readString(Path.of(".github/workflows/coverstory-starter.yml"));
        assertTrue(workflow.contains("pull_request:"));
        assertTrue(workflow.contains("./mvnw -B -ntp test"));
        assertTrue(workflow.contains("npm ci"));
        assertTrue(workflow.contains("npm run build"));
        assertTrue(workflow.contains("npm test"));
        assertTrue(workflow.contains("npm audit --audit-level=low"));
        assertFalse(workflow.contains("continue-on-error:"));
        assertFalse(workflow.contains("|| true"));
    }
}
