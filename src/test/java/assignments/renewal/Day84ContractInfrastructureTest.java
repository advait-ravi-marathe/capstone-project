package assignments.renewal;
import org.junit.jupiter.api.Test; import java.nio.file.*; import static org.junit.jupiter.api.Assertions.*;
class Day84ContractInfrastructureTest { @Test void assessmentKeepsFeatureScopeAndPrerequisitesExplicit() throws Exception {
 String m=Files.readString(Path.of("docs/reboot/continuation-71-84/manifest.json"));assertTrue(m.contains("actual_attempt_dates_supplied"));assertTrue(m.contains("ACCESS EXCLUSIVE"));assertTrue(m.contains("Preserve Day48 SQL ownership"));
 }}
