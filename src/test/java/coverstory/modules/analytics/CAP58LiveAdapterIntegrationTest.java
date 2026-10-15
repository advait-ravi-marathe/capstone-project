package coverstory.modules.analytics;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import coverstory.modules.analytics.api.AnalyticsReceiptStore;
import coverstory.modules.analytics.api.AnalyticsScopeHashPort;
import coverstory.modules.analytics.api.CAP58Seam;
import coverstory.modules.application.api.ApplicationJourneyOwnershipPort;
import coverstory.modules.policy.api.CollectionConsentPort;
import coverstory.CoverStoryApplication;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/** Proves the configured ingestion port invokes CAP58Seam before Analytics-owned PostgreSQL receipts. */
class CAP58LiveAdapterIntegrationTest {
    private static final ObjectMapper JSON=new ObjectMapper();
    private static final Path FIXTURE=Path.of("fixtures/coverstory/CAP-58/synthetic-v1.json");
    private static final String OWNER="3c7d76dc-29dd-4f2c-a979-8c4a4000a001";
    private static final byte[] KEY=new byte[32];

    @Test void durableReplayConflictAndPrivacySafeSchemaUseTheLearnerSeamOutput() throws Exception {
        try(Runtime db=runtime(new CAP58ReferenceValidator(),owner->true,(owner,ids)->Set.of())) {
            MockMvc mvc=mvc(db.port);
            JsonNode event=fixture().path("events").path("page_view").deepCopy();
            ((ObjectNode)event).put("clientTime","2035-01-01T00:00:00Z");
            byte[] request=batch(event);
            assertReceipt(mvc,request,202,"RECEIVED");
            assertReceipt(mvc,request,202,"DUPLICATE");
            ((ObjectNode)event).put("pageId","compare");
            assertReceipt(mvc,batch(event),409,"EVENT_ID_CONFLICT");
            assertEquals(1,db.count());
            assertEquals(List.of("owner_hash","event_id","payload_hash","session_scope_hash","event_type","page_id","step_code","target_id","duration_ms","server_received_at"),db.columns());
            assertFalse(db.columns().stream().anyMatch(x->x.contains("payload")&& !x.equals("payload_hash")));
            assertEquals(Instant.parse("2026-10-15T12:00:00Z"),db.receivedAt());
        }
    }

    @Test void applicationOwnershipAndConsentAreResolvedBeforeLearnerSerializationOrPersistence() throws Exception {
        AtomicInteger order=new AtomicInteger();
        UUID owned=UUID.fromString("3c7d76dc-29dd-4f2c-a979-8c4a40c8a001");
        ApplicationJourneyOwnershipPort ownership=(owner,ids)->{assertEquals(0,order.getAndIncrement());assertEquals(Set.of(owned),ids);return owner.equals(OWNER)?Set.of(owned):Set.of();};
        CAP58Seam seam=new CAP58ReferenceValidator(){
            @Override public ValidationResult validateAnalyticsBatch(byte[] body,TrustedAnalyticsContext context){
                assertEquals(1,order.getAndIncrement(),"Application owns and authorizes IDs before Analytics builds its sanitized events");
                return super.validateAnalyticsBatch(body,context);
            }
        };
        try(Runtime db=runtime(seam,owner->true,ownership)){
            JsonNode event=fixture().path("events").path("page_view").deepCopy();((ObjectNode)event).put("journeyId",owned.toString());
            assertReceipt(mvc(db.port),batch(event),202,"RECEIVED");
            order.set(0);
            assertEquals(new AnalyticsIngestionPort.Receipt(400,"JOURNEY_NOT_AUTHORIZED",0),db.port.accept(batch(event),"foreign-owner"));
            assertEquals(1,db.count());
        }

        AtomicInteger validatorCalls=new AtomicInteger();
        CAP58Seam forbiddenConsentSeam=new CAP58Seam(){
            @Override public ValidationResult validateAnalyticsBatch(byte[] b,TrustedAnalyticsContext c){validatorCalls.incrementAndGet();return null;}
        };
        try(Runtime db=runtime(forbiddenConsentSeam,owner->false,(owner,ids)->{throw new AssertionError("consent denial must short-circuit");})){
            assertReceipt(mvc(db.port),batch(fixture().path("events").path("page_view")),202,"DISCARDED_BY_POLICY");
            assertEquals(0,validatorCalls.get());assertEquals(0,db.count());
        }
    }

