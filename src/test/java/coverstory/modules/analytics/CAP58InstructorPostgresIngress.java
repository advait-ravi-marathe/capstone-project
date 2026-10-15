package coverstory.modules.analytics;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Clock;
import java.util.*;

/** Disposable, test-only durable receipt adapter. Never wired into the application. */
final class CAP58InstructorPostgresIngress implements AnalyticsIngestionPort, AutoCloseable {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<String> FIELDS = Set.of("eventId", "eventType", "schemaVersion", "pseudonymousSessionId", "pageId", "targetId", "optionalDurationMs", "clientTime", "stepCode");
    private static final Set<String> PAGES = Set.of("health_discovery", "compare", "quote_review", "home", "checkout", "onboarding", "final_review");
    private final EmbeddedPostgres pg;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final Clock clock;

    CAP58InstructorPostgresIngress(Clock clock) throws Exception {
        this.clock=clock;
        pg=EmbeddedPostgres.builder().start();
        DataSource ds=pg.getPostgresDatabase(); jdbc=new JdbcTemplate(ds); tx=new TransactionTemplate(new DataSourceTransactionManager(ds));
        try(Connection c=ds.getConnection(); Statement s=c.createStatement(); var in=getClass().getResourceAsStream("/coverstory/CAP-58/schema.sql")) {
            for(String sql:new String(in.readAllBytes(), StandardCharsets.UTF_8).split(";")) if(!sql.isBlank()) s.execute(sql);
        }
    }

    @Override public Receipt accept(byte[] body,String owner) {
        if(body.length>8192)return new Receipt(413,"BODY_TOO_LARGE",0);
        final JsonNode events;
        try { JsonNode root=JSON.readTree(body); events=root.path("events"); if(!events.isArray()||events.isEmpty()||events.size()>10)return new Receipt(400,"INVALID_EVENT_COUNT",0); }
        catch(Exception e){return new Receipt(400,"INVALID_JSON",0);}
        List<Event> parsed=new ArrayList<>(); Set<String> ids=new HashSet<>();
        try {
            for(JsonNode e:events) {
                if(!valid(e))return new Receipt(400,"INVALID_EVENT",0);
                String id=e.path("eventId").asText(); if(!ids.add(id))return new Receipt(400,"DUPLICATE_ID_IN_BATCH",0);
                parsed.add(new Event(UUID.fromString(id),hash(JSON.writeValueAsBytes(e)),hash(owner.getBytes(StandardCharsets.UTF_8)),hash(e.path("pseudonymousSessionId").asText().getBytes(StandardCharsets.UTF_8)),e.path("eventType").asText(),e.path("pageId").asText(),e.has("targetId")?e.path("targetId").asText():null,e.has("optionalDurationMs")?e.path("optionalDurationMs").intValue():null));
            }
        } catch(Exception e){return new Receipt(400,"INVALID_EVENT",0);}
        try {
            boolean[] duplicate={true};
            tx.executeWithoutResult(status->{
                for(Event e:parsed) {
                    int inserted=jdbc.update("INSERT INTO cap58_harness.receipt(event_id,owner_hash,payload_hash,session_hash,event_type,page_id,target_id,duration_ms,received_at) VALUES (?,?,?,?,?,?,?,?,?) ON CONFLICT DO NOTHING",
                            e.id,e.ownerHash,e.payloadHash,e.sessionHash,e.type,e.page,e.target,e.duration,java.sql.Timestamp.from(clock.instant()));
                    if(inserted==1)duplicate[0]=false;
                    byte[] actual=jdbc.queryForObject("SELECT payload_hash FROM cap58_harness.receipt WHERE event_id=? AND owner_hash=?",byte[].class,e.id,e.ownerHash);
                    if(!MessageDigest.isEqual(actual,e.payloadHash))throw new Conflict();
                }
            });
            return new Receipt(202,duplicate[0]?"DUPLICATE":"RECEIVED",parsed.size());
        } catch(Conflict c){return new Receipt(409,"EVENT_ID_CONFLICT",0);} catch(RuntimeException e){return new Receipt(503,"ANALYTICS_UNAVAILABLE",0);}
    }

    int count() { return jdbc.queryForObject("SELECT count(*) FROM cap58_harness.receipt",Integer.class); }
    List<String> columns() { return jdbc.query("SELECT column_name FROM information_schema.columns WHERE table_schema='cap58_harness' AND table_name='receipt' ORDER BY ordinal_position",(r,n)->r.getString(1)); }
    private static boolean valid(JsonNode e) {
        if(!e.isObject())return false; for(String name:e.propertyNames())if(!FIELDS.contains(name))return false;
        if(!e.path("eventId").asText().matches("(?i)[0-9a-f-]{36}")||!e.path("pseudonymousSessionId").asText().matches("(?i)[0-9a-f-]{36}")||e.path("schemaVersion").asInt(-1)!=1||!PAGES.contains(e.path("pageId").asText()))return false;
        String type=e.path("eventType").asText(); if(type.equals("PAGE_VIEW")&&e.has("targetId"))return false;
        if(type.equals("CONTROL_CLICK")&&(!e.path("targetId").isTextual()||!Set.of("compare_plan","save_draft").contains(e.path("targetId").asText())))return false;
        if(!type.equals("PAGE_VIEW")&&!type.equals("CONTROL_CLICK"))return false;
        if(e.has("optionalDurationMs")&&(!e.path("optionalDurationMs").isIntegralNumber()||e.path("optionalDurationMs").asInt(-1)<0||e.path("optionalDurationMs").asInt()>300000))return false;
        return !e.has("journeyId");
    }
    private static byte[] hash(byte[] bytes) throws Exception{return MessageDigest.getInstance("SHA-256").digest(bytes);}
    @Override public void close() throws Exception {pg.close();}
    private record Event(UUID id,byte[] payloadHash,byte[] ownerHash,byte[] sessionHash,String type,String page,String target,Integer duration){}
    private static final class Conflict extends RuntimeException{}
}
