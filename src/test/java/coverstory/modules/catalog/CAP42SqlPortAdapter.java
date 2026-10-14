package coverstory.modules.catalog;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import javax.sql.DataSource;

/** Instructor test adapter: executes the learner SQL through the Catalog-owned public port. */
final class CAP42SqlPortAdapter implements CatalogEvidenceTotalsPort {
    private static final String SQL = readSql();
    private final DataSource dataSource;

    CAP42SqlPortAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public CatalogEvidenceTotals totals(String editorFixture, OffsetDateTime fromUtc, OffsetDateTime toUtc) {
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(SQL)) {
            statement.setString(1, editorFixture);
            statement.setObject(2, fromUtc);
            statement.setObject(3, toUtc);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) throw new IllegalStateException("CAP-42 query must return one aggregate row");
                CatalogEvidenceTotals totals = new CatalogEvidenceTotals(
                        rows.getLong("published_count"), rows.getBigDecimal("known_base_total_minor"));
                if (rows.next()) throw new IllegalStateException("CAP-42 query must return exactly one aggregate row");
                return totals;
            }
        } catch (SQLException failure) {
            throw new IllegalStateException("CAP-42 catalog evidence query failed", failure);
        }
    }

    private static String readSql() {
        try (var input = CAP42SqlPortAdapter.class.getResourceAsStream("/coverstory/catalog/cap_42.sql")) {
            if (input == null) throw new IOException("Missing learner SQL resource");
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new ExceptionInInitializerError(failure);
        }
    }
}
