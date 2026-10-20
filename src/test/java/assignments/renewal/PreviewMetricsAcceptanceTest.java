package assignments.renewal;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry; import org.junit.jupiter.api.Tag; import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
@Tag("learnerAcceptance") class PreviewMetricsAcceptanceTest {
 @Test void onlyFiniteOutcomesCreateMetersRegardlessOfRequestCount() {
  var registry=new SimpleMeterRegistry();var metrics=new PreviewMetrics(registry);
  for(int i=0;i<1000;i++)metrics.record(PreviewMetrics.Outcome.SUCCESS,1_000_000L);
  assertEquals(2,registry.getMeters().size());assertEquals(1000,registry.counter("coverstory.preview.requests","outcome","SUCCESS").count(),0.0);
 }
}