    @Test void missingInfrastructureAndUnimplementedLearnerSeamHaveDifferentOutcomes() throws Exception {
        var event=batch(fixture().path("events").path("page_view"));
        CAP58AnalyticsAdapter absent=new CAP58AnalyticsAdapter(new CAP58Seam(),null,null,null,null,JSON,Clock.systemUTC());
        assertEquals(new AnalyticsIngestionPort.Receipt(503,"ANALYTICS_UNAVAILABLE",0),absent.accept(event,OWNER));
        try(Runtime db=runtime(new CAP58Seam(),owner->true,(owner,ids)->Set.of())){
            assertEquals(new AnalyticsIngestionPort.Receipt(501,"VALIDATOR_NOT_IMPLEMENTED",0),db.port.accept(event,OWNER));
        }
    }

    @Test void exactByteAndEventLimitsReachTheRealAdapterWithoutPartialWrites() throws Exception {
        try(Runtime db=runtime(new CAP58ReferenceValidator(),owner->true,(owner,ids)->Set.of())){
            MockMvc mvc=mvc(db.port);
            var ten=JSON.createArrayNode();
            for(int i=10;i<20;i++){JsonNode e=fixture().path("events").path("page_view").deepCopy();((ObjectNode)e).put("eventId",String.format("58000000-0000-4000-8000-%012d",i));ten.add(e);}
            byte[] compact=body(ten);byte[] exact=Arrays.copyOf(compact,8192);Arrays.fill(exact,compact.length,exact.length,(byte)' ');
            assertReceipt(mvc,exact,202,"RECEIVED");assertEquals(10,db.count());
            var eleven=ten.deepCopy();JsonNode e=fixture().path("events").path("page_view").deepCopy();((ObjectNode)e).put("eventId","58000000-0000-4000-8000-000000000020");eleven.add(e);
            assertReceipt(mvc,body(eleven),400,"INVALID_EVENT_COUNT");assertEquals(10,db.count());
        }
    }

    @Test void runningApplicationWiresDatasourceConsentHashingAndLearnerTodoToTheRealHttpRoute() throws Exception {
        try(EmbeddedPostgres postgres=EmbeddedPostgres.builder().start()) {
            String url,user;
            try(Connection c=postgres.getPostgresDatabase().getConnection();Statement s=c.createStatement()) {
                url=c.getMetaData().getURL();user=c.getMetaData().getUserName();
                s.execute("CREATE SCHEMA application");s.execute("CREATE SCHEMA analytics");
                s.execute("CREATE TABLE application.application_record(application_id UUID,owner_id UUID,revision BIGINT,PRIMARY KEY(application_id,revision))");
                s.execute("CREATE TABLE application.journey(journey_id UUID PRIMARY KEY,owner_id UUID,application_id UUID,revision BIGINT)");
                String ddl=Files.readString(Path.of("src/main/resources/db/schema/constraints.sql"));
                int start=ddl.indexOf("CREATE TABLE IF NOT EXISTS analytics.minimal_event_receipt (");int end=ddl.indexOf(");",start)+2;
                s.execute(ddl.substring(start,end));
            }
            String base64Key=Base64.getEncoder().encodeToString(KEY);
            try(ConfigurableApplicationContext context=new SpringApplicationBuilder(CoverStoryApplication.class,TestConsentConfiguration.class)
                    .properties("server.port=0","spring.main.banner-mode=off","logging.level.root=ERROR",
                            "spring.profiles.active=cap58-integration-test","coverstory.application.jdbc.url="+url,
                            "coverstory.application.jdbc.username="+user,"coverstory.application.jdbc.password=",
                            "coverstory.analytics.scope-hash-key-base64="+base64Key).run()) {
                assertInstanceOf(CAP58AnalyticsAdapter.class,context.getBean(AnalyticsIngestionPort.class));
                assertInstanceOf(JdbcAnalyticsReceiptStore.class,context.getBean(AnalyticsReceiptStore.class));
                assertNotNull(context.getBean(ApplicationJourneyOwnershipPort.class));
                MockMvc mvc=MockMvcBuilders.webAppContextSetup((org.springframework.web.context.WebApplicationContext)context).build();
                var response=mvc.perform(post("/api/v1/analytics/events").contentType("application/json")
                        .content(batch(fixture().path("events").path("page_view")))
                        .with(r->{r.setUserPrincipal(()->OWNER);return r;})).andReturn();
                assertEquals(501,response.getResponse().getStatus());
                assertEquals("VALIDATOR_NOT_IMPLEMENTED",JSON.readTree(response.getResponse().getContentAsByteArray()).path("code").asText());
                assertEquals(0,context.getBean(JdbcTemplate.class).queryForObject("SELECT count(*) FROM analytics.minimal_event_receipt",Integer.class));
            }
        }
    }

