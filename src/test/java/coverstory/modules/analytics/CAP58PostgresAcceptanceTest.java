package coverstory.modules.analytics;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/** Test-only PostgreSQL-backed receipts across the actual Spring HTTP boundary. */
class CAP58PostgresAcceptanceTest {
    private static final ObjectMapper JSON=new ObjectMapper();
    private static final Path FIXTURE=Path.of("fixtures/coverstory/CAP-58/synthetic-v1.json");

    @Test void postgresReceiptsReplayConflictAndKeepOnlyAllowlistedColumns() throws Exception {
        Instant received=Instant.parse("2026-10-15T00:00:00Z");
        try(CAP58InstructorPostgresIngress adapter=new CAP58InstructorPostgresIngress(Clock.fixed(received,ZoneOffset.UTC))) {
            MockMvc mvc=standaloneSetup(new coverstory.modules.analytics.api.AnalyticsIngestionController(adapter)).build();
            JsonNode event=fixture().path("events").path("page_view").deepCopy(); ((ObjectNode)event).put("clientTime","2001-01-01T00:00:00Z");
            byte[] body=batch(event);
            var first=mvc.perform(auth(post("/api/v1/analytics/events").contentType("application/json").content(body))).andReturn();
            assertEquals(202,first.getResponse().getStatus());assertEquals("RECEIVED",JSON.readTree(first.getResponse().getContentAsByteArray()).path("code").asText());
            var replay=mvc.perform(auth(post("/api/v1/analytics/events").contentType("application/json").content(body))).andReturn();
            assertEquals(202,replay.getResponse().getStatus());assertEquals("DUPLICATE",JSON.readTree(replay.getResponse().getContentAsByteArray()).path("code").asText());
            ((ObjectNode)event).put("pageId","compare");
            var conflict=mvc.perform(auth(post("/api/v1/analytics/events").contentType("application/json").content(batch(event)))).andReturn();
            assertEquals(409,conflict.getResponse().getStatus());assertEquals("EVENT_ID_CONFLICT",JSON.readTree(conflict.getResponse().getContentAsByteArray()).path("code").asText());
            assertEquals(1,adapter.count());
            assertEquals(List.of("event_id","owner_hash","payload_hash","session_hash","event_type","page_id","target_id","duration_ms","received_at"),adapter.columns());
            var anonymous=mvc.perform(post("/api/v1/analytics/events").contentType("application/json").content(body)).andReturn();assertEquals(401,anonymous.getResponse().getStatus());
            byte[] over=("{"+" ".repeat(8191)+"}").getBytes(java.nio.charset.StandardCharsets.UTF_8);
            var tooLarge=mvc.perform(auth(post("/api/v1/analytics/events").contentType("application/json").content(over))).andReturn();assertEquals(413,tooLarge.getResponse().getStatus());
            JsonNode sensitive=fixture().path("events").path("sensitive_extras");
            var invalid=mvc.perform(auth(post("/api/v1/analytics/events").contentType("application/json").content(batch(sensitive)))).andReturn();
            assertEquals(400,invalid.getResponse().getStatus());assertFalse(invalid.getResponse().getContentAsString().contains("fixture-secret"));assertEquals(1,adapter.count());
        }
    }

    @Test void utf8BodyAndDurationBoundariesRemainStrict() throws Exception {
        try(CAP58InstructorPostgresIngress adapter=new CAP58InstructorPostgresIngress(Clock.systemUTC())) {
            assertEquals(new AnalyticsIngestionPort.Receipt(413,"BODY_TOO_LARGE",0),adapter.accept("😀".repeat(2049).getBytes(java.nio.charset.StandardCharsets.UTF_8),"owner-A"));
            JsonNode click=fixture().path("events").path("control_click").deepCopy();
            ((ObjectNode)click).put("optionalDurationMs",300000);
            assertEquals(202,adapter.accept(batch(click),"owner-A").status());
            ((ObjectNode)click).put("eventId","58000000-0000-4000-8000-000000000008").put("optionalDurationMs",300001);
            assertEquals(400,adapter.accept(batch(click),"owner-A").status());
            ((ObjectNode)click).put("optionalDurationMs",true);
            assertEquals(400,adapter.accept(batch(click),"owner-A").status());
        }
    }

    @Test void actualHttpAcceptsExact8192BytesAndTenEventsButRejectsElevenAtomically() throws Exception {
        try(CAP58InstructorPostgresIngress adapter=new CAP58InstructorPostgresIngress(Clock.systemUTC())) {
            MockMvc mvc=standaloneSetup(new coverstory.modules.analytics.api.AnalyticsIngestionController(adapter)).build();
            var ten=JSON.createObjectNode().putArray("events");
            for(int i=10;i<20;i++) {
                JsonNode event=fixture().path("events").path("page_view").deepCopy();
                ((ObjectNode)event).put("eventId",String.format("58000000-0000-4000-8000-%012d",i));ten.add(event);
            }
            byte[] compact=JSON.writeValueAsBytes(JSON.createObjectNode().set("events",ten));
            byte[] exact=java.util.Arrays.copyOf(compact,8192);java.util.Arrays.fill(exact,compact.length,exact.length,(byte)' ');
            assertEquals(8192,exact.length);
            var accepted=mvc.perform(auth(post("/api/v1/analytics/events").contentType("application/json").content(exact))).andReturn();
            assertEquals(202,accepted.getResponse().getStatus());assertEquals(10,adapter.count());

            var eleven=JSON.createObjectNode().putArray("events");
            for(int i=20;i<31;i++) {
                JsonNode event=fixture().path("events").path("page_view").deepCopy();
                ((ObjectNode)event).put("eventId",String.format("58000000-0000-4000-8000-%012d",i));eleven.add(event);
            }
            var rejected=mvc.perform(auth(post("/api/v1/analytics/events").contentType("application/json").content(JSON.writeValueAsBytes(JSON.createObjectNode().set("events",eleven))))).andReturn();
            assertEquals(400,rejected.getResponse().getStatus());assertEquals(10,adapter.count());
        }
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder auth(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder r){return r.with(q->{q.setUserPrincipal((Principal)()->"synthetic-owner-A");return q;});}
    private static JsonNode fixture()throws Exception{return JSON.readTree(Files.readString(FIXTURE));}
    private static byte[] batch(JsonNode e)throws Exception{var root=JSON.createObjectNode();root.putArray("events").add(e);return JSON.writeValueAsBytes(root);}
}
