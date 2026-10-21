# Day 9 — Read and write rows with raw JDBC

**Required course background:** in28Minutes Official, *Master Spring Boot 3 & Spring Framework 6 with Java*, section “Getting Started with JPA and Hibernate with Spring and Spring Boot”: “Step 07 - Querying Data using Spring JDBC” (5:36; item 35019858). This is higher-level background; the focused guide teaches the repository's raw `DataSource`/`PreparedStatement` seam. “Step 06 - Inserting and Deleting Data using Spring JDBC” (9:01; item 35019864) is optional later, not stacked today. Catalog title verified; playback not verified.

Study budget: 5:36 course + 15:24 worked guide + 4:00 readiness = 25 minutes. This is PostgreSQL/JDBC, not a request to adopt JPA, `JdbcTemplate`, or a new database.

## Start with the isolated table

```sql
CREATE TABLE teaching_prices (
  shop_id TEXT NOT NULL,
  sku TEXT NOT NULL,
  amount NUMERIC(14,2) NOT NULL,
  recorded_at TIMESTAMPTZ NOT NULL,
  PRIMARY KEY (shop_id, sku)
);
```

The instructor test creates and closes its own embedded PostgreSQL server and its own teaching table. It does not use the production migration or implement `QuoteRepositoryAdapter`.

## Complete reference operations

```java
record Price(BigDecimal amount, Instant recordedAt) { }

static void insertPrice(DataSource ds, String shop, String sku, Price price) throws SQLException {
    try (Connection c = ds.getConnection();
         PreparedStatement p = c.prepareStatement(
             "INSERT INTO teaching_prices (shop_id, sku, amount, recorded_at) VALUES (?, ?, ?, ?)")) {
        p.setString(1, shop);
        p.setString(2, sku);
        p.setBigDecimal(3, price.amount());
        p.setObject(4, price.recordedAt().atOffset(ZoneOffset.UTC));
        p.executeUpdate();
    }
}

static Optional<Price> findPrice(DataSource ds, String shop, String sku) throws SQLException {
    try (Connection c = ds.getConnection();
         PreparedStatement p = c.prepareStatement(
             "SELECT amount, recorded_at FROM teaching_prices WHERE shop_id = ? AND sku = ?")) {
        p.setString(1, shop);
        p.setString(2, sku);
        try (ResultSet r = p.executeQuery()) {
            if (!r.next()) return Optional.empty();
            return Optional.of(new Price(r.getBigDecimal("amount"),
                r.getObject("recorded_at", OffsetDateTime.class).toInstant()));
        }
    }
}
```

Follow one request in order: obtain one connection; prepare fixed SQL with placeholders; bind values by 1-based positions; execute; advance the result set from its before-first position with `next()`; map columns to a detached value; close result, statement, and connection through try-with-resources. Placeholders keep supplied text as data. `Optional.empty()` means the query succeeded and found no row; `SQLException` remains a database failure, not absence.

The executable reference checks these observations on PostgreSQL:

1. Insert shop `north`, SKU `pen`, `12.30`, instant `2026-01-01T10:00:00Z`; a fresh lookup returns the same decimal value and instant.
2. `south/pen`, `north/missing`, and the literal SKU `x' OR '1'='1` each return empty.
3. Re-inserting `north/pen` with a different value fails with `23505`; a new connection still reads the original value.
4. The `+05:30` timestamp `2026-01-01T15:30:00+05:30` round-trips as the same instant `10:00:00Z`.
5. In a separate test, dropping the teaching table makes lookup throw PostgreSQL `42P01`; it is not mapped to absence. The test recreates the table before cleanup.

```sh
./mvnw -Dtest=Day09JdbcReferenceTest test
```

## Practice and transfer

Keep the existing 85-minute practice allocation: 15 minutes run/inspect the reference, 10 minutes predict the scoped `north/pencil` variation, 45 minutes on the existing adapter, and 15 minutes on its selector/diagnosis. Evidence stays within 10 minutes.

**Readiness question:** Why distinguish a missing row from database failure? Which construct closes each JDBC resource? Why bind both shop and SKU? Then inspect the actual `QuoteSnapshot` and implement only the existing `DataSource` adapter task; this example does not provide its complete SQL mapping.

Existing learner selector: `./mvnw -Plearner-tests -Dtest=Day09ContractTest test`. This reference does not satisfy Days 7–8 or integrated acceptance.

**Primary references:** pgJDBC [querying and processing results](https://jdbc.postgresql.org/documentation/query/), including prepared statements and Java time classes; Java 21 [`PreparedStatement`](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/PreparedStatement.html); Oracle's short [try-with-resources](https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html) resource-lifetime example.
