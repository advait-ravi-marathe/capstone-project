package assignments.delivery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Instructor-owned tests for the supplied one-shot CLI's exit/result contract. */
class LocalIssuanceDeliveryCommandTest {
    @Test
    void invalidBatchSizesReturnUsageFailureWithoutCallingDispatcher() {
        for (String value : new String[]{"0", "-1", "21", "not-a-number"}) {
            OneBatchDispatcher dispatcher = mock(OneBatchDispatcher.class);
            LocalIssuanceDeliveryCommand command = command(dispatcher);
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            int exitCode = command.execute(args(value), sink(), new PrintStream(err, true, StandardCharsets.UTF_8));
            assertEquals(2, exitCode, "invalid batch size must have a stable usage exit: " + value);
            assertTrue(err.toString(StandardCharsets.UTF_8).contains("supply one integer in [1,20]"));
            verifyNoInteractions(dispatcher);
        }
    }

    @Test
    void emptyBatchReturnsSuccessAndTruthfulZeroCounts() {
        OneBatchDispatcher dispatcher = mock(OneBatchDispatcher.class);
        when(dispatcher.runOnce(10)).thenReturn(new BatchDeliveryReport(0, 0, 0, 0, 0));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int exitCode = command(dispatcher).execute(args(null), new PrintStream(out, true, StandardCharsets.UTF_8), sink());
        assertEquals(0, exitCode);
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("captured=0 receipts=0 replayed=0 acknowledged=0 pending=0"));
        verify(dispatcher).runOnce(10);
    }

    @Test
    void partialFailureReturnsNonzeroAndPrintsAlreadyCommittedProgress() {
        OneBatchDispatcher dispatcher = mock(OneBatchDispatcher.class);
        when(dispatcher.runOnce(3)).thenThrow(new BatchDeliveryFailureException(
                new BatchDeliveryReport(3, 1, 0, 1, 2), new IllegalStateException("receipt conflict")));
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int exitCode = command(dispatcher).execute(args("3"), sink(), new PrintStream(err, true, StandardCharsets.UTF_8));
        assertEquals(1, exitCode);
        String message = err.toString(StandardCharsets.UTF_8);
        assertTrue(message.contains("captured=3 receipts=1 replayed=0 acknowledged=1 pending=2"));
        assertTrue(message.contains("failure=IllegalStateException"));
        verify(dispatcher).runOnce(3);
    }

    private static LocalIssuanceDeliveryCommand command(OneBatchDispatcher dispatcher) {
        return new LocalIssuanceDeliveryCommand(dispatcher, mock(ConfigurableApplicationContext.class));
    }

    private static DefaultApplicationArguments args(String batchSize) {
        return new DefaultApplicationArguments(batchSize == null
                ? new String[]{"--coverstory.delivery.run-once"}
                : new String[]{"--coverstory.delivery.run-once", "--coverstory.delivery.batch-size=" + batchSize});
    }

    private static PrintStream sink() { return new PrintStream(new ByteArrayOutputStream()); }
}
