package coverstory.modules.catalog;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.JsonNode;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** CAP-631 instructor failure/recovery witness; all publication state and principals are synthetic. */
class First14RollbackRecoveryTest {
    @Test void precommitFailureRollsBackCandidateAndExplicitRetryPublishesItOnce() throws Exception {
        try(EmbeddedPostgres postgres=First14TestSupport.freshDatabase("cs90_30_seed.sql");
            ConfigurableApplicationContext app=First14TestSupport.startApp(postgres)){
            publishInitial(app);
            assertEquals(3,First14TestSupport.sqlActiveSnapshot(postgres).size());
            assertEquals(3,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.publication_audit_event"));
            assertEquals(0,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.health_offer_version WHERE version_id='"+First14TestSupport.A2+"'"));

            var failed=First14TestSupport.stageAndPublish(app,First14TestSupport.PRODUCT_A,First14TestSupport.A2,
                    "A/v2","synthetic-health-A",First14TestSupport.HASH_A2,true);
            assertEquals(503,failed.status(),failed.body());assertEquals("PUBLICATION_UNAVAILABLE",failed.code());
            assertEquals(0,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.health_offer_version WHERE version_id='"+First14TestSupport.A2+"'"),"candidate version insert must roll back");
            assertEquals(0,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.health_offer_discovery_metadata WHERE version_id='"+First14TestSupport.A2+"'"));
            assertEquals(0,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.active_health_offer WHERE version_id='"+First14TestSupport.A2+"'"));
            assertEquals(3,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.publication_audit_event"),"no orphan audit row");
            assertEquals(First14TestSupport.A1,First14TestSupport.sqlActiveSnapshot(postgres).get("synthetic-health-A").versionId());
            JsonNode afterFailure=First14TestSupport.apiEnvelope(app);
            assertEquals(3,afterFailure.path("versions").size());
            assertEquals(3,First14TestSupport.apiPublishedSnapshot(afterFailure).size());

            var retry=First14TestSupport.stageAndPublish(app,First14TestSupport.PRODUCT_A,First14TestSupport.A2,
                    "A/v2","synthetic-health-A",First14TestSupport.HASH_A2,false);
            assertEquals(201,retry.status(),retry.body());assertEquals("PUBLISHED",retry.code());
            assertEquals(1,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.health_offer_version WHERE version_id='"+First14TestSupport.A2+"'"));
            assertEquals(4,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.publication_audit_event"));
            assertEquals(First14TestSupport.A2,First14TestSupport.sqlActiveSnapshot(postgres).get("synthetic-health-A").versionId());
            assertPublishedAndRetired(First14TestSupport.apiEnvelope(app));
            var replay=First14TestSupport.stageAndPublish(app,First14TestSupport.PRODUCT_A,First14TestSupport.A2,
                    "A/v2","synthetic-health-A",First14TestSupport.HASH_A2,false);
            assertEquals(200,replay.status(),replay.body());assertEquals("DUPLICATE",replay.code());
            assertEquals(4,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.publication_audit_event"),"same retry does not create a second publication audit");
            assertPublishedAndRetired(First14TestSupport.apiEnvelope(app));
        }
    }

    @Test void chromiumKeepsA1SelectionAcrossUnavailableReadAndExplicitRetry() throws Exception {
        Assumptions.assumeTrue(Boolean.getBoolean("coverstory.cs90.browser"),"enable with -Dcoverstory.cs90.browser=true and Chromium");
        try(EmbeddedPostgres postgres=First14TestSupport.freshDatabase("cs90_30_seed.sql");
            ConfigurableApplicationContext app=First14TestSupport.startApp(postgres)){
            publishInitial(app);
            ProcessBuilder builder=new ProcessBuilder("node","web/scripts/first14-recovery-browser.mjs","http://127.0.0.1:"+First14TestSupport.port(app))
                    .directory(java.nio.file.Path.of(".").toFile()).redirectErrorStream(true);
            String chromium=System.getProperty("coverstory.cs90.chromium");if(chromium!=null&&!chromium.isBlank())builder.environment().put("COVERSTORY_CHROMIUM",chromium);
            Process browser=builder.start();
            try(BufferedReader output=new BufferedReader(new InputStreamReader(browser.getInputStream(),StandardCharsets.UTF_8));
                OutputStreamWriter commands=new OutputStreamWriter(browser.getOutputStream(),StandardCharsets.UTF_8)){
                First14TestSupport.awaitLine(output,"CS90_30_BROWSER_READY",Duration.ofSeconds(90));
                var failure=First14TestSupport.stageAndPublish(app,First14TestSupport.PRODUCT_A,First14TestSupport.A2,
                        "A/v2","synthetic-health-A",First14TestSupport.HASH_A2,true);
                assertEquals(503,failure.status(),failure.body());
                try(var connection=postgres.getPostgresDatabase().getConnection();var statement=connection.createStatement()){
                    statement.execute("DROP VIEW catalog.health_offer_discovery_export");
                }
                var unavailable=First14TestSupport.get(app,"/api/catalog/versions?category=health");
                assertEquals(503,unavailable.statusCode(),unavailable.body());
                assertEquals("CATALOG_PERSISTENCE_UNAVAILABLE",First14TestSupport.JSON.readTree(unavailable.body()).path("code").asText());
                commands.write("OUTAGE\n");commands.flush();
                First14TestSupport.awaitLine(output,"CS90_30_OUTAGE_VALIDATED",Duration.ofSeconds(30));

                First14TestSupport.executeResource(postgres,"/db/schema/catalog_discovery.sql");
                var retry=First14TestSupport.stageAndPublish(app,First14TestSupport.PRODUCT_A,First14TestSupport.A2,
                        "A/v2","synthetic-health-A",First14TestSupport.HASH_A2,false);
                assertEquals(201,retry.status(),retry.body());
                var duplicate=First14TestSupport.stageAndPublish(app,First14TestSupport.PRODUCT_A,First14TestSupport.A2,
                        "A/v2","synthetic-health-A",First14TestSupport.HASH_A2,false);
                assertEquals(200,duplicate.status(),duplicate.body());assertEquals("DUPLICATE",duplicate.code());
                commands.write("RECOVER\n");commands.flush();
                First14TestSupport.awaitLine(output,"CS90_30_RECOVERY_VALIDATED",Duration.ofSeconds(45));
            }
            assertTrue(browser.waitFor(30,TimeUnit.SECONDS),"Chromium recovery browser did not exit");
            assertEquals(0,browser.exitValue());
            assertPublishedAndRetired(First14TestSupport.apiEnvelope(app));
            assertEquals(4,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.publication_audit_event"));
        }
    }

    private static void publishInitial(ConfigurableApplicationContext app)throws Exception{
        assertEquals(201,First14TestSupport.publishPrepared(app,First14TestSupport.PRODUCT_A,First14TestSupport.A1,First14TestSupport.HASH_A1,false).status());
        assertEquals(201,First14TestSupport.publishPrepared(app,First14TestSupport.PRODUCT_B,First14TestSupport.B1,First14TestSupport.HASH_B1,false).status());
        assertEquals(201,First14TestSupport.publishPrepared(app,First14TestSupport.PRODUCT_C,First14TestSupport.C1,First14TestSupport.HASH_C1,false).status());
    }

    private static void assertPublishedAndRetired(JsonNode envelope){
        assertEquals(4,envelope.path("versions").size());
        boolean a1=false,a2=false;
        for(JsonNode row:envelope.path("versions")){
            if(First14TestSupport.A1.equals(row.path("versionId").asText()))a1="RETIRED".equals(row.path("status").asText());
            if(First14TestSupport.A2.equals(row.path("versionId").asText()))a2="PUBLISHED".equals(row.path("status").asText());
        }
        assertTrue(a1,"A/v1 remains immutable, selected history with retired status");assertTrue(a2,"A/v2 is visible after explicit publish and refetch");
        assertEquals(3,First14TestSupport.apiPublishedSnapshot(envelope).size());
    }
}
