package assignments.learning;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Day09JdbcReferenceTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;
    record Price(BigDecimal amount, Instant recordedAt) { }

    @BeforeAll static void startDisposablePostgres() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE TABLE teaching_prices (shop_id TEXT NOT NULL, sku TEXT NOT NULL, amount NUMERIC(14,2) NOT NULL, recorded_at TIMESTAMPTZ NOT NULL, PRIMARY KEY(shop_id,sku))");
        }
    }

    @AfterAll static void closeDisposablePostgres() throws IOException { if (postgres != null) postgres.close(); }

    @BeforeEach void clearRows() throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) { s.execute("TRUNCATE teaching_prices"); }
    }

    @Test void scopedReadRoundTripsDecimalAndInstantWithFreshConnection() throws Exception {
        var expected = new Price(new BigDecimal("12.30"), Instant.parse("2026-01-01T10:00:00Z"));
        insertPrice(dataSource, "north", "pen", expected);
        assertEquals(Optional.of(expected), findPrice(dataSource, "north", "pen"));
        assertEquals(Optional.empty(), findPrice(dataSource, "south", "pen"));
        assertEquals(Optional.empty(), findPrice(dataSource, "north", "missing"));
        assertEquals(Optional.empty(), findPrice(dataSource, "north", "x' OR '1'='1"));
    }

    @Test void duplicateSnapshotDoesNotReplaceCommittedValue() throws Exception {
        var original = new Price(new BigDecimal("12.30"), Instant.parse("2026-01-01T10:00:00Z"));
        insertPrice(dataSource, "north", "pen", original);
        var changed = new Price(new BigDecimal("99.00"), Instant.parse("2026-01-02T10:00:00Z"));
        SQLException error = assertThrows(SQLException.class, () -> insertPrice(dataSource, "north", "pen", changed));
        assertEquals("23505", error.getSQLState());
        assertEquals(Optional.of(original), findPrice(dataSource, "north", "pen"));
    }

    @Test void nonUtcOffsetIsStoredAsTheSameInstant() throws Exception {
        var local = OffsetDateTime.parse("2026-01-01T15:30:00+05:30");
        var expected = new Price(new BigDecimal("0.07"), local.toInstant());
        insertPrice(dataSource, "north", "pencil", expected);
        assertEquals(Instant.parse("2026-01-01T10:00:00Z"), findPrice(dataSource, "north", "pencil").orElseThrow().recordedAt());
    }

    @Test void sqlFailureIsNotMistakenForAbsentRow() throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) { s.execute("DROP TABLE teaching_prices"); }
        try {
            SQLException error = assertThrows(SQLException.class, () -> findPrice(dataSource, "north", "pen"));
            assertEquals("42P01", error.getSQLState());
        } finally {
            try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
                s.execute("CREATE TABLE teaching_prices (shop_id TEXT NOT NULL, sku TEXT NOT NULL, amount NUMERIC(14,2) NOT NULL, recorded_at TIMESTAMPTZ NOT NULL, PRIMARY KEY(shop_id,sku))");
            }
        }
    }

    static void insertPrice(DataSource ds, String shop, String sku, Price price) throws SQLException {
        try (Connection c = ds.getConnection(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO teaching_prices (shop_id, sku, amount, recorded_at) VALUES (?, ?, ?, ?)")) {
            p.setString(1, shop); p.setString(2, sku); p.setBigDecimal(3, price.amount());
            p.setObject(4, price.recordedAt().atOffset(ZoneOffset.UTC)); p.executeUpdate();
        }
    }

    static Optional<Price> findPrice(DataSource ds, String shop, String sku) throws SQLException {
        try (Connection c = ds.getConnection(); PreparedStatement p = c.prepareStatement(
                "SELECT amount, recorded_at FROM teaching_prices WHERE shop_id = ? AND sku = ?")) {
            p.setString(1, shop); p.setString(2, sku);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return Optional.empty();
                return Optional.of(new Price(r.getBigDecimal("amount"),
                        r.getObject("recorded_at", OffsetDateTime.class).toInstant()));
            }
        }
    }
}
