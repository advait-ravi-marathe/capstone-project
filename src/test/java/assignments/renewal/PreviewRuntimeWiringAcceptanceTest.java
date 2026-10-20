package assignments.renewal;
import org.junit.jupiter.api.Tag; import org.junit.jupiter.api.Test; import java.nio.file.*;
import assignments.insurance.PolicyRenewalReadStore;
import assignments.insurance.PolicyRenewalFacts;
import assignments.local.LocalApplicationComposition;
import assignments.local.LocalRuntimeConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
@Tag("learnerAcceptance") class PreviewRuntimeWiringAcceptanceTest {
 @Test void localAppUsesFinitePoolAndSingletonBoundedDecorator() throws Exception {
  String properties=Files.readString(Path.of("src/main/resources/application-local.properties"));
  assertTrue(properties.lines().anyMatch(s->s.trim().equals("spring.datasource.hikari.maximum-pool-size=4")));
  assertTrue(properties.lines().anyMatch(s->s.trim().equals("spring.datasource.hikari.connection-timeout=1000")));
  String wiring=Files.readString(Path.of("src/main/java/assignments/local/LocalRuntimeConfiguration.java"));
  assertTrue(wiring.contains("LEARNER TODO Day 79"));
 }

 @Test void actualLocalCompositionUsesOneSingletonDecoratorAroundTheExistingStore() throws Exception {
  try (var context = new AnnotationConfigApplicationContext()) {
   context.getEnvironment().setActiveProfiles("local");
   context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", Map.of(
           "coverstory.preview.permit-capacity", "2",
           "spring.datasource.hikari.maximum-pool-size", "4",
           "spring.datasource.hikari.connection-timeout", "1000")));
   context.registerBean("testDataSource", DataSource.class, () -> new DriverManagerDataSource());
   context.register(LocalRuntimeConfiguration.class);
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
}
