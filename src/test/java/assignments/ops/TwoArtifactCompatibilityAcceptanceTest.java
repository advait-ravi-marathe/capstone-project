package assignments.ops;

import assignments.delivery.recovery.InspectionReport;
import assignments.delivery.recovery.InspectionReportCodec;
import assignments.delivery.recovery.RecoveryRecommendation;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Actual two-JAR, same-database matrix. Supply separately built immutable artifacts by property. */
@Tag("learnerAcceptance")
class TwoArtifactCompatibilityAcceptanceTest {
    private static final ThreadLocal<Integer> successfulOwnedProcessRuns = ThreadLocal.withInitial(() -> 0);
    private static final String POLICY = "policy-demo-1";
    private static final long VERSION = 1;

    @Test void day146RefusesAbsentSchemaWithoutStartingMigration() throws Exception {
        Path b=artifact("coverstory.release.candidate.jar");
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            String url=pg.getJdbcUrl("postgres","postgres");
            int before=scalar(url,"SELECT count(*) FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname='public' AND c.relkind IN ('r','p','v','m','f')");
            var args=List.of("--coverstory.release.inspect","--coverstory.release.policy-id=never-migrated","--coverstory.release.source-version=1");
            var result=runRaw(b,url,args);
            assertNotEquals(0,result.exitCode(),"absent schema is a nonzero refusal");
            assertFalse(result.capturedOutput().contains(InspectionReportCodec.PREFIX),"failure emits no success frame");
            assertTrue(result.capturedOutput().contains("release inspection failed (BadSqlGrammarException)"),
                    "an absent migration history table is diagnosed as the intended schema refusal");
            assertEquals(before,scalar(url,"SELECT count(*) FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace WHERE n.nspname='public' AND c.relkind IN ('r','p','v','m','f')"),
                    "startup and schema preflight cannot migrate or seed the database");
        }
    }

    @Test void day145PackagedInvalidModeFailsBeforeDatasourceInitialization() throws Exception {
        Path b=artifact("coverstory.release.candidate.jar");
        var result=runRaw(b,"jdbc:postgresql://127.0.0.1:1/no_database",List.of(
                "--coverstory.release.inspect","--coverstory.release.policy-id=bad","--coverstory.release.source-version=1",
                "--coverstory.recovery.action=retry"));
        assertNotEquals(0,result.exitCode());
        assertFalse(result.capturedOutput().contains(InspectionReportCodec.PREFIX));
        assertTrue(result.capturedOutput().contains("IllegalArgumentException"),
                "request rejection must occur before a database connection attempt");
        assertFalse(result.capturedOutput().contains("Connection refused"),
                "invalid mixed mode must not initialize the datasource");
    }

    @Test void day146ExistingSchemaProducesReadOnlyMissingWorkObservation() throws Exception {
        Path b=artifact("coverstory.release.candidate.jar");
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            var ds=pg.getPostgresDatabase(); Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
            String url=pg.getJdbcUrl("postgres","postgres"); String before=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            String output=run(b,url,List.of("--coverstory.release.inspect","--coverstory.release.policy-id=absent-key","--coverstory.release.source-version=99"));
            var frames=output.lines().filter(line->line.startsWith(InspectionReportCodec.PREFIX)).toList(); assertEquals(1,frames.size());
            var report=new InspectionReportCodec(new ObjectMapper()).decodeSingleFrame(frames.getFirst(),new InspectionReport.RequestedKey("absent-key",99));
            assertFalse(report.workPresent()); assertNull(report.envelope()); assertEquals(RecoveryRecommendation.NOT_FOUND,report.recommendation());
            assertEquals(before,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),"valid startup and absent-work inspection preserve every table and schema row");
        }
    }

    @Test void day146DatabaseReadFailureEmitsNoSuccessFrame() throws Exception {
        Path b=artifact("coverstory.release.candidate.jar");
        var result=runRaw(b,"jdbc:postgresql://127.0.0.1:1/no_database",List.of(
                "--coverstory.release.inspect","--coverstory.release.policy-id=valid-request","--coverstory.release.source-version=1"));
        assertNotEquals(0,result.exitCode(),"a valid request with database failure must fail closed");
        assertFalse(result.capturedOutput().contains(InspectionReportCodec.PREFIX),"read failure cannot produce a success frame");
        assertTrue(result.capturedOutput().contains("release inspection failed ("),"failure is diagnosed without raw connection details");
        assertTrue(result.capturedOutput().contains("CannotGetJdbcConnectionException"),"valid request reached the database-read failure path");
        assertFalse(result.capturedOutput().contains("127.0.0.1"),"database URL is not exposed in diagnostics");
    }

    @Test void day146SchemaGateAcceptsExactV001ToV005AndRejectsTypmodMutation() throws Exception {
        try (EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            var ds=pg.getPostgresDatabase();
            Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
            var jdbc=new JdbcTemplate(ds);
            var verifier=new assignments.delivery.recovery.ReleaseSchemaVerifier(jdbc);
            verifier.requireV001ThroughV005();
            jdbc.execute("ALTER TABLE quote_versions ALTER COLUMN annual_premium_rupees TYPE NUMERIC(13,2)");
            var mismatch=assertThrows(IllegalStateException.class,verifier::requireV001ThroughV005);
            assertTrue(mismatch.getMessage().contains("exact V001-V005 column contract"));
            assertEquals(13, jdbc.queryForObject("SELECT numeric_precision FROM information_schema.columns WHERE table_name='quote_versions' AND column_name='annual_premium_rupees'", Integer.class));
            assertEquals(5, jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE success", Integer.class));
        }
    }

    @Test void day146SchemaGateRejectsMigrationChecksumDriftWithoutRepair() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            var ds=pg.getPostgresDatabase(); Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
            var jdbc=new JdbcTemplate(ds);
            jdbc.update("UPDATE flyway_schema_history SET checksum=checksum+1 WHERE version='001'");
            String url=pg.getJdbcUrl("postgres","postgres"); String before=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            var verifier=new assignments.delivery.recovery.ReleaseSchemaVerifier(jdbc);
            assertThrows(IllegalStateException.class,verifier::requireV001ThroughV005);
            assertEquals(before,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),"checksum refusal is read-only and does not repair history");
        }
    }

    @Test void day151RejectsCannedRecommendationAgainstObservedEvidence() throws Exception {
        Path b=artifact("coverstory.release.candidate.jar");
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            var ds=pg.getPostgresDatabase();Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
            new JdbcTemplate(ds).update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES (?, ?, 'ISSUED', DATE '2026-12-01', 1)",POLICY,VERSION);
            String url=pg.getJdbcUrl("postgres","postgres");String before=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            String output=run(b,url,List.of("--coverstory.release.inspect","--coverstory.release.policy-id="+POLICY,"--coverstory.release.source-version="+VERSION));
            var frames=output.lines().filter(line->line.startsWith(InspectionReportCodec.PREFIX)).toList();assertEquals(1,frames.size());
            String falseSuccess=frames.getFirst().replace("ELIGIBLE_FOR_ONE_ATTEMPT","ALREADY_COMPLETE");
            assertNotEquals(frames.getFirst(),falseSuccess,"fixture must exercise a genuine misleading recommendation mutation");
            assertThrows(java.io.IOException.class,()->new assignments.delivery.recovery.InspectionReportCodec(new tools.jackson.databind.ObjectMapper())
                    .decodeSingleFrame(falseSuccess,new InspectionReport.RequestedKey(POLICY,VERSION)));
            var plausibleFalse=new InspectionReportCodec(new ObjectMapper()).decodeSingleFrame(
                    frames.getFirst().replace("2026-12-01","2026-12-02"),new InspectionReport.RequestedKey(POLICY,VERSION));
            assertThrows(AssertionError.class,()->assertReportMatchesIndependentDatabaseObservation(plausibleFalse,url,POLICY,VERSION),
                    "a structurally plausible report with a false observed date must fail against the database oracle");
            assertEquals(before,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),"bad report witness cannot alter actual evidence");
            var jdbc=new JdbcTemplate(pg.getPostgresDatabase());
            jdbc.update("INSERT INTO training_issuance_delivery_receipt(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES (?, ?, 'ISSUED', DATE '2026-12-01', 1)",POLICY,VERSION);
            String matchingBefore=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            var matching=reportFromPackagedB(b,url,POLICY,VERSION);
            assertReportMatchesIndependentDatabaseObservation(matching,url,POLICY,VERSION);
            assertEquals(assignments.delivery.recovery.ReceiptRelation.MATCHING,matching.receiptRelation());
            assertEquals(matchingBefore,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),"matching receipt without acknowledgment remains read-only");
            jdbc.update("UPDATE training_issuance_delivery_receipt SET effective_date=DATE '2026-12-02' WHERE policy_id=? AND source_version=?",POLICY,VERSION);
            String conflictingBefore=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            var conflicting=reportFromPackagedB(b,url,POLICY,VERSION);
            assertReportMatchesIndependentDatabaseObservation(conflicting,url,POLICY,VERSION);
            assertEquals(assignments.delivery.recovery.ReceiptRelation.CONFLICTING,conflicting.receiptRelation());
            assertEquals(conflictingBefore,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),"conflicting receipt observation remains read-only");
        }
    }

    @Test void day148RunsDistinctArtifactsAndReportCapabilityOnly() throws Exception {
        Path a=artifact("coverstory.release.baseline.jar"), b=artifact("coverstory.release.candidate.jar");
        JsonNode pair=validatedPairManifest(a,b);
        assertNotEquals(sha(a),sha(b),"A and B must be distinct JAR bytes");
        assertFalse(jarContains(a,"BOOT-INF/classes/assignments/delivery/recovery/ReleaseInspectionOnlyApplication.class"),
                "A must not contain B's report-only entry point");
        assertTrue(jarContains(b,"BOOT-INF/classes/assignments/delivery/recovery/ReleaseInspectionOnlyApplication.class"),
                "B's additive report capability must be present in the actual JAR");
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            var ds=pg.getPostgresDatabase(); Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
            new JdbcTemplate(ds).update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES (?, ?, 'ISSUED', DATE '2026-12-01', 1)",POLICY,VERSION);
            String url=pg.getJdbcUrl("postgres","postgres"); String before=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            assertReadOnly(a,url,"inspect"); assertEquals(before,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url));
            assertReadOnly(b,url,"inspect"); assertEquals(before,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url));
            String output=run(b,url,List.of("--coverstory.release.inspect","--coverstory.release.policy-id="+POLICY,"--coverstory.release.source-version="+VERSION));
            var frames=output.lines().filter(s->s.startsWith(InspectionReportCodec.PREFIX)).toList(); assertEquals(1,frames.size());
            var report=new InspectionReportCodec(new ObjectMapper()).decodeSingleFrame(frames.getFirst(),new InspectionReport.RequestedKey(POLICY,VERSION));
            assertReportMatchesIndependentDatabaseObservation(report,url,POLICY,VERSION);
            assertEquals(before,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),"Day 148 read-only report preserves full state");
        }
    }

    @Test void day149RunsCrossVersionRetainedStateMatrix() throws Exception {
        Path a=artifact("coverstory.release.baseline.jar"), b=artifact("coverstory.release.candidate.jar");
        validatedPairManifest(a,b);
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            var ds=pg.getPostgresDatabase(); Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
            JdbcTemplate jdbc=new JdbcTemplate(ds);
            seedCompatibilityFixture(jdbc);
            String url=pg.getJdbcUrl("postgres","postgres");
            String sourceBefore=sourceSnapshot(url); String workBefore=workInvariantSnapshot(url);
            String targetImmutableBefore=targetImmutableWorkSnapshot(url), schemaBefore=schemaFingerprint(url);
            Map<String,String> historyBefore=historyRows(url), receiptsBefore=receiptRows(url);
            String original=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            run(b,url,action("quarantine",2,"MANUAL_HOLD"));
            String quarantined=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            assertNotEquals(original,quarantined); assertEquals(sourceBefore,sourceSnapshot(url));
            assertEquals(workBefore,workInvariantSnapshot(url)); assertEquals(targetImmutableBefore,targetImmutableWorkSnapshot(url));
            assertEquals(schemaBefore,schemaFingerprint(url)); assertControlState(url,3,true,"MANUAL_HOLD",false);
            Map<String,String> historyAfterQuarantine=historyRows(url);
            assertHistoryAppend(historyBefore,historyAfterQuarantine,POLICY,VERSION,3,"QUARANTINE","MANUAL_HOLD",1);
            assertEquals(receiptsBefore,receiptRows(url));
            assertTrue(run(a,url,action("inspect",null,null)).contains("MANUAL_HOLD"));
            assertEquals(quarantined,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),"A legacy inspect is read-only on B-retained state");
            run(a,url,action("release",3,"REVIEWED_FOR_RETRY"));
            String released=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            assertControlState(url,4,false,null,false); assertEquals(sourceBefore,sourceSnapshot(url));
            assertEquals(workBefore,workInvariantSnapshot(url)); assertEquals(targetImmutableBefore,targetImmutableWorkSnapshot(url));
            assertEquals(schemaBefore,schemaFingerprint(url));
            assertHistoryAppend(historyAfterQuarantine,historyRows(url),POLICY,VERSION,4,"RELEASE","REVIEWED_FOR_RETRY",1);
            Map<String,String> historyAfterRelease=historyRows(url);
            assertEquals(receiptsBefore,receiptRows(url));
            run(b,url,action("retry",4,null));
            String completed=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url); String receipt=receiptSnapshot(url);
            assertNotEquals(released,completed); assertEquals(sourceBefore,sourceSnapshot(url));
            assertEquals(workBefore,workInvariantSnapshot(url)); assertEquals(targetImmutableBefore,targetImmutableWorkSnapshot(url));
            assertControlState(url,4,false,null,true); assertReceiptAppend(receiptsBefore,receiptRows(url),POLICY,VERSION);
            assertEquals(historyAfterRelease,historyRows(url));
            assertEquals(schemaBefore,schemaFingerprint(url),"B retry preserves the full pre-matrix schema and Flyway history identity");
            String beforeReplay=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            String aCompletedObservation=run(a,url,action("inspect",null,null));
            assertTrue(aCompletedObservation.contains("acknowledgedAt=")
                    && aCompletedObservation.contains("receiptRelation=MATCHING"),
                    "A's original inspection contract must observe the matching acknowledged receipt");
            assertEquals(beforeReplay,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url));
            run(a,url,action("retry",4,null)); assertEquals(beforeReplay,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url));
            assertEquals(receipt,receiptSnapshot(url),"A replay does not replace receipt identity or payload");
        }
    }

    @Test void day153RunsCompleteObservedPairMatrixAndFallbackDecision() throws Exception {
        successfulOwnedProcessRuns.set(0);
        Path a = artifact("coverstory.release.baseline.jar");
        Path b = artifact("coverstory.release.candidate.jar");
        JsonNode pair=validatedPairManifest(a,b);
        String aHash = sha(a), bHash = sha(b);
        assertNotEquals(aHash, bHash, "baseline and candidate must be distinct actual JAR bytes");
        String recipe=pair.path("build_recipe").asText();
        String aSource=pair.path("baseline").path("source_commit").asText();
        String bSource=pair.path("candidate").path("source_commit").asText();
        String aBuild=recipe+"; overlay="+pair.path("baseline").path("overlay").toString();
        String bBuild=recipe+"; overlay="+pair.path("candidate").path("overlay").toString();
        String verifiedSchemaFingerprint="";
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            var ds = pg.getPostgresDatabase();
            Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
            JdbcTemplate jdbc = new JdbcTemplate(ds);
            String url = pg.getJdbcUrl("postgres", "postgres");
            String schemaBeforeMatrix=schemaFingerprint(url);
            seedCompatibilityFixture(jdbc);
            String start = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            String sourceBefore = sourceSnapshot(url);
            String workBefore=workInvariantSnapshot(url);
            String targetImmutableBefore=targetImmutableWorkSnapshot(url);
            Map<String,String> historyBefore=historyRows(url), receiptsBefore=receiptRows(url);
            String aLegacyOutput = assertReadOnly(a, url, "inspect");
            String afterAInspect = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            assertEquals(start, afterAInspect);
            String bLegacyOutput = assertReadOnly(b, url, "inspect");
            assertEquals(start, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url));
            boolean legacyMatrixPassed = aLegacyOutput.contains(POLICY)
                    && bLegacyOutput.contains(POLICY)
                    && aLegacyOutput.contains("receiptRelation=ABSENT")
                    && bLegacyOutput.contains("receiptRelation=ABSENT");
            assertTrue(legacyMatrixPassed, "both actual artifacts completed their legacy inspect path");
            String reportOutput = run(b, url, List.of("--coverstory.release.inspect",
                    "--coverstory.release.policy-id=" + POLICY,
                    "--coverstory.release.source-version=" + VERSION));
            var framed = reportOutput.lines().filter(s -> s.startsWith(InspectionReportCodec.PREFIX)).toList();
            assertEquals(1, framed.size(), "B emits exactly one framed report");
            InspectionReport report = new InspectionReportCodec(new ObjectMapper()).decodeSingleFrame(
                    framed.getFirst(), new InspectionReport.RequestedKey(POLICY, VERSION));
            assertReportMatchesIndependentDatabaseObservation(report, url, POLICY, VERSION);
            assertEquals(start, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),
                    "B report startup and inspection must be read-only");
            boolean reportModePassed = report.schemaVersion() == 1
                    && report.key().policyId().equals(POLICY)
                    && report.key().sourceVersion() == VERSION
                    && report.recommendation() != null;
            assertTrue(reportModePassed, "B's parsed report is bound to the independent database oracle");

            run(b, url, action("quarantine", 2, "MANUAL_HOLD"));
            String quarantined=DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            assertEquals(sourceBefore,sourceSnapshot(url),"B quarantine cannot rewrite journeys, quotes, selections, lifecycle or idempotency rows");
            assertEquals(workBefore,workInvariantSnapshot(url),"only target control fields may change; all unrelated work rows remain exact");
            assertEquals(targetImmutableBefore,targetImmutableWorkSnapshot(url),"target source envelope and capture identity remain exact");
            assertEquals(schemaBeforeMatrix,schemaFingerprint(url),"schema identity matches the pre-matrix measurement");
            assertControlState(url,3,true,"MANUAL_HOLD",false);
            Map<String,String> historyAfterQuarantine=historyRows(url);
            assertHistoryAppend(historyBefore,historyAfterQuarantine,POLICY,VERSION,3,"QUARANTINE","MANUAL_HOLD",1);
            assertEquals(receiptsBefore,receiptRows(url),"quarantine preserves all prior receipts");
            assertEquals("MANUAL_HOLD",scalarText(url,"SELECT quarantine_reason FROM training_issuance_work WHERE policy_id='policy-demo-1'"));

            // The fallback decision is evaluated on the live fixture immediately before A resumes.
            // Evidence inputs below come from actual process exits, parsed report, hashes and DB snapshots.
            String schemaBeforeResume=schemaFingerprint(url);
            verifiedSchemaFingerprint=schemaBeforeResume;
            assertEquals(schemaBeforeMatrix,schemaBeforeResume,"schema identity remains equal to its pre-matrix value");
            String fixtureIdentity="embedded-postgres:"+url;
            assertEquals(aHash,sha(a),"A bytes remain the tested immutable artifact");
            assertEquals(bHash,sha(b),"B bytes remain the tested immutable artifact");
            boolean retainedStateVerified = quarantined.equals(DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url))
                    && sourceBefore.equals(sourceSnapshot(url))
                    && workBefore.equals(workInvariantSnapshot(url))
                    && targetImmutableBefore.equals(targetImmutableWorkSnapshot(url))
                    && historyRows(url).size()==historyBefore.size()+1
                    && receiptsBefore.equals(receiptRows(url));
            assertTrue(retainedStateVerified, "the expected quarantined state is still present on the live fixture");
            // BoundedProcessRunner.run() returns only after exit, timeout, output, and owned-process
            // cleanup checks have succeeded. Count completed invocations from this thread.
            boolean cleanupPassed = successfulOwnedProcessRuns.get() >= 3;
            assertTrue(cleanupPassed, "all pre-fallback child processes returned after owned cleanup");
            var evidence=new ReleaseArtifactPair(
                    new ReleaseArtifactPair.ArtifactIdentity(aHash,aSource,"legacy-inspect",aBuild,pair.path("baseline").path("overlay").size()>0),
                    new ReleaseArtifactPair.ArtifactIdentity(bHash,bSource,"legacy-inspect+report-v1",bBuild,pair.path("candidate").path("overlay").size()>0),
                    fixtureIdentity,schemaBeforeResume,legacyMatrixPassed,reportModePassed,
                    retainedStateVerified,cleanupPassed);
            new ReleaseArtifactPairPolicy().validate(evidence);
            assertEquals(BinaryFallbackPolicy.FallbackDecision.ALLOW_TESTED_PAIR,
                    new BinaryFallbackPolicy().decide(evidence,sha(a),sha(b),"embedded-postgres:"+url,schemaFingerprint(url)),
                    "fallback authorization is checked against current artifact, fixture and schema identity before invoking A");
            assertEquals(BinaryFallbackPolicy.FallbackDecision.BLOCKED,
                    new BinaryFallbackPolicy().decide(evidence,sha(a),sha(b),"embedded-postgres:"+url,"stale-schema-fingerprint"),
                    "fallback refuses when the freshly measured current schema differs from the tested schema");
            assertTrue(run(a, url, action("inspect", null, null)).contains("MANUAL_HOLD"));
            assertEquals(quarantined,DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),"A inspection must preserve all rows");
            run(a, url, action("release", 3, "REVIEWED_FOR_RETRY"));
            String released = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            assertControlState(url,4,false,null,false);
            assertEquals(sourceBefore,sourceSnapshot(url)); assertEquals(workBefore,workInvariantSnapshot(url));
            assertEquals(targetImmutableBefore,targetImmutableWorkSnapshot(url));
            assertEquals(schemaBeforeMatrix,schemaFingerprint(url));
            assertHistoryAppend(historyAfterQuarantine,historyRows(url),POLICY,VERSION,4,"RELEASE","REVIEWED_FOR_RETRY",1);
            Map<String,String> historyAfterRelease=historyRows(url);
            assertEquals(receiptsBefore,receiptRows(url));
            run(b, url, action("retry", 4, null));
            String completed = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            String receiptAfterDelivery=receiptSnapshot(url);
            assertEquals(sourceBefore, sourceSnapshot(url), "source/lifecycle/idempotency data is retained");
            assertEquals(workBefore,workInvariantSnapshot(url)); assertEquals(targetImmutableBefore,targetImmutableWorkSnapshot(url));
            assertControlState(url,4,false,null,true);
            assertEquals(schemaBeforeMatrix,schemaFingerprint(url),"B retry preserves the complete initial schema and migration history");
            assertReceiptAppend(receiptsBefore,receiptRows(url),POLICY,VERSION);
            assertTrue(released.contains("REVIEWED_FOR_RETRY"));
            assertEquals(1, scalar(url, "SELECT count(*) FROM training_issuance_work WHERE policy_id='policy-demo-1' AND acknowledged_at IS NOT NULL"));
            assertEquals(workBefore,workInvariantSnapshot(url),"delivery/ack may change only selected-key acknowledgement/receipt evidence");
            assertEquals(targetImmutableBefore,targetImmutableWorkSnapshot(url));
            assertEquals(historyAfterRelease,historyRows(url),"retry cannot rewrite recovery history");
            assertNotEquals(released, completed, "receipt then acknowledgment are visible persisted changes");
            String beforeAReplay = DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url);
            run(a, url, action("inspect", null, null));
            assertEquals(beforeAReplay, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url));
            run(a, url, action("retry", 4, null));
            assertEquals(beforeAReplay, DatabaseRestoreAcceptanceTest.fullDatabaseSnapshot(url),
                    "A replays retained completion without replacing receipt or timestamps");
            assertEquals(receiptAfterDelivery,receiptSnapshot(url),"replay preserves original receipt payload/time");
            assertTrue(sha(a).equals(aHash) && sha(b).equals(bHash), "artifact bytes remain immutable during the matrix");
            assertEquals(schemaBeforeMatrix,schemaFingerprint(url),"schema and migration identity stay equal to the pre-matrix baseline");
        }
        System.out.println("TWO_ARTIFACT_MATRIX_PASSED A="+aHash+" B="+bHash+" sourceA="+aSource+" sourceB="+bSource+
                " buildA="+aBuild+" buildB="+bBuild+" schema="+verifiedSchemaFingerprint);
    }

    private static Path artifact(String property) {
        String value = System.getProperty(property, "");
        assertFalse(value.isBlank(), "missing separately built artifact property " + property);
        Path path = Path.of(value).toAbsolutePath();
        assertTrue(Files.isRegularFile(path), "artifact must exist: " + property);
        return path;
    }

    private static JsonNode validatedPairManifest(Path a,Path b) throws Exception {
        Path manifest=Path.of(System.getProperty("coverstory.release.pair.manifest", "" )).toAbsolutePath();
        assertTrue(Files.isRegularFile(manifest),"pair provenance manifest must be created by prepare-release-artifacts.sh");
        JsonNode root=new ObjectMapper().readTree(Files.readString(manifest));
        assertEquals(1,root.path("schema_version").asInt());
        assertEquals(sha(a),root.path("baseline").path("sha256").asText(),"A digest is bound to the manifest");
        assertEquals(sha(b),root.path("candidate").path("sha256").asText(),"B digest is bound to the manifest");
        assertEquals("105bbc9dd448460f9bd5f7e6b9c67eafa99a16c3",root.path("baseline").path("source_commit").asText());
        String candidateCommit=root.path("candidate").path("source_commit").asText();
        assertFalse(candidateCommit.isBlank());
        Process revision=new ProcessBuilder("git","rev-parse","HEAD").start();
        String currentCommit;
        try (var reader=revision.inputReader()) { currentCommit=reader.readLine(); }
        assertEquals(0,revision.waitFor(),"could not read candidate source revision");
        assertEquals(currentCommit,candidateCommit,"candidate provenance must identify the exact source commit used to build B");
        assertTrue(root.path("baseline").path("overlay").isArray());
        assertTrue(root.path("candidate").path("overlay").isArray());
        assertOverlayManifest(root.path("baseline").path("overlay"));
        assertOverlayManifest(root.path("candidate").path("overlay"));
        assertEquals(5,root.path("migration_sources").size(),"manifest records the unchanged V001-V005 source set");
        return root;
    }

    private static void assertOverlayManifest(JsonNode overlay) throws Exception {
        for (JsonNode entry : overlay) {
            Path source=Path.of(entry.path("source").asText()).toAbsolutePath();
            assertTrue(Files.isRegularFile(source),"overlay source must remain available for provenance validation: "+source);
            assertEquals(entry.path("sha256").asText(),sha(source),"overlay bytes must match their recorded digest");
            assertFalse(entry.path("destination").asText().isBlank(),"overlay destination is required");
        }
    }

    private static boolean jarContains(Path jar,String entry) throws Exception {
        try(var zip=new java.util.zip.ZipFile(jar.toFile())) { return zip.getEntry(entry)!=null; }
    }
    private static InspectionReport reportFromPackagedB(Path jar,String url,String policy,long version) throws Exception {
        String output=run(jar,url,List.of("--coverstory.release.inspect","--coverstory.release.policy-id="+policy,"--coverstory.release.source-version="+version));
        var frames=output.lines().filter(line->line.startsWith(InspectionReportCodec.PREFIX)).toList(); assertEquals(1,frames.size());
        return new InspectionReportCodec(new ObjectMapper()).decodeSingleFrame(frames.getFirst(),new InspectionReport.RequestedKey(policy,version));
    }

    private static List<String> action(String action, Integer revision, String reason) {
        var args = new java.util.ArrayList<>(List.of("--coverstory.recovery.action=" + action,
                "--coverstory.recovery.policy-id=" + POLICY, "--coverstory.recovery.source-version=" + VERSION));
        if (revision != null) args.add("--coverstory.recovery.revision=" + revision);
        if (reason != null) args.add("--coverstory.recovery.reason=" + reason);
        return args;
    }

    private static String assertReadOnly(Path jar, String url, String action) throws Exception {
        String out = run(jar, url, action(action, null, null));
        assertFalse(out.contains("release inspection failed"));
        return out;
    }

    private static String run(Path jar, String url, List<String> args) throws Exception {
        var result=runRaw(jar,url,args);
        assertFalse(result.timedOut(), "actual child command exceeded watchdog");
        assertFalse(result.outputOverflow(), "actual child command exceeded one MiB bound");
        assertEquals(0, result.exitCode(), "actual child command failed: " + result.capturedOutput());
        successfulOwnedProcessRuns.set(successfulOwnedProcessRuns.get()+1);
        return result.capturedOutput();
    }
    private static BoundedProcessRunner.Result runRaw(Path jar,String url,List<String> args) throws Exception {
        var cmd = new java.util.ArrayList<String>();
        cmd.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        cmd.add("-jar"); cmd.add(jar.toString()); cmd.addAll(args);
        var env=java.util.Map.of("SPRING_PROFILES_ACTIVE","local","COVERSTORY_DB_URL",url,
                "COVERSTORY_DB_USER","postgres","COVERSTORY_DB_PASSWORD","");
        return new BoundedProcessRunner().run(cmd,env,java.time.Duration.ofSeconds(30), 1024 * 1024);
    }

    private static String sourceSnapshot(String url) throws Exception {
        try (Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement();
             ResultSet r=s.executeQuery("SELECT coalesce((SELECT jsonb_agg(to_jsonb(x) ORDER BY journey_id)::text FROM journeys x),'[]')||'|'||coalesce((SELECT jsonb_agg(to_jsonb(x) ORDER BY journey_id,quote_version_id)::text FROM quote_versions x),'[]')||'|'||coalesce((SELECT jsonb_agg(to_jsonb(x) ORDER BY journey_id,selection_order)::text FROM selections x),'[]')||'|'||coalesce((SELECT jsonb_agg(to_jsonb(x) ORDER BY policy_id)::text FROM training_policy_lifecycle x),'[]')||'|'||coalesce((SELECT jsonb_agg(to_jsonb(x) ORDER BY policy_id,version)::text FROM training_policy_lifecycle_event x),'[]')||'|'||coalesce((SELECT jsonb_agg(to_jsonb(x) ORDER BY owner_id,client_request_id)::text FROM training_policy_idempotency x),'[]')")) {
            assertTrue(r.next()); return r.getString(1);
        }
    }
    private static void seedCompatibilityFixture(JdbcTemplate jdbc) {
        jdbc.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version,captured_at,control_revision) VALUES ('policy-demo-1',1,'ISSUED',DATE '2026-12-01',1,TIMESTAMPTZ '2026-10-01 10:02:00+00',2)");
        jdbc.update("INSERT INTO training_issuance_recovery_history(policy_id,source_version,control_revision,action,reason_code,decided_at) VALUES ('policy-demo-1',1,1,'QUARANTINE','OBSERVED_FAILURE',TIMESTAMPTZ '2026-10-01 12:10:00+00'),('policy-demo-1',1,2,'RELEASE','REVIEWED_FOR_RETRY',TIMESTAMPTZ '2026-10-01 12:20:00+00')");
        jdbc.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version,captured_at,acknowledged_at,quarantined_at,quarantine_reason,control_revision) VALUES ('policy-renewal-a',1,'ISSUED',DATE '2026-01-15',1,TIMESTAMPTZ '2026-10-01 10:05:00+00',TIMESTAMPTZ '2026-10-01 12:30:00+00',TIMESTAMPTZ '2026-10-01 12:25:00+00','MANUAL_HOLD',1)");
        jdbc.update("INSERT INTO training_issuance_delivery_receipt(policy_id,source_version,event_type,effective_date,envelope_schema_version,received_at) VALUES ('policy-renewal-a',1,'ISSUED',DATE '2026-01-15',1,TIMESTAMPTZ '2026-10-01 12:31:00+00')");
        jdbc.update("INSERT INTO training_issuance_recovery_history(policy_id,source_version,control_revision,action,reason_code,decided_at) VALUES ('policy-renewal-a',1,1,'QUARANTINE','MANUAL_HOLD',TIMESTAMPTZ '2026-10-01 12:25:00+00')");
    }
    private static String workInvariantSnapshot(String url) throws Exception {
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery(
                "SELECT coalesce(jsonb_agg(CASE WHEN policy_id='policy-demo-1' AND source_version=1 THEN to_jsonb(w)-'quarantined_at'-'quarantine_reason'-'control_revision'-'acknowledged_at' ELSE to_jsonb(w) END ORDER BY policy_id,source_version),'[]'::jsonb)::text FROM training_issuance_work w")) {
            assertTrue(r.next()); return r.getString(1);
        }
    }
    private static String targetImmutableWorkSnapshot(String url) throws Exception {
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery(
                "SELECT (to_jsonb(w)-'quarantined_at'-'quarantine_reason'-'control_revision'-'acknowledged_at')::text FROM training_issuance_work w WHERE policy_id='policy-demo-1' AND source_version=1")) {
            assertTrue(r.next()); return r.getString(1);
        }
    }
    private static void assertControlState(String url,int revision,boolean quarantined,String reason,boolean acknowledged) throws Exception {
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery(
                "SELECT control_revision,quarantined_at,quarantine_reason,acknowledged_at FROM training_issuance_work WHERE policy_id='policy-demo-1' AND source_version=1")) {
            assertTrue(r.next()); assertEquals(revision,r.getInt(1)); assertEquals(quarantined,r.getTimestamp(2)!=null);
            assertEquals(reason,r.getString(3)); assertEquals(acknowledged,r.getTimestamp(4)!=null);
        }
    }
    private static Map<String,String> historyRows(String url) throws Exception {
        var rows=new java.util.TreeMap<String,String>();
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery(
                "SELECT policy_id,source_version,control_revision,action,reason_code,decided_at FROM training_issuance_recovery_history ORDER BY policy_id,source_version,control_revision")) {
            while(r.next()) rows.put(r.getString(1)+"|"+r.getLong(2)+"|"+r.getInt(3),r.getString(4)+"|"+r.getString(5)+"|"+r.getTimestamp(6).toInstant());
        }
        return rows;
    }
    private static void assertHistoryAppend(Map<String,String> before,Map<String,String> after,String policy,long version,int revision,
            String action,String reason,int additions) {
        assertEquals(before.size()+additions,after.size(),"only the intended recovery-history append rows may be added");
        before.forEach((key,value)->assertEquals(value,after.get(key),"preexisting history row was rewritten: "+key));
        String key=policy+"|"+version+"|"+revision;
        assertTrue(after.containsKey(key),"expected history row is present: "+key);
        assertTrue(after.get(key).startsWith(action+"|"+reason+"|"),"history row has the exact action and reason");
    }
    private static Map<String,String> receiptRows(String url) throws Exception {
        var rows=new java.util.TreeMap<String,String>();
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery(
                "SELECT policy_id,source_version,event_type,effective_date,envelope_schema_version,received_at FROM training_issuance_delivery_receipt ORDER BY policy_id,source_version")) {
            while(r.next()) rows.put(r.getString(1)+"|"+r.getLong(2),r.getString(3)+"|"+r.getDate(4)+"|"+r.getInt(5)+"|"+r.getTimestamp(6).toInstant());
        }
        return rows;
    }
    private static void assertReceiptAppend(Map<String,String> before,Map<String,String> after,String policy,long version) {
        assertEquals(before.size()+1,after.size(),"retry appends exactly one receipt row");
        before.forEach((key,value)->assertEquals(value,after.get(key),"preexisting receipt was rewritten: "+key));
        assertTrue(after.containsKey(policy+"|"+version),"target receipt was created");
        assertTrue(after.get(policy+"|"+version).startsWith("ISSUED|2026-12-01|1|"));
    }
    private static String receiptSnapshot(String url) throws Exception {
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery(
                "SELECT coalesce(jsonb_agg(to_jsonb(x) ORDER BY policy_id,source_version),'[]'::jsonb)::text FROM training_issuance_delivery_receipt x")) {
            assertTrue(r.next()); return r.getString(1);
        }
    }
    private static int scalar(String url, String sql) throws Exception {
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) {
            assertTrue(r.next()); return r.getInt(1);
        }
    }
    private static String scalarText(String url,String sql) throws Exception {
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) { assertTrue(r.next()); return r.getString(1); }
    }
    private static void assertReportMatchesIndependentDatabaseObservation(InspectionReport report, String url,
            String policyId, long version) throws Exception {
        assertEquals(1, report.schemaVersion());
        assertEquals(policyId, report.key().policyId());
        assertEquals(version, report.key().sourceVersion());
        var key=new assignments.delivery.IssuanceSourceKey(policyId,version);
        boolean present=false; String type=null; java.time.LocalDate date=null; int envelopeSchema=0,revision=0;
        java.time.Instant ack=null,quarantined=null; assignments.delivery.recovery.RecoveryReason reason=null;
        try(Connection c=DriverManager.getConnection(url,"postgres",""); var ps=c.prepareStatement("SELECT event_type,effective_date,envelope_schema_version,control_revision,acknowledged_at,quarantined_at,quarantine_reason FROM training_issuance_work WHERE policy_id=? AND source_version=?")) {
            ps.setString(1,policyId); ps.setLong(2,version);
            try(ResultSet r=ps.executeQuery()) { if(r.next()) { present=true; type=r.getString("event_type"); date=r.getDate("effective_date").toLocalDate(); envelopeSchema=r.getInt("envelope_schema_version"); revision=r.getInt("control_revision");
                var ackValue=r.getTimestamp("acknowledged_at"); ack=ackValue==null?null:ackValue.toInstant(); var quarantineValue=r.getTimestamp("quarantined_at"); quarantined=quarantineValue==null?null:quarantineValue.toInstant();
                String rawReason=r.getString("quarantine_reason"); reason=rawReason==null?null:assignments.delivery.recovery.RecoveryReason.valueOf(rawReason); } }
        }
        String relation="ABSENT";
        try(Connection c=DriverManager.getConnection(url,"postgres",""); var ps=c.prepareStatement("SELECT w.event_type,w.effective_date,w.envelope_schema_version,r.event_type AS receipt_type,r.effective_date AS receipt_date,r.envelope_schema_version AS receipt_schema FROM training_issuance_work w LEFT JOIN training_issuance_delivery_receipt r USING(policy_id,source_version) WHERE w.policy_id=? AND w.source_version=?")) {
            ps.setString(1,policyId); ps.setLong(2,version);
            try(ResultSet r=ps.executeQuery()) { if(r.next()&&r.getString("receipt_type")!=null) relation=r.getString("event_type").equals(r.getString("receipt_type"))&&r.getDate("effective_date").equals(r.getDate("receipt_date"))&&r.getInt("envelope_schema_version")==r.getInt("receipt_schema")?"MATCHING":"CONFLICTING"; }
        }
        var receiptRelation=assignments.delivery.recovery.ReceiptRelation.valueOf(relation);
        var envelope=present?new assignments.delivery.IssuanceEnvelope(policyId,version,type,date,envelopeSchema):null;
        var observation=new assignments.delivery.recovery.RecoveryObservation(key,present,envelope,revision,ack,quarantined,reason,receiptRelation);
        assertEquals(present,report.workPresent());
        assertEquals(envelope==null?null:envelope.policyId(),report.envelope()==null?null:report.envelope().policyId());
        assertEquals(envelope==null?null:envelope.sourceVersion(),report.envelope()==null?null:report.envelope().sourceVersion());
        assertEquals(envelope==null?null:envelope.eventType(),report.envelope()==null?null:report.envelope().eventType());
        assertEquals(envelope==null?null:envelope.effectiveDate(),report.envelope()==null?null:report.envelope().effectiveDate());
        assertEquals(envelope==null?null:envelope.schemaVersion(),report.envelope()==null?null:report.envelope().schemaVersion());
        assertEquals(revision,report.controlRevision()); assertEquals(ack,report.acknowledgedAt()); assertEquals(quarantined,report.quarantinedAt());
        assertEquals(reason,report.quarantineReason()); assertEquals(receiptRelation,report.receiptRelation());
        var expectedHistory=new java.util.ArrayList<assignments.delivery.recovery.RecoveryHistoryEntry>();
        try(Connection c=DriverManager.getConnection(url,"postgres",""); var ps=c.prepareStatement("SELECT control_revision,action,reason_code,decided_at FROM training_issuance_recovery_history WHERE policy_id=? AND source_version=? ORDER BY control_revision DESC LIMIT 20")) {
            ps.setString(1,policyId); ps.setLong(2,version); try(ResultSet r=ps.executeQuery()) { while(r.next()) expectedHistory.add(new assignments.delivery.recovery.RecoveryHistoryEntry(r.getInt(1),assignments.delivery.recovery.RecoveryAction.valueOf(r.getString(2)),assignments.delivery.recovery.RecoveryReason.valueOf(r.getString(3)),r.getTimestamp(4).toInstant())); }
        }
        java.util.Collections.reverse(expectedHistory);
        assertEquals(expectedHistory.size(),report.history().size());
        for(int i=0;i<expectedHistory.size();i++) { var x=expectedHistory.get(i); var y=report.history().get(i); assertEquals(x.controlRevision(),y.controlRevision()); assertEquals(x.action(),y.action()); assertEquals(x.reason(),y.reason()); assertEquals(x.decidedAt(),y.decidedAt()); }
        assertTrue(report.historySupplementary());
        assertEquals(new assignments.delivery.recovery.RecoveryDecisionPolicy().classify(observation).recommendation(),report.recommendation());
    }
    private static String sha(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
    private static String schemaFingerprint(String url) throws Exception {
        StringBuilder identity=new StringBuilder();
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT c.relname,a.attname,pg_catalog.format_type(a.atttypid,a.atttypmod),a.attnotnull,coalesce(pg_get_expr(d.adbin,d.adrelid),'<none>') FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid=c.relnamespace JOIN pg_catalog.pg_attribute a ON a.attrelid=c.oid LEFT JOIN pg_catalog.pg_attrdef d ON d.adrelid=c.oid AND d.adnum=a.attnum WHERE n.nspname='public' AND c.relkind IN ('r','p') AND a.attnum>0 AND NOT a.attisdropped ORDER BY c.relname,a.attnum")) {
            while(r.next()) identity.append("column|").append(r.getString(1)).append('.').append(r.getString(2)).append(':').append(r.getString(3)).append('|').append(r.getBoolean(4)).append('|').append(r.getString(5)).append('\n');
        }
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT conrelid::regclass::text,conname,pg_get_constraintdef(oid),convalidated,condeferrable,condeferred FROM pg_catalog.pg_constraint WHERE connamespace='public'::regnamespace ORDER BY conrelid::regclass::text,conname")) {
            while(r.next()) identity.append("constraint|").append(r.getString(1)).append('|').append(r.getString(2)).append('|').append(r.getString(3)).append('|').append(r.getBoolean(4)).append('|').append(r.getBoolean(5)).append('|').append(r.getBoolean(6)).append('\n');
        }
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT tablename,indexname,indexdef FROM pg_catalog.pg_indexes WHERE schemaname='public' ORDER BY tablename,indexname")) {
            while(r.next()) identity.append("index|").append(r.getString(1)).append('|').append(r.getString(2)).append('|').append(r.getString(3)).append('\n');
        }
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT tgrelid::regclass::text,tgname,pg_get_triggerdef(oid),tgenabled FROM pg_catalog.pg_trigger WHERE NOT tgisinternal ORDER BY tgrelid::regclass::text,tgname")) {
            while(r.next()) identity.append("trigger|").append(r.getString(1)).append('|').append(r.getString(2)).append('|').append(r.getString(3)).append('|').append(r.getString(4)).append('\n');
        }
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT schemaname,tablename,rulename,definition FROM pg_catalog.pg_rules WHERE schemaname='public' ORDER BY tablename,rulename")) {
            while(r.next()) identity.append("rule|").append(r.getString(1)).append('|').append(r.getString(2)).append('|').append(r.getString(3)).append('|').append(r.getString(4)).append('\n');
        }
        try(Connection c=DriverManager.getConnection(url,"postgres",""); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT installed_rank,version,description,type,script,checksum,installed_by,installed_on,execution_time,success FROM flyway_schema_history ORDER BY installed_rank")) {
            while(r.next()) identity.append("flyway|").append(r.getInt(1)).append('|').append(r.getString(2)).append('|').append(r.getString(3)).append('|').append(r.getString(4)).append('|').append(r.getString(5)).append('|').append(r.getString(6)).append('|').append(r.getString(7)).append('|').append(r.getTimestamp(8)).append('|').append(r.getInt(9)).append('|').append(r.getBoolean(10)).append('\n');
        }
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(identity.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
}
