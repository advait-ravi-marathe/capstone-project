package assignments.delivery;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Test-only child JVM entry point; never packaged into the local application JAR. */
public final class DurableDeliveryChildMain {
    private DurableDeliveryChildMain() { }

    public static void main(String[] args) throws Exception {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(args[0]);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        IssuanceWorkStore repository = new JdbcIssuanceWorkStore(jdbc);
        if (args.length > 2 && args[1].equals("block-before-ack")) {
            Path marker = Path.of(args[2]);
            repository = new BlockingAcknowledgmentStore(repository, marker);
        }
        BatchDeliveryReport report = new OneBatchDispatcher(repository,
                new DataSourceTransactionManager(dataSource)).runOnce(10);
        System.out.println(report);
    }

    private static final class BlockingAcknowledgmentStore implements IssuanceWorkStore {
        private final IssuanceWorkStore delegate;
        private final Path marker;
        private final CountDownLatch neverRelease = new CountDownLatch(1);
        BlockingAcknowledgmentStore(IssuanceWorkStore delegate, Path marker) {
            this.delegate = delegate;
            this.marker = marker;
        }
        @Override public int captureCommittedIssuances(int limit) { return delegate.captureCommittedIssuances(limit); }
        @Override public List<IssuanceEnvelope> findPending(int limit) { return delegate.findPending(limit); }
        @Override public DeliveryReceiptResult acceptReceipt(IssuanceEnvelope envelope) {
            return delegate.acceptReceipt(envelope);
        }
        @Override public boolean acknowledge(IssuanceEnvelope envelope) {
            try {
                Files.writeString(marker, "receipt transaction returned; acknowledgment not called");
                if (!neverRelease.await(Duration.ofSeconds(90).toMillis(), TimeUnit.MILLISECONDS)) {
                    throw new IllegalStateException("bounded test block expired before parent terminated child");
                }
                return delegate.acknowledge(envelope);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("test-only acknowledgment blocker interrupted", exception);
            } catch (java.io.IOException exception) {
                throw new IllegalStateException("could not signal child commit boundary", exception);
            }
        }
    }
}
