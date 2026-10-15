package capstone.coverstory.application;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

/** Fresh disposable database containing the two fixed synthetic immutable quote snapshots. */
final class CAP115TestDatabase implements AutoCloseable {
    static final UUID OWNER_A=UUID.fromString("11500000-0000-4000-8000-000000000001");
    static final UUID OWNER_B=UUID.fromString("11500000-0000-4000-8000-000000000002");
    static final UUID ORIGINAL=UUID.fromString("11500000-0000-4000-8000-000000000011");
    static final UUID REVISED=UUID.fromString("11500000-0000-4000-8000-000000000012");
    static final UUID CASE=UUID.fromString("11500000-0000-4000-8000-000000000099");
    static final String ORIGINAL_HASH="3e3c1f0849a29bac2ddfd3d356c1ff0bfb925bcce3ce4fcc8f07db188c96bafe";
    static final String REVISED_HASH="f96df155cd21849d54e42885cdb9432a9182fcc894f1b4ce794df7eae828d91c";
    private final EmbeddedPostgres postgres;
    private final DataSource dataSource;
    private CAP115TestDatabase(EmbeddedPostgres pg,DataSource ds){postgres=pg;dataSource=ds;}

    static CAP115TestDatabase fresh() throws Exception {
        EmbeddedPostgres pg=EmbeddedPostgres.builder().start();
        try(Connection connection=pg.getPostgresDatabase().getConnection()) {
            DataSource ds=new DriverManagerDataSource(connection.getMetaData().getURL(),connection.getMetaData().getUserName(),"");
            new ResourceDatabasePopulator(new ClassPathResource("/coverstory/cap_115_offer_explanation.sql")).execute(ds);
            JdbcTemplate jdbc=new JdbcTemplate(ds);
            jdbc.update("INSERT INTO cap115_harness.offer_snapshot(offer_id,snapshot_role,owner_id,revision,content_sha256,amount_minor,currency,term_codes,expires_at) VALUES(?,?,?,?,?,?,?,?::text[],?::timestamptz)",
                    ORIGINAL,"ORIGINAL",OWNER_A,"q1",ORIGINAL_HASH,100000,"INR",new String[]{"copay_10_percent","waiting_30_days"},"2027-01-15T00:00:00Z");
            jdbc.update("INSERT INTO cap115_harness.offer_snapshot(offer_id,snapshot_role,owner_id,revision,content_sha256,amount_minor,currency,term_codes,expires_at) VALUES(?,?,?,?,?,?,?,?::text[],?::timestamptz)",
                    REVISED,"REVISED",OWNER_A,"o2",REVISED_HASH,110000,"INR",new String[]{"copay_0_percent","restore_benefit","waiting_30_days"},"2027-01-20T00:00:00Z");
            jdbc.update("INSERT INTO cap115_harness.authorized_source(owner_id,source_id,version) VALUES(?,?,?)",OWNER_A,"offer-source-a","v2");
            jdbc.update("INSERT INTO cap115_harness.authorized_source(owner_id,source_id,version) VALUES(?,?,?)",OWNER_B,"owner-b-source","v1");
            return new CAP115TestDatabase(pg,ds);
        } catch(Exception error){pg.close();throw error;}
    }
    EmbeddedPostgres postgres(){return postgres;}
    DataSource dataSource(){return dataSource;}
    @Override public void close() throws Exception { postgres.close(); }
}
