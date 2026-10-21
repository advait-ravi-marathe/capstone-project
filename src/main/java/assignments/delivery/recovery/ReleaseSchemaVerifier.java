package assignments.delivery.recovery;

import org.springframework.jdbc.core.JdbcTemplate;

/** Read-only preflight: refuses absent/incomplete history instead of migrating or seeding it. */
public final class ReleaseSchemaVerifier {
    private final JdbcTemplate jdbc;
    public ReleaseSchemaVerifier(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void requireV001ThroughV005() {
        // Fixed catalog/history reads only; never call Flyway.migrate or initialize tables here.
        Integer count = jdbc.queryForObject("""
                SELECT count(*) FROM flyway_schema_history
                 WHERE success=true AND version IN ('001','002','003','004','005')
                """, Integer.class);
        if (count == null || count != 5) throw new IllegalStateException("inspection requires successful V001-V005 schema history");
        for (String table : java.util.List.of("training_policy_lifecycle", "training_policy_idempotency",
                "training_issuance_work", "training_issuance_delivery_receipt", "training_issuance_recovery_history")) {
            Boolean exists = jdbc.queryForObject("SELECT to_regclass(?) IS NOT NULL", Boolean.class, "public." + table);
            if (!Boolean.TRUE.equals(exists)) throw new IllegalStateException("inspection schema is incomplete");
        }
    }
}
