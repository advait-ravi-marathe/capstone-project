package coverstory.modules.catalog;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/** Test-only SQL reader; it executes the saved learner resource in disposable PostgreSQL. */
final class CAP45SqlPortAdapter {
    private final DataSource dataSource;

    CAP45SqlPortAdapter(DataSource dataSource) { this.dataSource = dataSource; }

    List<Row> query(String editor, OffsetDateTime fromUtc, OffsetDateTime toUtc) throws Exception {
        String sql = resource("/coverstory/catalog/cap_45.sql");
        try (Connection c = dataSource.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, editor);
            p.setObject(2, fromUtc);
            p.setObject(3, toUtc);
            try (ResultSet rs = p.executeQuery()) {
                List<Row> rows = new ArrayList<>();
                while (rs.next()) {
                    long amount = rs.getLong("base_amount_minor");
                    BigDecimal nullableAmount = rs.wasNull() ? null : BigDecimal.valueOf(amount);
                    int attempts = rs.getInt("attempt_count");
                    rows.add(new Row(rs.getString("source_id"), nullableAmount, attempts,
                            rs.getObject("last_attempt_at", OffsetDateTime.class), rs.getBoolean("has_attempt")));
                }
                return rows;
            }
        }
    }

    private static String resource(String path) throws IOException {
        try (var in = CAP45SqlPortAdapter.class.getResourceAsStream(path)) {
            if (in == null) throw new IOException("Missing SQL resource " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    record Row(String sourceId, BigDecimal amountMinor, int attemptCount, OffsetDateTime lastAttemptAt,
               boolean hasAttempt) { }
}
