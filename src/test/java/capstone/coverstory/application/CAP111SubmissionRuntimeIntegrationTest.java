package capstone.coverstory.application;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import coverstory.CoverStoryApplication;

import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Test-only HTTP witness; this controller is absent from the ordinary application context. */
class CAP111SubmissionRuntimeIntegrationTest {
    private static final UUID APP=UUID.fromString("11100000-0000-4000-8000-000000000001");
    private static final UUID OWNER=UUID.fromString("11100000-0000-4000-8000-000000000002");
    private static final UUID QUOTE=UUID.fromString("11100000-0000-4000-8000-000000000004");
    private static final String CONFIG="a".repeat(64), TERMS="b".repeat(64);

    @Test void explicitSubmissionIsOwnerScopedIdempotentAndOnlyPending() throws Exception {
        try (EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            String url,user;
            try (Connection c=pg.getPostgresDatabase().getConnection(); Statement s=c.createStatement()) {
                url=c.getMetaData().getURL(); user=c.getMetaData().getUserName();
                s.execute(Files.readString(Path.of("src/main/resources/db/schema/ownership.sql")));
                s.execute(Files.readString(Path.of("src/main/resources/db/schema/constraints.sql")));
                s.execute(Files.readString(Path.of("src/main/resources/db/schema/cap78_purchase.sql")));
                s.execute("INSERT INTO application.application_record(application_id,owner_id,revision,quote_id,status) VALUES ('"+APP+"','"+OWNER+"',3,'"+QUOTE+"','DRAFT')");
                s.execute(Files.readString(Path.of("src/test/resources/coverstory/cap_111_submission.sql")));
                s.execute("INSERT INTO cap111_harness.review_facts(application_id,owner_id,reviewed_revision,quote_id,configuration_hash,terms_hash,current_configuration_hash,current_terms_hash,required_fields_complete,disclosures_complete,extraction_confirmed,payment_reconciled,refund_reconciled) VALUES ('"+APP+"','"+OWNER+"',3,'"+QUOTE+"','"+CONFIG+"','"+TERMS+"','"+CONFIG+"','"+TERMS+"',true,true,true,true,true)");
            }
            try (ConfigurableApplicationContext app=start(url,user)) {
                int port=((WebServerApplicationContext)app).getWebServer().getPort();
                var missingConsent=post(port,OWNER,"key-1",false,3);
                assertEquals(400,missingConsent.statusCode());
                JdbcTemplate db=app.getBean("applicationResumeJdbcTemplate",JdbcTemplate.class);
                db.update("UPDATE cap111_harness.review_facts SET current_terms_hash=? WHERE application_id=?", "c".repeat(64), APP);
                assertEquals(409,post(port,OWNER,"stale-terms",true,3).statusCode());
                db.update("UPDATE cap111_harness.review_facts SET current_terms_hash=? WHERE application_id=?", TERMS, APP);
                var first=post(port,OWNER,"key-1",true,3);
                assertEquals(202,first.statusCode(),first.body());
                String intent=json(first.body(),"intentId");
                assertEquals("SUBMISSION_PENDING",json(first.body(),"status"));
                var replay=post(port,OWNER,"key-1",true,3);
                assertEquals(202,replay.statusCode()); assertEquals(intent,json(replay.body(),"intentId"));
                var conflict=post(port,OWNER,"key-1",true,2);
                assertEquals(409,conflict.statusCode());
                assertEquals(404,post(port,UUID.randomUUID(),"foreign",true,3).statusCode());
                assertEquals(1,db.queryForObject("SELECT count(*) FROM cap111_harness.submission_intent",Integer.class));
                assertEquals(1,db.queryForObject("SELECT count(*) FROM cap111_harness.idempotency_receipt",Integer.class));
                assertEquals(1,db.queryForObject("SELECT count(*) FROM cap111_harness.dispatch_intent WHERE status='PENDING'",Integer.class),"durable intent is pending only; no insurer request or cover is claimed");
                UUID dispatch=db.queryForObject("SELECT dispatch_id FROM cap111_harness.dispatch_intent",UUID.class);
                var cancelled=cancel(port,dispatch);
                assertEquals(200,cancelled.statusCode(),cancelled.body()); assertEquals("CANCELLED_BEFORE_CLAIM",json(cancelled.body(),"status"));
                assertEquals("CANCELLED",db.queryForObject("SELECT status FROM cap111_harness.dispatch_intent",String.class));
                db.update("UPDATE cap111_harness.dispatch_intent SET status='UNKNOWN' WHERE dispatch_id=?",dispatch);
                db.update("UPDATE cap111_harness.submission_intent SET status='SUBMISSION_PENDING' WHERE intent_id=?",UUID.fromString(intent));
                var unknown=cancel(port,dispatch);
                assertEquals(409,unknown.statusCode()); assertEquals("RECONCILIATION_REQUIRED",json(unknown.body(),"status"));
                assertEquals("RECONCILIATION_REQUIRED",db.queryForObject("SELECT status FROM cap111_harness.submission_intent",String.class));
                db.update("UPDATE application.application_record SET revision=4 WHERE application_id=?",APP);
                assertEquals(409,post(port,OWNER,"key-2",true,3).statusCode(),"changed review revision fails closed");
            }
        }
    }

