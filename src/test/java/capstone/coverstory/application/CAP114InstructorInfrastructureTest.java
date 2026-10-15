package capstone.coverstory.application;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Instructor fixtures/oracle checks; learner implementation remains in CAP114AcceptanceTest. */
class CAP114InstructorInfrastructureTest {
    @Test void ruleFixtureAndPersistedOracleCoverThresholdPersistenceRecoveryAndNeedsInformation() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection()) {
            var ds=new DriverManagerDataSource(c.getMetaData().getURL(),c.getMetaData().getUserName(),"");
            new ResourceDatabasePopulator(new ClassPathResource("/coverstory/cap_114_review_alert.sql")).execute(ds);
            var oracle=new CAP114ReferenceAlertWindow(ds); JdbcTemplate jdbc=new JdbcTemplate(ds);
            assertEquals("PENDING",oracle.record("i-1",100,6,1).alertState());
            assertEquals("PENDING",oracle.record("i-2",100,6,0).alertState());
            var firing=oracle.record("i-3",100,6,0);
            assertEquals("FIRING",firing.alertState()); assertEquals(1,firing.firingTransitions());
            assertEquals("FIRING",oracle.record("i-4",100,7,0).alertState());
            assertEquals(1,oracle.snapshot().firingTransitions());
            assertEquals("RESOLVED",oracle.record("healthy",100,0,0).alertState());
            assertEquals(1,oracle.snapshot().resolutionTransitions());
            assertEquals(3,jdbc.queryForObject("SELECT count(*) FROM cap114_harness.alert_transition",Integer.class));
            assertThrows(IllegalArgumentException.class,()->oracle.record("healthy",100,5,0),"reused id with altered payload is rejected");

            oracle.reset();
            assertEquals("NORMAL",oracle.record("boundary-1",100,5,0).alertState());
            assertEquals("NORMAL",oracle.record("boundary-2",100,5,0).alertState());
            assertEquals("NORMAL",oracle.record("boundary-3",100,5,0).alertState());
            assertEquals("NORMAL",oracle.record("low-volume",2,1,1).alertState());
            assertEquals("NORMAL",oracle.record("healthy-after-low",100,0,0).alertState());
            assertEquals(0,oracle.snapshot().firingTransitions());
            assertEquals(1,oracle.snapshot().needsInformationCount());
            assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM cap114_harness.alert_transition",Integer.class));
        }
    }

    @Test void schemaRejectsInvalidCountsAndArbitraryWindowIdentifiers() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection();var s=c.createStatement()) {
            new ResourceDatabasePopulator(new ClassPathResource("/coverstory/cap_114_review_alert.sql")).execute(new DriverManagerDataSource(c.getMetaData().getURL(),c.getMetaData().getUserName(),""));
            assertThrows(java.sql.SQLException.class,()->s.execute("INSERT INTO cap114_harness.window_sample VALUES('bad/url',100,6,0,now())"));
            assertThrows(java.sql.SQLException.class,()->s.execute("INSERT INTO cap114_harness.window_sample VALUES('bad-count',2,3,0,now())"));
        }
    }
}
