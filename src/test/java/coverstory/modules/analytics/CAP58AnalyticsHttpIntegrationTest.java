package coverstory.modules.analytics;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import coverstory.modules.analytics.api.AnalyticsIngestionController;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/** Actual Spring MVC request mapping with test-only validator/receipt reference adapter. */
class CAP58AnalyticsHttpIntegrationTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-58/synthetic-v1.json");

    @Test void authenticatedIngressReturns202DuplicateAndSafeChangedPayloadConflict() throws Exception {
        CAP58InstructorIngressHarness adapter = new CAP58InstructorIngressHarness(Set.of("owner-A:journey-A"));
        AtomicInteger calls = new AtomicInteger();
        MockMvc mvc = mvc((body, owner) -> { calls.incrementAndGet(); try { var x=adapter.ingest(body,true,true,owner,null); return new AnalyticsIngestionPort.Receipt(x.status(),x.code(),x.acceptedCount()); } catch (Exception e) { throw new IllegalStateException(e); } });
        byte[] event = batch(fixture().path("events").path("page_view"));
        MvcResult accepted=mvc.perform(authenticated(post("/api/v1/analytics/events").contentType("application/json").content(event))).andReturn();
        assertEquals(202,accepted.getResponse().getStatus());assertEquals("RECEIVED",JSON.readTree(accepted.getResponse().getContentAsByteArray()).path("code").asText());
        MvcResult replay=mvc.perform(authenticated(post("/api/v1/analytics/events").contentType("application/json").content(event))).andReturn();
        assertEquals(202,replay.getResponse().getStatus());assertEquals("DUPLICATE",JSON.readTree(replay.getResponse().getContentAsByteArray()).path("code").asText());
        JsonNode changed=fixture().path("events").path("page_view").deepCopy();((ObjectNode)changed).put("pageId","compare"); byte[] changedBody=batch(changed);
        MvcResult conflict=mvc.perform(authenticated(post("/api/v1/analytics/events").contentType("application/json").content(changedBody))).andReturn();
        assertEquals(409,conflict.getResponse().getStatus());assertEquals("EVENT_ID_CONFLICT",JSON.readTree(conflict.getResponse().getContentAsByteArray()).path("code").asText());
        assertEquals(3,calls.get());
    }

    @Test void anonymousOversizedAndSensitiveRequestsFailBeforeAnyPayloadReceipt() throws Exception {
        AtomicInteger calls=new AtomicInteger();MockMvc mvc=mvc((body,owner)->{calls.incrementAndGet();return new AnalyticsIngestionPort.Receipt(202,"RECEIVED",1);});
        byte[] event=batch(fixture().path("events").path("page_view"));
        MvcResult anonymous=mvc.perform(post("/api/v1/analytics/events").contentType("application/json").content(event)).andReturn();
        assertEquals(401,anonymous.getResponse().getStatus());
        MvcResult oversized=mvc.perform(authenticated(post("/api/v1/analytics/events").contentType("application/json").content(new byte[8193]))).andReturn();
        assertEquals(413,oversized.getResponse().getStatus());
        byte[] sensitive=batch(fixture().path("events").path("sensitive_extras"));
        MvcResult invalid=mvc((body,owner)->{try{var x=new CAP58InstructorIngressHarness(Set.of()).ingest(body,true,true,owner,null);return new AnalyticsIngestionPort.Receipt(x.status(),x.code(),x.acceptedCount());}catch(Exception e){throw new IllegalStateException(e);}})
                .perform(authenticated(post("/api/v1/analytics/events").contentType("application/json").content(sensitive))).andReturn();
        assertEquals(400,invalid.getResponse().getStatus());assertFalse(invalid.getResponse().getContentAsString().contains("fixture-secret"));
        assertEquals(0,calls.get());
    }

    private static MockMvc mvc(AnalyticsIngestionPort port) { return standaloneSetup(new AnalyticsIngestionController(port)).build(); }
    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticated(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) {
        return request.with(r->{r.setUserPrincipal((Principal)()->"owner-A");return r;});
    }
    private static JsonNode fixture() throws Exception { return JSON.readTree(Files.readString(FIXTURE)); }
    private static byte[] batch(JsonNode event) throws Exception {var root=JSON.createObjectNode();root.putArray("events").add(event);return JSON.writeValueAsBytes(root);}
}