    private static ConfigurableApplicationContext start(String url,String user) {
        return new SpringApplicationBuilder(CoverStoryApplication.class, TestHarnessConfiguration.class)
                .web(WebApplicationType.SERVLET).properties("server.port=0","spring.main.banner-mode=off","logging.level.root=ERROR",
                        "spring.profiles.active=cap111-runtime-test","coverstory.application.jdbc.url="+url,
                        "coverstory.application.jdbc.username="+user,"coverstory.application.jdbc.password=").run();
    }
    private static String json(String body,String key) throws Exception { return new tools.jackson.databind.ObjectMapper().readTree(body).path(key).asText(); }
    private static HttpResponse<String> post(int port,UUID owner,String key,boolean consent,long revision) throws Exception {
        String body="{\"revision\":"+revision+",\"quoteId\":\""+QUOTE+"\",\"configurationHash\":\""+CONFIG+"\",\"termsHash\":\""+TERMS+"\",\"consent\":"+consent+"}";
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/test/cap111/submit"))
                .header("Content-Type","application/json").header("X-Test-Owner",owner.toString()).header("Idempotency-Key",key)
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }

    private static HttpResponse<String> cancel(int port,UUID dispatch) throws Exception {
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/test/cap111/dispatch/"+dispatch+"/cancel"))
                .POST(HttpRequest.BodyPublishers.noBody()).build(),HttpResponse.BodyHandlers.ofString());
    }

    @Configuration(proxyBeanMethods=false)
    static class TestHarnessConfiguration {
        @Bean @Primary CAP111TestController cap111TestController(@Qualifier("applicationResumeJdbcTemplate") JdbcTemplate jdbc,
                PlatformTransactionManager tx) { return new CAP111TestController(jdbc,new TransactionTemplate(tx)); }
    }

    @RestController
    static class CAP111TestController {
        private final JdbcTemplate jdbc; private final TransactionTemplate tx;
        CAP111TestController(JdbcTemplate jdbc,TransactionTemplate tx){this.jdbc=jdbc;this.tx=tx;}
        record Payload(long revision,String quoteId,String configurationHash,String termsHash,boolean consent) { }
        record Reply(String intentId,String status) { }
        @PostMapping("/test/cap111/dispatch/{dispatch}/cancel") ResponseEntity<?> cancel(@PathVariable("dispatch") UUID dispatch) {
            return tx.execute(status -> {
                int changed=jdbc.update("UPDATE cap111_harness.dispatch_intent SET status='CANCELLED',updated_at=now() WHERE dispatch_id=? AND status='PENDING'",dispatch);
                if(changed==1)return ResponseEntity.ok(Map.of("status","CANCELLED_BEFORE_CLAIM"));
                var state=jdbc.query("SELECT status,submission_intent_id FROM cap111_harness.dispatch_intent WHERE dispatch_id=?",(rs,n)->new Object[]{rs.getString(1),rs.getObject(2,UUID.class)},dispatch);
                if(state.isEmpty())return ResponseEntity.notFound().build();
                Object[] row=state.get(0);
                if("UNKNOWN".equals(row[0])) {
                    jdbc.update("UPDATE cap111_harness.submission_intent SET status='RECONCILIATION_REQUIRED' WHERE intent_id=?",row[1]);
                    return ResponseEntity.status(409).body(Map.of("status","RECONCILIATION_REQUIRED"));
                }
                return ResponseEntity.status(409).body(Map.of("status","CLAIMED_OR_TERMINAL"));
            });
        }

        @PostMapping("/test/cap111/submit") ResponseEntity<?> submit(@RequestHeader("X-Test-Owner") UUID owner,
                @RequestHeader("Idempotency-Key") String key,@RequestBody Payload payload) {
            if(key.length()<1||key.length()>100)return ResponseEntity.badRequest().build();
            return tx.execute(status -> {
                var facts=jdbc.query("SELECT r.revision,r.quote_id,r.owner_id,f.reviewed_revision,f.quote_id,f.configuration_hash,f.terms_hash,f.required_fields_complete,f.disclosures_complete,f.extraction_confirmed,f.payment_reconciled,f.refund_reconciled,f.current_configuration_hash,f.current_terms_hash FROM application.application_record r JOIN cap111_harness.review_facts f USING(application_id,owner_id) WHERE r.application_id=? AND r.owner_id=? FOR UPDATE",(rs,n)->new Object[]{rs.getLong(1),rs.getObject(2,UUID.class),rs.getObject(3,UUID.class),rs.getLong(4),rs.getObject(5,UUID.class),rs.getString(6),rs.getString(7),rs.getBoolean(8),rs.getBoolean(9),rs.getBoolean(10),rs.getBoolean(11),rs.getBoolean(12),rs.getString(13),rs.getString(14)},APP,owner);
                if(facts.isEmpty())return ResponseEntity.notFound().build(); Object[] f=facts.get(0);
                byte[] keyHash=sha(key), requestHash=sha(payload.revision()+":"+payload.quoteId()+":"+payload.configurationHash()+":"+payload.termsHash()+":"+payload.consent());
                var prior=jdbc.query("SELECT request_hash,intent_id FROM cap111_harness.idempotency_receipt WHERE owner_id=? AND application_id=? AND key_hash=?",(rs,n)->new Object[]{rs.getBytes(1),rs.getObject(2,UUID.class)},owner,APP,keyHash);
                if(!prior.isEmpty()) { Object[] old=prior.get(0); if(!MessageDigest.isEqual((byte[])old[0],requestHash))return ResponseEntity.status(409).body(Map.of("code","IDEMPOTENCY_CONFLICT")); return ResponseEntity.accepted().body(new Reply(old[1].toString(),"SUBMISSION_PENDING")); }
                var decision=CAP111InstructorOracle.evaluate(new CAP111InstructorOracle.Facts((long)f[0],(long)f[3],(UUID)f[1],(UUID)f[4],(String)f[12],(String)f[5],(String)f[13],(String)f[6],(boolean)f[7],(boolean)f[8],(boolean)f[9],(boolean)f[10],(boolean)f[11]),payload.consent());
                if(payload.revision()!=(long)f[0] || !payload.quoteId().equals(f[1].toString()) || !payload.quoteId().equals(f[4].toString())
                        || !payload.configurationHash().equals(f[12]) || !payload.termsHash().equals(f[13]))
                    return ResponseEntity.status(409).body(Map.of("code","REVIEW_STALE"));
                if(decision.status()!=202)return ResponseEntity.status(decision.status()).body(Map.of("code",decision.code()));
                UUID id=UUID.randomUUID(); UUID dispatchId=UUID.randomUUID();
                jdbc.update("INSERT INTO cap111_harness.submission_intent(intent_id,application_id,owner_id,reviewed_revision,quote_id,configuration_hash,terms_hash,status) VALUES (?,?,?,?,?,?,?,'SUBMISSION_PENDING')",id,APP,owner,f[3],f[4],f[5],f[6]);
                jdbc.update("INSERT INTO cap111_harness.dispatch_intent(dispatch_id,submission_intent_id,status) VALUES (?,?,'PENDING')",dispatchId,id);
                jdbc.update("INSERT INTO cap111_harness.idempotency_receipt(owner_id,application_id,key_hash,request_hash,intent_id,response_status) VALUES (?,?,?,?,?,202)",owner,APP,keyHash,requestHash,id);
                return ResponseEntity.accepted().body(new Reply(id.toString(),"SUBMISSION_PENDING"));
            });
        }
        private static byte[] sha(String value){try{return MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));}catch(Exception e){throw new IllegalStateException(e);}}
    }
}
