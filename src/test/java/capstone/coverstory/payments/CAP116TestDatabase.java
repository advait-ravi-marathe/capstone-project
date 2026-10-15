package capstone.coverstory.payments;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.UUID;

/** Fresh disposable PostgreSQL with frozen CAP-116 synthetic snapshots, effects and reservations. */
final class CAP116TestDatabase implements AutoCloseable {
    private final EmbeddedPostgres postgres;
    private final DataSource dataSource;
    private CAP116TestDatabase(EmbeddedPostgres postgres, DataSource dataSource) { this.postgres = postgres; this.dataSource = dataSource; }

    static CAP116TestDatabase fresh() throws Exception {
        EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
        try (Connection connection = pg.getPostgresDatabase().getConnection()) {
            DataSource ds = new DriverManagerDataSource(connection.getMetaData().getURL(), connection.getMetaData().getUserName(), "");
            new ResourceDatabasePopulator(new ClassPathResource("/coverstory/cap_116_reconciliation.sql")).execute(ds);
            seed(new JdbcTemplate(ds), new tools.jackson.databind.ObjectMapper().readTree(Files.readString(Path.of("docs/coverstory/fixtures/cap-116.json"))));
            return new CAP116TestDatabase(pg, ds);
        } catch (Exception failure) { pg.close(); throw failure; }
    }

    private static void seed(JdbcTemplate jdbc, tools.jackson.databind.JsonNode fixture) {
        for (var scenario : fixture.path("scenarios")) {
            String scenarioId = scenario.path("id").asText();
            var accepted = scenario.path("accepted_offer");
            jdbc.update("INSERT INTO cap116_harness.accepted_offer_revision(scenario_id,owner_id,application_id,offer_id,revision,content_sha256,total_minor,currency,accepted) VALUES(?,?,?,?,?,?,?,?,?)",
                    scenarioId, uuid(accepted,"owner_id"), uuid(accepted,"application_id"), uuid(accepted,"offer_id"),
                    accepted.path("revision").asText(), accepted.path("content_sha256").asText(), accepted.path("total_minor").asLong(),
                    accepted.path("currency").asText(), accepted.path("accepted").asBoolean());
            int index = 0;
            for (var effect : scenario.path("effects")) {
                UUID observation = UUID.nameUUIDFromBytes((scenarioId + ":effect-observation:" + index++).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                jdbc.update("INSERT INTO cap116_harness.effect_observation(observation_id,scenario_id,effect_id,owner_id,application_id,provider_reference,effect_kind,amount_minor,currency,offer_id,revision,content_sha256,authoritative) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        observation, scenarioId, nullableUuid(effect,"effect_id"), uuid(effect,"owner_id"), uuid(effect,"application_id"),
                        effect.path("provider_reference").asText(), effect.path("kind").asText(), effect.path("amount_minor").asLong(),
                        effect.path("currency").asText(), uuid(effect,"offer_id"), effect.path("revision").asText(),
                        effect.path("content_sha256").asText(), effect.path("attached_to_authoritative_effect").asBoolean());
            }
            for (var pending : scenario.path("pending_allocations")) {
                jdbc.update("INSERT INTO cap116_harness.pending_allocation(allocation_id,scenario_id,owner_id,application_id,idempotency_key,offer_id,revision,content_sha256,allocation_kind,status,amount_minor,currency) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)",
                        uuid(pending,"allocation_id"), scenarioId, uuid(pending,"owner_id"), uuid(pending,"application_id"), pending.path("idempotency_key").asText(),
                        uuid(pending,"offer_id"), pending.path("revision").asText(), pending.path("content_sha256").asText(), pending.path("kind").asText(),
                        pending.path("status").asText(), pending.path("amount_minor").asLong(), pending.path("currency").asText());
            }
        }
    }

    static UUID uuid(tools.jackson.databind.JsonNode node, String field) { return UUID.fromString(node.path(field).asText()); }
    static UUID nullableUuid(tools.jackson.databind.JsonNode node, String field) { return node.path(field).isNull() ? null : uuid(node,field); }
    EmbeddedPostgres postgres() { return postgres; }
    DataSource dataSource() { return dataSource; }
    JdbcTemplate jdbc() { return new JdbcTemplate(dataSource); }
    @Override public void close() throws Exception { postgres.close(); }
}
