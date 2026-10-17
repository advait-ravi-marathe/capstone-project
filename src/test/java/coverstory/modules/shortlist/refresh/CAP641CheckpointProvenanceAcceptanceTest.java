package coverstory.modules.shortlist.refresh;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

/** CAP-641 learner migration acceptance. It starts from the previous schema and retained synthetic history. */
@Tag("learnerAcceptance")
class CAP641CheckpointProvenanceAcceptanceTest {
    @Test void expandsMetadataWithoutGuessingHistoricalRowsOrChangingReferences() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection()) {
            try(Statement st=c.createStatement()) {
                st.execute("CREATE SCHEMA shortlist");
                try(InputStream in=getClass().getResourceAsStream("/db/schema/shortlist_checkpoint_refresh.sql")) { assertNotNull(in);st.execute(new String(in.readAllBytes(),StandardCharsets.UTF_8)); }
                st.execute("INSERT INTO shortlist.checkpoint_revision(owner_id,shortlist_id,revision,parent_revision,checkpoint_id,scenario_id,slot_refs,source_digest_set_sha256) VALUES ('00000000-0000-4000-8000-000000000001','00000000-0000-4000-8000-000000000011',1,NULL,'00000000-0000-4000-8000-000000000021','scenario-health-001','[{\"slotId\":\"s1\",\"productId\":\"00000000-0000-4000-8000-000000000101\",\"versionId\":\"v1\",\"sourceSha256\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"}]',decode(repeat('a',64),'hex'))");
            }
            String before;
            try(Statement st=c.createStatement();ResultSet rs=st.executeQuery("SELECT encode(source_digest_set_sha256,'hex') FROM shortlist.checkpoint_revision")){assertTrue(rs.next());before=rs.getString(1);}
            String migration;
            try(InputStream in=getClass().getResourceAsStream("/db/schema/shortlist_checkpoint_provenance_expand.sql")) { assertNotNull(in);migration=new String(in.readAllBytes(),StandardCharsets.UTF_8).replaceAll("(?m)^\\s*--.*$","").trim(); }
            assertFalse(migration.isBlank(),"TODO(CAP-641): implement the additive provenance migration before historical compatibility can pass");
            try(Statement st=c.createStatement()){for(String sql:migration.split(";"))if(!sql.isBlank())st.execute(sql);}
            try(Statement st=c.createStatement();ResultSet rs=st.executeQuery("SELECT provenance_code,encode(source_digest_set_sha256,'hex') FROM shortlist.checkpoint_revision")){
                assertTrue(rs.next());assertNull(rs.getString(1),"retained historical rows have no generated provenance evidence");assertEquals(before,rs.getString(2));
            }
        }
    }
}
