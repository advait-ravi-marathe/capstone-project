package capstone.coverstory.application;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class InsurerReviewEventReducerTest {
    private static final UUID OWNER=UUID.fromString("11200000-0000-4000-8000-000000000001");
    private static final UUID FOREIGN=UUID.fromString("11200000-0000-4000-8000-000000000003");
    private static final UUID APP=UUID.fromString("11200000-0000-4000-8000-000000000002");
    private static final String SECRET="SYNTHETIC_PRIVATE_ANSWER_SENTINEL";

    @Test void duplicateAndOlderProviderEventsAreDurableButNeverRegressState() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            DriverManagerDataSource ds=db(pg); seed(ds);
            assertEquals("APPLIED",CAP112ReferenceReducer.apply(ds,OWNER,APP,"rv1",2,"REVIEW",UUID.randomUUID(),5,null));
            assertEquals("DUPLICATE",CAP112ReferenceReducer.apply(ds,OWNER,APP,"rv1",2,"REVIEW",UUID.randomUUID(),6,null));
            assertEquals("STALE_RECORDED",CAP112ReferenceReducer.apply(ds,OWNER,APP,"rv0",1,"NEEDS_INFORMATION",UUID.randomUUID(),7,null));
            assertEquals("REVIEW",query(ds,"SELECT status FROM cap112_harness.application_review_state"));
            assertEquals("2",query(ds,"SELECT version::text FROM cap112_harness.application_review_state"));
            assertEquals(2,count(ds,"source_event")); assertEquals(1,count(ds,"review_timeline"));
            assertEquals(1,countWhere(ds,"safe_diagnostic","outcome='DUPLICATE'"));
            assertEquals(1,countWhere(ds,"safe_diagnostic","outcome='STALE_RECORDED'"));
        }
    }

    @Test void ownerMismatchFailsWithoutRecordingAnotherOwnersEvent() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) { DriverManagerDataSource ds=db(pg);seed(ds);
            assertEquals("OWNER_NOT_FOUND",CAP112ReferenceReducer.apply(ds,FOREIGN,APP,"foreign-rv",2,"REVIEW",UUID.randomUUID(),4,null));
            assertEquals(0,count(ds,"source_event"));assertEquals(1,count(ds,"safe_diagnostic")); }
    }

    @Test void providerExceptionIsReducedToAllowlistedDiagnostic() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) { DriverManagerDataSource ds=db(pg);seed(ds);
            assertEquals("PROVIDER_FAILURE",CAP112ReferenceReducer.apply(ds,OWNER,APP,"rv-secret",2,"REVIEW",UUID.randomUUID(),9,
                    new IllegalStateException("provider echoed "+SECRET+"\\nprivate answer")));
            String stored=databaseText(ds);
            assertFalse(stored.contains(SECRET));assertFalse(stored.contains("private answer"));assertEquals(0,count(ds,"source_event"));
        }
    }

    @Test void fixtureAndSchemaContainNoRawProviderPayloadColumns() throws Exception {
        var fixture=new tools.jackson.databind.ObjectMapper().readTree(Files.readString(Path.of("docs/coverstory/fixtures/cap-112.json")));
        assertEquals("SYNTHETIC_ONLY_NO_NETWORK",fixture.path("classification").asText());
        assertTrue(fixture.toString().contains(SECRET), "the synthetic sentinel must be present to test its removal from output sinks");
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()){DriverManagerDataSource ds=db(pg);seed(ds);
            assertEquals(java.util.Set.of("request_id","route_code","outcome","duration_millis","received_at"),schemaColumns(ds,"safe_diagnostic"));
        }
    }

    private static DriverManagerDataSource db(EmbeddedPostgres pg)throws Exception {
        String url,user;try(Connection c=pg.getPostgresDatabase().getConnection()){url=c.getMetaData().getURL();user=c.getMetaData().getUserName();}
        return new DriverManagerDataSource(url,user,"");
    }
    private static void seed(DriverManagerDataSource ds)throws Exception{try(Connection c=ds.getConnection();Statement s=c.createStatement()){s.execute("CREATE SCHEMA cap112_harness");s.execute(Files.readString(Path.of("src/test/resources/coverstory/cap_112_review.sql")));s.execute("INSERT INTO cap112_harness.application_review_state VALUES ('"+APP+"','"+OWNER+"','SUBMITTED',1)");}}
    private static int count(DriverManagerDataSource ds,String table)throws Exception{return countWhere(ds,table,"true");}
    private static int countWhere(DriverManagerDataSource ds,String table,String where)throws Exception{try(Connection c=ds.getConnection();Statement s=c.createStatement();var r=s.executeQuery("SELECT count(*) FROM cap112_harness."+table+" WHERE "+where)){r.next();return r.getInt(1);}}
    private static String query(DriverManagerDataSource ds,String sql)throws Exception{try(Connection c=ds.getConnection();Statement s=c.createStatement();var r=s.executeQuery(sql)){r.next();return r.getString(1);}}
    private static String databaseText(DriverManagerDataSource ds)throws Exception{
        return query(ds,"SELECT coalesce(string_agg(value,'|'),'') FROM ("+
                "SELECT status AS value FROM cap112_harness.application_review_state UNION ALL "+
                "SELECT event_id||'|'||received_status||'|'||outcome FROM cap112_harness.source_event UNION ALL "+
                "SELECT status||'|'||source_event_id FROM cap112_harness.review_timeline UNION ALL "+
                "SELECT route_code||'|'||outcome FROM cap112_harness.safe_diagnostic) all_rows");
    }
    private static java.util.Set<String> schemaColumns(DriverManagerDataSource ds,String table)throws Exception{try(Connection c=ds.getConnection();Statement s=c.createStatement();var r=s.executeQuery("SELECT column_name FROM information_schema.columns WHERE table_schema='cap112_harness' AND table_name='"+table+"'")){var out=new java.util.HashSet<String>();while(r.next())out.add(r.getString(1));return out;}}
}
