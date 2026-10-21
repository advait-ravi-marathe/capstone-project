package assignments.delivery.recovery;

import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.CRC32;

/** Read-only preflight: refuses absent, additional, mismatched, or altered schema without repairing it. */
public final class ReleaseSchemaVerifier {
    private static final Map<String, Migration> MIGRATIONS = Map.of(
            "001", new Migration("homesecure quotes", "V001__homesecure_quotes.sql"),
            "002", new Migration("local policy lifecycle", "V002__local_policy_lifecycle.sql"),
            "003", new Migration("seed synthetic local policies", "V003__seed_synthetic_local_policies.sql"),
            "004", new Migration("event log delivery work", "V004__event_log_delivery_work.sql"),
            "005", new Migration("manual issuance recovery controls", "V005__manual_issuance_recovery_controls.sql"));

    /** Exact public-table column types for the unchanged V001-V005 project schema. */
    private static final Map<String, String> COLUMNS = expectedColumns();

    private final JdbcTemplate jdbc;
    public ReleaseSchemaVerifier(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void requireV001ThroughV005() {
        if (jdbc.getDataSource() == null) throw new IllegalStateException("release inspection requires a configured datasource");
        Map<String, String> applied = new TreeMap<>();
        jdbc.query("SELECT version, description, script, checksum, success FROM flyway_schema_history ORDER BY installed_rank", rs -> {
            String version = rs.getString("version");
            Migration expected = MIGRATIONS.get(version);
            String actual = rs.getString("description") + "|" + rs.getString("script");
            Integer expectedChecksum = expected == null ? null : migrationChecksum(expected.script());
            if (expected == null || !(expected.description() + "|" + expected.script()).equals(actual)
                    || rs.getObject("checksum") == null || rs.getInt("checksum") != expectedChecksum
                    || !rs.getBoolean("success") || applied.putIfAbsent(version, actual) != null)
                throw new IllegalStateException("release inspection requires exact successful V001-V005 migration history");
        });
        if (applied.size() != MIGRATIONS.size())
            throw new IllegalStateException("release inspection requires exact successful V001-V005 migration history");

        Map<String, String> actualColumns = new TreeMap<>();
        jdbc.query("""
                SELECT c.relname AS table_name, a.attname AS column_name,
                       pg_catalog.format_type(a.atttypid, a.atttypmod) AS formatted_type
                  FROM pg_catalog.pg_class c
                  JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
                  JOIN pg_catalog.pg_attribute a ON a.attrelid = c.oid
                 WHERE n.nspname = 'public' AND c.relkind IN ('r','p')
                   AND c.relname <> 'flyway_schema_history' AND a.attnum > 0 AND NOT a.attisdropped
                 ORDER BY c.relname, a.attnum
                """, rs -> {
            String key = rs.getString("table_name") + "." + rs.getString("column_name");
            actualColumns.put(key, rs.getString("formatted_type"));
        });
        if (!actualColumns.equals(COLUMNS))
            throw new IllegalStateException("release inspection refuses a schema outside the exact V001-V005 column contract");
    }

    /** Flyway's SQL checksum is CRC32 over UTF-8 lines, without line terminators or an initial BOM. */
    private static int migrationChecksum(String script) {
        CRC32 crc=new CRC32();
        try(var stream=ReleaseSchemaVerifier.class.getClassLoader().getResourceAsStream("db/migration/"+script)) {
            if(stream==null) throw new IllegalStateException("packaged migration is absent: "+script);
            try(var reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))) {
                String line; boolean first=true;
                while((line=reader.readLine())!=null) {
                    if(first&&line.startsWith("\uFEFF")) line=line.substring(1);
                    first=false; crc.update(line.getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch(IOException failure) { throw new IllegalStateException("cannot read packaged migration: "+script,failure); }
        return (int)crc.getValue();
    }

    private record Migration(String description,String script) {}

    private static Map<String, String> expectedColumns() {
        Map<String, String> result = new LinkedHashMap<>();
        add(result, "journeys", "journey_id:text");
        add(result, "quote_versions", "journey_id:text", "quote_version_id:text", "product_code:text", "product_version:text",
                "rate_version:text", "rebuild_amount_rupees:numeric(14,2)", "coverage_code:text", "currency:text",
                "annual_premium_rupees:numeric(14,2)", "created_at:timestamp with time zone", "expires_at:timestamp with time zone");
        add(result, "selections", "journey_id:text", "quote_version_id:text", "selection_order:integer");
        add(result, "training_policy_lifecycle", "policy_id:text", "owner_id:text", "journey_id:text", "quote_version_id:text",
                "product_version:text", "rate_version:text", "annual_premium_rupees:numeric(12,2)", "currency:character(3)",
                "effective_date:date", "status:text", "version:bigint", "cancellation_effective_date:date");
        add(result, "training_policy_lifecycle_event", "policy_id:text", "version:bigint", "event_type:text", "effective_date:date",
                "change_code:text", "training_rebuild_amount_rupees:numeric(12,2)");
        add(result, "training_policy_idempotency", "owner_id:text", "client_request_id:text", "request_fingerprint:text", "policy_id:text");
        add(result, "training_issuance_work", "policy_id:text", "source_version:bigint", "event_type:text", "effective_date:date",
                "envelope_schema_version:integer", "captured_at:timestamp with time zone", "acknowledged_at:timestamp with time zone",
                "quarantined_at:timestamp with time zone", "quarantine_reason:text", "control_revision:integer");
        add(result, "training_issuance_delivery_receipt", "policy_id:text", "source_version:bigint", "event_type:text", "effective_date:date",
                "envelope_schema_version:integer", "received_at:timestamp with time zone");
        add(result, "training_issuance_recovery_history", "policy_id:text", "source_version:bigint", "control_revision:integer", "action:text",
                "reason_code:text", "decided_at:timestamp with time zone");
        return Map.copyOf(result);
    }

    private static void add(Map<String, String> target, String table, String... columns) {
        for (String column : columns) {
            int separator = column.indexOf(':');
            target.put(table + "." + column.substring(0, separator), column.substring(separator + 1));
        }
    }
}
