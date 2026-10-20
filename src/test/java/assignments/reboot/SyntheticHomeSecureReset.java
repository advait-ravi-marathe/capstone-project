package assignments.reboot;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Fixed instructor fixture reset, used only against a disposable embedded PostgreSQL instance. */
final class SyntheticHomeSecureReset {
    private SyntheticHomeSecureReset() { }

    static void reset(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.execute("TRUNCATE selections, quote_versions, journeys RESTART IDENTITY CASCADE");
                statement.execute("INSERT INTO journeys (journey_id) VALUES ('homesecure-training-journey')");
                statement.execute("""
                        INSERT INTO quote_versions (
                          journey_id, quote_version_id, product_code, product_version, rate_version,
                          rebuild_amount_rupees, coverage_code, currency, annual_premium_rupees,
                          created_at, expires_at
                        ) VALUES (
                          'homesecure-training-journey', 'home-secure-training-v1', 'homesecure',
                          'home-secure-v1', 'home-secure-training-rate-v1', 100000.00, 'BUILDING',
                          'INR', 125.00, TIMESTAMPTZ '2026-10-20 12:00:00+00',
                          TIMESTAMPTZ '2026-10-20 12:15:00+00'
                        )
                        """);
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }
}
