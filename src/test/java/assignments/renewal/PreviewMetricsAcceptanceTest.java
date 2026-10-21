package assignments.renewal;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry; import org.junit.jupiter.api.Tag; import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
@Tag("learnerAcceptance") class PreviewMetricsAcceptanceTest {
 @Test void everyFiniteOutcomeUsesOnlyBoundedTagsRegardlessOfRequestCount() {
  var registry=new SimpleMeterRegistry();var metrics=new PreviewMetrics(registry);
  for(var outcome:PreviewMetrics.Outcome.values())for(int i=0;i<1000;i++)metrics.record(outcome,1_000_000L);
  assertEquals(PreviewMetrics.Outcome.values().length*2,registry.getMeters().size());
  for(var outcome:PreviewMetrics.Outcome.values()) {
   assertEquals(1000,registry.counter("coverstory.preview.requests","outcome",outcome.name()).count(),0.0);
   assertEquals(1000,registry.timer("coverstory.preview.latency","outcome",outcome.name()).count());
  }
  assertTrue(registry.getMeters().stream().flatMap(meter->meter.getId().getTags().stream())
          .allMatch(tag->tag.getKey().equals("outcome")),"no request or policy identifier may become a metric tag");
 }
}
