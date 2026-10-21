package assignments.learning;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Real PostgreSQL EXPLAIN/output comparison on an isolated teaching table. */
class Day24ExplainReferenceTest {
    @Test void indexChangesAvailablePlanWithoutChangingOrderedRows() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            try (Statement s = c.createStatement()) {
                s.execute("CREATE TABLE teaching_readings(station_id INT NOT NULL, reading_id INT PRIMARY KEY, measured_at TIMESTAMPTZ NOT NULL)");
                s.execute("INSERT INTO teaching_readings SELECT g%100, g, TIMESTAMPTZ '2026-01-01 00:00:00+00' + floor(g/1000.0)*INTERVAL '1 minute' FROM generate_series(1,10000) g");
                s.execute("ANALYZE teaching_readings");
            }
            String sql = "SELECT reading_id,measured_at FROM teaching_readings WHERE station_id=7 ORDER BY measured_at DESC,reading_id ASC LIMIT 2";
            List<String> before = rows(c, sql);
            String beforePlan = explain(c, sql);
            try (Statement s = c.createStatement()) { s.execute("CREATE INDEX teaching_readings_order ON teaching_readings(station_id,measured_at DESC,reading_id ASC)"); }
            try (Statement s = c.createStatement()) { s.execute("ANALYZE teaching_readings"); }
            List<String> after = rows(c, sql);
            String afterPlan = explain(c, sql);
            assertEquals(List.of("9007@2026-01-01T00:09:00Z", "9107@2026-01-01T00:09:00Z"), before);
            assertEquals(before, after);
            assertEquals(true, afterPlan.contains("teaching_readings_order"), afterPlan);
            assertFalse(afterPlan.contains("Sort"), afterPlan);
            System.out.println("Teaching plan before index:\n" + beforePlan + "Teaching plan after index:\n" + afterPlan);
        }
    }
    private static List<String> rows(Connection c, String sql) throws Exception {
        List<String> out = new ArrayList<>();
        try (Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) { while(r.next()) out.add(r.getInt(1)+"@"+r.getTimestamp(2).toInstant()); }
        return out;
    }
    private static String explain(Connection c, String sql) throws Exception {
        StringBuilder out=new StringBuilder();
        try (Statement s=c.createStatement(); ResultSet r=s.executeQuery("EXPLAIN "+sql)) { while(r.next()) out.append(r.getString(1)).append('\n'); }
        return out.toString();
    }
}
