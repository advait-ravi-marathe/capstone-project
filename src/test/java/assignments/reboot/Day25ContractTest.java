package assignments.reboot;

import assignments.insurance.IdempotentSelectionWriter;
import assignments.insurance.QuoteSelectionKey;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Real PostgreSQL and concurrent callers; schema is isolated from unfinished earlier migrations. */
@Tag("learnerAcceptance")
class Day25ContractTest {
    private static EmbeddedPostgres postgres;
    private static DataSource dataSource;

    @BeforeAll
    static void startPostgresAndCreateIsolatedConstraints() throws Exception {
        postgres = EmbeddedPostgres.builder().start();
        dataSource = postgres.getPostgresDatabase();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE selections (journey_id TEXT NOT NULL, quote_version_id TEXT NOT NULL, PRIMARY KEY (journey_id, quote_version_id))");
        }
    }

    @AfterAll
    static void stopPostgres() throws IOException {
        if (postgres != null) postgres.close();
    }

    @BeforeEach
    void clearRows() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE selections");
        }
    }

    @Test
    void twoConcurrentSamePairCallsReturnOneRepresentationAndOneRow() throws Exception {
        var returned = callConcurrently("journey-A", "quote-v1", "journey-A", "quote-v1");
        assertEquals(returned.first(), returned.second());
        assertEquals(1, rowCount());
    }

    @Test
    void twoConcurrentDifferentPairsCreateTwoRows() throws Exception {
        var returned = callConcurrently("journey-A", "quote-v1", "journey-A", "quote-v2");
        assertEquals(new QuoteSelectionKey("journey-A", "quote-v1"), returned.first());
        assertEquals(new QuoteSelectionKey("journey-A", "quote-v2"), returned.second());
        assertEquals(2, rowCount());
    }

    private static Pair callConcurrently(String journeyA, String quoteA, String journeyB, String quoteB)
            throws Exception {
        IdempotentSelectionWriter writer = new IdempotentSelectionWriter(dataSource);
        CyclicBarrier ready = new CyclicBarrier(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<QuoteSelectionKey> first = pool.submit(() -> {
                ready.await();
                return writer.select(journeyA, quoteA);
            });
            Future<QuoteSelectionKey> second = pool.submit(() -> {
                ready.await();
                return writer.select(journeyB, quoteB);
            });
            return new Pair(first.get(), second.get());
        } finally {
            pool.shutdownNow();
        }
    }

    private static int rowCount() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT count(*) FROM selections")) {
            result.next();
            return result.getInt(1);
        }
    }

    private record Pair(QuoteSelectionKey first, QuoteSelectionKey second) { }
}
