package assignments.renewal;

import assignments.insurance.PolicyRenewalReadStore;
import assignments.local.LocalApplicationComposition;
import assignments.sprint6.Sprint6LocalApplication;
import assignments.sprint6.Sprint6LocalConfiguration;
import assignments.sprint6.Sprint6PreviewCapacityConfiguration;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class PreviewRuntimeWiringAcceptanceTest {
    @Test
    void boundedEntryAndPackagedHarnessExcludeLaterRuntimeTodos() throws Exception {
        String entry = Files.readString(Path.of("src/main/java/assignments/sprint6/Sprint6LocalApplication.java"));
        String configuration = Files.readString(Path.of("src/main/java/assignments/sprint6/Sprint6LocalConfiguration.java"));
        String localRuntime = Files.readString(Path.of("src/main/java/assignments/local/LocalRuntimeConfiguration.java"));
        String sharedStore = Files.readString(Path.of("src/main/java/assignments/local/LocalPolicyRenewalReadStoreConfiguration.java"));
        String localApplication = Files.readString(Path.of("src/main/java/assignments/local/LocalCoverStoryApplication.java"));
        String packagedProcess = Files.readString(Path.of("src/test/java/assignments/local/Sprint6PackagedRuntimeProcess.java"));
        String day83 = Files.readString(Path.of("src/test/java/assignments/local/Day83PackagedLockBrowserAcceptanceTest.java"));
        String pom = Files.readString(Path.of("pom.xml"));

        assertTrue(entry.contains("@Import(Sprint6LocalConfiguration.class)"));
        assertTrue(configuration.contains("LocalApplicationComposition.compose(sprint6Clock, dataSource)"));
        assertTrue(configuration.contains("LocalPolicyRenewalReadStoreConfiguration.class"));
        assertTrue(localApplication.contains("scanBasePackages = \"assignments.local\""));
        assertFalse(localRuntime.contains("LocalPolicyRenewalReadStoreConfiguration.class"),
                "the application package scan discovers the one shared bean instead of importing a duplicate");
        assertFalse(configuration.contains("LEARNER TODO Day 79"));
        assertFalse(localRuntime.contains("LEARNER TODO Day 79"));
        assertTrue(sharedStore.contains("LEARNER TODO Day 79"));
        assertTrue(sharedStore.contains("BoundedPolicyRenewalReadStore"));
        assertEquals(1, occurrences(sharedStore, "LEARNER TODO Day 79"),
                "both local runtime profiles must use one Day 79 learner seam");
        assertFalse(configuration.contains("PreviewReadBudgetPolicy"));
        assertFalse(configuration.contains("TimedPolicyRenewalReadStore"));
        assertFalse(configuration.contains("LocalRuntimeConfiguration.class"));
        assertTrue(packagedProcess.contains("capstone-starter-1.0-SNAPSHOT-sprint6.jar"));
        assertTrue(packagedProcess.contains("--spring.profiles.active=local,sprint6-local"));
        assertTrue(packagedProcess.contains("verifyArtifactReceipt(jar, evidence)"));
        assertTrue(day83.contains("Sprint6PackagedRuntimeProcess.start(postgres, CLOCK)"));
        assertFalse(day83.contains("LocalPackagedRuntimeProcess.start"));
        assertTrue(pom.contains("<id>sprint6-local</id>"));
        assertTrue(pom.contains("assignments.sprint6.Sprint6LocalApplication"));
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        for (int at = 0; (at = text.indexOf(needle, at)) >= 0; at += needle.length()) count++;
        return count;
    }

    @Test
    void actualSprint6CompositionUsesOneBoundedDecoratorAroundTheExistingStore() throws Exception {
        try (var context = context(Map.of(
                "coverstory.preview.permit-capacity", "2",
                "spring.datasource.hikari.maximum-pool-size", "4",
                "spring.datasource.hikari.connection-timeout", "1000"), Sprint6LocalConfiguration.class)) {
            context.refresh();
            PolicyRenewalReadStore first = context.getBean(PolicyRenewalReadStore.class);
            assertSame(first, context.getBean(PolicyRenewalReadStore.class));
            assertInstanceOf(BoundedPolicyRenewalReadStore.class, first);
            Field delegate = BoundedPolicyRenewalReadStore.class.getDeclaredField("delegate");
            delegate.setAccessible(true);
            LocalApplicationComposition.Components components = context.getBean(LocalApplicationComposition.Components.class);
            assertSame(components.renewalReadStore(), delegate.get(first));
            assertEquals(1000, context.getBean(PreviewCapacityPolicy.class).connectionTimeoutMillis());
        }
    }

    @Test
    void invalidCapacityFailsAtTheCapacityValidatorNotAnUnrelatedStartupBean() {
        try (var context = context(Map.of(
                "coverstory.preview.permit-capacity", "5",
                "spring.datasource.hikari.maximum-pool-size", "4",
                "spring.datasource.hikari.connection-timeout", "1000"), Sprint6PreviewCapacityConfiguration.class)) {
            BeanCreationException failure = assertThrows(BeanCreationException.class, context::refresh);
            Throwable root = failure;
            while (root.getCause() != null) root = root.getCause();
            assertInstanceOf(IllegalArgumentException.class, root,
                    "the intended validation error must be the root cause, not a future TODO or any startup failure");
        }
    }

    private static AnnotationConfigApplicationContext context(Map<String, Object> properties, Class<?> configuration) {
        var context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles("local", "sprint6-local");
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("sprint6-test", properties));
        context.registerBean("testDataSource", DataSource.class, () -> new DriverManagerDataSource());
        context.registerBean("testMeterRegistry", MeterRegistry.class, () -> new SimpleMeterRegistry());
        context.register(configuration);
        return context;
    }
}
