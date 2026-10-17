package coverstory.modules.catalog;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.JsonNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** CAP-630 instructor integration witness; never calls or implements a learner seam. */
class First14CatalogConsistencyTest {
    @Test void duplicateAndStaleReferenceOraclePreserveSqlApiIdentity() throws Exception {
        try (EmbeddedPostgres postgres=First14TestSupport.freshDatabase("cs90_29_seed.sql");
             ConfigurableApplicationContext app=First14TestSupport.startApp(postgres)) {
            assertEquals(201,First14TestSupport.publishPrepared(app,First14TestSupport.PRODUCT_A,First14TestSupport.A1,First14TestSupport.HASH_A1,false).status());
            assertEquals(201,First14TestSupport.publishPrepared(app,First14TestSupport.PRODUCT_B,First14TestSupport.B1,First14TestSupport.HASH_B1,false).status());
            assertEquals(201,First14TestSupport.publishPrepared(app,First14TestSupport.PRODUCT_C,First14TestSupport.C1,First14TestSupport.HASH_C1,false).status());

            Map<String,First14TestSupport.PublishedIdentity> sqlBefore=First14TestSupport.sqlActiveSnapshot(postgres);
            JsonNode apiBefore=First14TestSupport.apiEnvelope(app);
            assertMatchesSqlAndApi(sqlBefore,apiBefore);
            assertEquals(3,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.health_offer_version"));
            assertEquals(3,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.publication_audit_event"));

            var duplicate=First14TestSupport.publishPrepared(app,First14TestSupport.PRODUCT_A,First14TestSupport.A1,First14TestSupport.HASH_A1,false);
            assertEquals(200,duplicate.status(),duplicate.body());
            assertEquals("DUPLICATE",duplicate.code());
            assertEquals(apiBefore,First14TestSupport.apiEnvelope(app),"duplicate publication leaves the public API envelope byte-meaning unchanged");
            assertEquals(sqlBefore,First14TestSupport.sqlActiveSnapshot(postgres));
            assertEquals(1,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.health_offer_version WHERE product_id='"+First14TestSupport.PRODUCT_A+"' AND version_id='"+First14TestSupport.A1+"'"));
            assertEquals(3,First14TestSupport.count(postgres,"SELECT count(*) FROM catalog.publication_audit_event"),"duplicate A1 must not add another audit receipt");

            JsonNode fixture=First14TestSupport.JSON.readTree(First14TestSupport.fixture("fixtures/coverstory/CS90-29/catalog-consistency.json"));
            var correct=fixture.path("correct_snapshot");
            var actual=sqlBefore.get(correct.path("sourceId").asText());
            assertEquals(correct.path("versionId").asText(),actual.versionId());
            assertEquals(correct.path("contentSha256").asText(),actual.contentSha256());
            assertIdentity(correct.path("sourceId").asText(),correct.path("versionId").asText(),correct.path("sourceVersion").asText(),
                    actual,apiPublished(apiBefore,correct.path("sourceId").asText()),actual);

            var stale=fixture.path("stale_export_reference");
            AssertionError rejected=assertThrows(AssertionError.class,()->assertIdentity(stale.path("sourceId").asText(),
                    stale.path("versionId").asText(),stale.path("sourceVersion").asText(),actual,
                    apiPublished(apiBefore,stale.path("sourceId").asText()),actual));
            assertTrue(rejected.getMessage().contains("identity/provenance mismatch"),rejected.getMessage());
            // Negative self-check used copies only; verify the correct durable snapshot still passes.
            assertIdentity(correct.path("sourceId").asText(),correct.path("versionId").asText(),correct.path("sourceVersion").asText(),
                    First14TestSupport.sqlActiveSnapshot(postgres).get(correct.path("sourceId").asText()),
                    apiPublished(First14TestSupport.apiEnvelope(app),correct.path("sourceId").asText()),actual);
        }
    }

    @Test void chromiumComparisonRendersTheSameSqlApiIdentities() throws Exception {
        Assumptions.assumeTrue(Boolean.getBoolean("coverstory.cs90.browser"),"enable with -Dcoverstory.cs90.browser=true and Chromium");
        try (EmbeddedPostgres postgres=First14TestSupport.freshDatabase("cs90_29_seed.sql");
             ConfigurableApplicationContext app=First14TestSupport.startApp(postgres)) {
            First14TestSupport.publishPrepared(app,First14TestSupport.PRODUCT_A,First14TestSupport.A1,First14TestSupport.HASH_A1,false);
            First14TestSupport.publishPrepared(app,First14TestSupport.PRODUCT_B,First14TestSupport.B1,First14TestSupport.HASH_B1,false);
            First14TestSupport.publishPrepared(app,First14TestSupport.PRODUCT_C,First14TestSupport.C1,First14TestSupport.HASH_C1,false);
            Map<String,First14TestSupport.PublishedIdentity> sql=First14TestSupport.sqlActiveSnapshot(postgres);
            JsonNode api=First14TestSupport.apiEnvelope(app);
            assertMatchesSqlAndApi(sql,api);

            ProcessBuilder builder=new ProcessBuilder("node","web/scripts/first14-consistency-browser.mjs","http://127.0.0.1:"+First14TestSupport.port(app))
                    .directory(java.nio.file.Path.of(".").toFile()).redirectErrorStream(true);
            String chromium=System.getProperty("coverstory.cs90.chromium");
            if(chromium!=null&&!chromium.isBlank())builder.environment().put("COVERSTORY_CHROMIUM",chromium);
            Process browser=builder.start();
            String output;
            try(BufferedReader lines=new BufferedReader(new InputStreamReader(browser.getInputStream(),StandardCharsets.UTF_8))){
                StringBuilder collected=new StringBuilder();String line;while((line=lines.readLine())!=null)collected.append(line).append('\n');output=collected.toString();
            }
            assertTrue(browser.waitFor(100,TimeUnit.SECONDS),"Chromium browser script timed out");
            assertEquals(0,browser.exitValue(),output);
            String summaryLine=output.lines().filter(line->line.startsWith("CS90_29_RESULT ")).findFirst().orElseThrow(()->new AssertionError(output));
            JsonNode summary=First14TestSupport.JSON.readTree(summaryLine.substring("CS90_29_RESULT ".length()));
            assertTrue(summary.path("liveSpringApi").asBoolean());assertTrue(summary.path("instructorDecoderInjected").asBoolean());
            assertTrue(summary.path("unknownVisible").asBoolean());assertTrue(summary.path("payableClaim").asBoolean());
            Map<String,First14TestSupport.PublishedIdentity> ui=new HashMap<>();
            JsonNode headers=summary.path("header"),ids=summary.path("versionIds");
            for(int i=0;i<headers.size();i++){
                JsonNode h=headers.get(i);String source=h.path("sourceId").asText();
                ui.put(source,new First14TestSupport.PublishedIdentity(source,h.path("sourceVersion").asText(),ids.get(i).asText(),""));
            }
            for(String source:sql.keySet())assertIdentity(source,sql.get(source).versionId(),sql.get(source).sourceVersion(),sql.get(source),apiPublished(api,source),ui.get(source));
        }
    }

    private static void assertMatchesSqlAndApi(Map<String,First14TestSupport.PublishedIdentity> sql,JsonNode api){
        Map<String,First14TestSupport.PublishedIdentity> apiRows=First14TestSupport.apiPublishedSnapshot(api);
        assertEquals(sql.keySet(),apiRows.keySet());
        for(String source:sql.keySet()){
            var db=sql.get(source);var projection=apiRows.get(source);
            assertIdentity(source,db.versionId(),db.sourceVersion(),db,projection,db);
        }
    }

    private static First14TestSupport.PublishedIdentity apiPublished(JsonNode envelope,String source){
        var value=First14TestSupport.apiPublishedSnapshot(envelope).get(source);
        assertNotNull(value,"public API must include published source "+source);return value;
    }

    private static void assertIdentity(String source,String expectedId,String expectedVersion,
                                       First14TestSupport.PublishedIdentity sql,
                                       First14TestSupport.PublishedIdentity api,
                                       First14TestSupport.PublishedIdentity ui){
        assertNotNull(sql,"SQL snapshot missing "+source);assertNotNull(api,"API snapshot missing "+source);assertNotNull(ui,"comparison view missing "+source);
        boolean matches=source.equals(sql.sourceId())&&source.equals(api.sourceId())&&source.equals(ui.sourceId())
                &&expectedId.equals(sql.versionId())&&expectedId.equals(api.versionId())&&expectedId.equals(ui.versionId())
                &&expectedVersion.equals(sql.sourceVersion())&&expectedVersion.equals(api.sourceVersion())&&expectedVersion.equals(ui.sourceVersion());
        assertTrue(matches,"identity/provenance mismatch for "+source+": expected "+expectedId+" / "+expectedVersion
                +", SQL "+sql.versionId()+" / "+sql.sourceVersion()+", API "+api.versionId()+" / "+api.sourceVersion()
                +", UI "+ui.versionId()+" / "+ui.sourceVersion());
    }
}
