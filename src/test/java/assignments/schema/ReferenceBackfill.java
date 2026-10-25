package assignments.schema;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/** Test-only witness that runs real bounded SQL in PostgreSQL; never substitutes for learner completion. */
final class ReferenceBackfill {
    private ReferenceBackfill() { }

    static Integer runBatch(DataSource source, String jobId, int limit) throws Exception {
        return runBatch(source, jobId, limit, () -> { });
    }

    static Integer runBatch(DataSource source, String jobId, int limit, Runnable afterFirstRow) throws Exception {
        if (jobId == null || jobId.isBlank() || limit < 1 || limit > 4) throw new IllegalArgumentException("invalid job or batch size");
        try (Connection connection = source.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int cursor = 0;
                try (PreparedStatement read = connection.prepareStatement("SELECT last_item_id FROM gap_backfill_checkpoint WHERE job_id=?")) {
                    read.setString(1, jobId);
                    try (ResultSet rows = read.executeQuery()) { if (rows.next()) cursor = rows.getInt(1); }
                }
                int max;
                try (var statement = connection.createStatement(); var rows = statement.executeQuery("SELECT COALESCE(max(item_id),0) FROM gap_backfill_item")) {
                    rows.next(); max = rows.getInt(1);
                }
                if (cursor > max) throw new IllegalArgumentException("cursor exceeds highest source id");
                List<Integer> ids = new ArrayList<>();
                try (PreparedStatement select = connection.prepareStatement(
                        "SELECT item_id FROM gap_backfill_item WHERE item_id>? ORDER BY item_id LIMIT ?")) {
                    select.setInt(1, cursor); select.setInt(2, limit);
                    try (ResultSet rows = select.executeQuery()) { while (rows.next()) ids.add(rows.getInt(1)); }
                }
                if (ids.isEmpty()) { connection.commit(); return null; }
                for (int id : ids) {
                    try (PreparedStatement update = connection.prepareStatement("""
                            UPDATE gap_backfill_item SET normalized_value=lower(trim(legacy_value)),
                              normalization_version=1, update_count=update_count+1 WHERE item_id=? AND normalized_value IS NULL
                            """)) {
                        update.setInt(1, id);
                        if (update.executeUpdate() != 1) throw new IllegalStateException("source row was absent or already updated");
                    }
                    if (id == ids.get(0)) afterFirstRow.run();
                }
                int next = ids.get(ids.size() - 1);
                try (PreparedStatement checkpoint = connection.prepareStatement("""
                        INSERT INTO gap_backfill_checkpoint(job_id,last_item_id) VALUES (?,?)
                        ON CONFLICT (job_id) DO UPDATE SET last_item_id=EXCLUDED.last_item_id
                        """)) {
                    checkpoint.setString(1, jobId); checkpoint.setInt(2, next); checkpoint.executeUpdate();
                }
                connection.commit(); return next;
            } catch (Exception failure) { connection.rollback(); throw failure; }
        }
    }
}