    private static Runtime runtime(CAP58Seam seam,CollectionConsentPort consent,ApplicationJourneyOwnershipPort journeys)throws Exception{
        EmbeddedPostgres pg=EmbeddedPostgres.builder().start();var ds=pg.getPostgresDatabase();
        try(Connection c=ds.getConnection();Statement s=c.createStatement()){
            s.execute("CREATE SCHEMA analytics");
            String ddl=Files.readString(Path.of("src/main/resources/db/schema/constraints.sql"));
            int start=ddl.indexOf("CREATE TABLE IF NOT EXISTS analytics.minimal_event_receipt (");
            int end=ddl.indexOf(");",start)+2;
            if(start<0||end<2)throw new IllegalStateException("missing Analytics receipt schema");
            s.execute(ddl.substring(start,end));
        }
        JdbcTemplate jdbc=new JdbcTemplate(ds);AnalyticsReceiptStore store=new JdbcAnalyticsReceiptStore(ds);
        AnalyticsScopeHashPort hashes=new HmacAnalyticsScopeHasher(Base64.getEncoder().encodeToString(KEY));
        return new Runtime(pg,jdbc,new CAP58AnalyticsAdapter(seam,consent,journeys,hashes,store,JSON,Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"),ZoneOffset.UTC)));
    }
    private static MockMvc mvc(AnalyticsIngestionPort port){return standaloneSetup(new coverstory.modules.analytics.api.AnalyticsIngestionController(port)).build();}
    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder auth(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder r){return r.with(q->{q.setUserPrincipal(()->OWNER);return q;});}
    private static void assertReceipt(MockMvc mvc,byte[] body,int status,String code)throws Exception{var r=mvc.perform(auth(post("/api/v1/analytics/events").contentType("application/json").content(body))).andReturn();assertEquals(status,r.getResponse().getStatus());assertEquals(code,JSON.readTree(r.getResponse().getContentAsByteArray()).path("code").asText());}
    private static JsonNode fixture()throws Exception{return JSON.readTree(Files.readString(FIXTURE));}
    private static byte[] batch(JsonNode event)throws Exception{var a=JSON.createArrayNode().add(event);return body(a);}
    private static byte[] body(JsonNode events)throws Exception{var root=JSON.createObjectNode();root.set("events",events);return JSON.writeValueAsBytes(root);}

    private record Runtime(EmbeddedPostgres postgres,JdbcTemplate jdbc,AnalyticsIngestionPort port)implements AutoCloseable{
        int count(){return jdbc.queryForObject("SELECT count(*) FROM analytics.minimal_event_receipt",Integer.class);}
        Instant receivedAt(){return jdbc.queryForObject("SELECT server_received_at FROM analytics.minimal_event_receipt LIMIT 1",java.sql.Timestamp.class).toInstant();}
        List<String> columns(){return jdbc.query("SELECT column_name FROM information_schema.columns WHERE table_schema='analytics' AND table_name='minimal_event_receipt' ORDER BY ordinal_position",(r,n)->r.getString(1));}
        @Override public void close()throws Exception{postgres.close();}
    }

    @Configuration @Profile("cap58-integration-test") static class TestConsentConfiguration {
        @Bean CollectionConsentPort testConsentPolicy(){return owner->true;}
    }
}
