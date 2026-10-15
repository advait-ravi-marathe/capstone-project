package coverstory.modules.catalog;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

/** One isolated empty-publication catalog DB per CAP57 acceptance method. */
final class CAP57TestDatabase implements AutoCloseable {
    private final EmbeddedPostgres postgres;

    private CAP57TestDatabase(EmbeddedPostgres postgres) { this.postgres = postgres; }

    static CAP57TestDatabase fresh() throws Exception {
        EmbeddedPostgres pg = EmbeddedPostgres.builder().start();
        try (Connection c = pg.getPostgresDatabase().getConnection(); Statement s = c.createStatement()) {
            executeResource(s, "/db/schema/ownership.sql");
            executeResource(s, "/db/schema/catalog_publication.sql");
            executeResource(s, "/db/schema/catalog_discovery.sql");
            executeResource(s, "/coverstory/catalog/cap_57_fixtures.sql");
            executeResource(s, "/coverstory/catalog/cap_57_discovery.sql");
            try (var rs = s.executeQuery("SELECT count(*) FROM catalog.active_health_offer")) {
                if (!rs.next() || rs.getInt(1) != 0) throw new AssertionError("fresh CAP57 DB must have no active publication");
            }
            try (var rs = s.executeQuery("SELECT count(*) FROM catalog.health_offer_version")) {
                if (!rs.next() || rs.getInt(1) != 3) throw new AssertionError("fresh CAP57 DB must load all three immutable prepared versions");
            }
            return new CAP57TestDatabase(pg);
        } catch (Exception error) {
            pg.close();
            throw error;
        }
    }

    EmbeddedPostgres postgres() { return postgres; }

    @Override public void close() throws Exception { postgres.close(); }

    private static void executeResource(Statement s, String path) throws Exception {
        try (var in = CAP57TestDatabase.class.getResourceAsStream(path)) {
            if (in == null) throw new java.io.IOException("Missing synthetic SQL resource " + path);
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            boolean dollar = false, single = false, lineComment = false; StringBuilder statement = new StringBuilder();
            for (int i = 0; i < sql.length(); i++) {
                if (!single && !dollar && sql.startsWith("--", i)) lineComment = true;
                if (lineComment) { char commentChar = sql.charAt(i); if (commentChar == '\n') { lineComment = false; statement.append(commentChar); } continue; }
                if (!single && sql.startsWith("$$", i)) { dollar = !dollar; statement.append("$$"); i++; continue; }
                char c = sql.charAt(i);
                if (!dollar && c == '\'') single = !single;
                if (!dollar && !single && c == ';') {
                    if (!statement.toString().isBlank()) s.execute(statement.toString());
                    statement.setLength(0);
                } else statement.append(c);
            }
            if (!statement.toString().isBlank()) s.execute(statement.toString());
        }
    }
}
