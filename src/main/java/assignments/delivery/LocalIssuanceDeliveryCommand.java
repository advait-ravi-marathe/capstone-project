package assignments.delivery;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/** Runs only when explicitly invoked with --coverstory.delivery.run-once. */
@Component
public final class LocalIssuanceDeliveryCommand implements ApplicationRunner {
    private final OneBatchDispatcher dispatcher;
    private final ConfigurableApplicationContext context;

    public LocalIssuanceDeliveryCommand(OneBatchDispatcher dispatcher, ConfigurableApplicationContext context) {
        this.dispatcher = dispatcher;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption("coverstory.delivery.run-once")) return;
        var values = args.getOptionValues("coverstory.delivery.batch-size");
        int limit = values == null || values.isEmpty() ? 10 : Integer.parseInt(values.getFirst());
        try {
            BatchDeliveryReport result = dispatcher.runOnce(limit);
            System.out.printf("delivery batch committed: captured=%d receipts=%d replayed=%d acknowledged=%d pending=%d%n",
                    result.captured(), result.receiptsCreated(), result.identicalReplays(),
                    result.acknowledged(), result.pendingAtStop());
            int exitCode = SpringApplication.exit(context, () -> 0);
            System.exit(exitCode);
        } catch (BatchDeliveryFailureException failure) {
            BatchDeliveryReport progress = failure.committedProgress();
            System.err.printf("delivery batch stopped: captured=%d receipts=%d replayed=%d acknowledged=%d pending=%d failure=%s%n",
                    progress.captured(), progress.receiptsCreated(), progress.identicalReplays(),
                    progress.acknowledged(), progress.pendingAtStop(), failure.getCause().getClass().getSimpleName());
            int exitCode = SpringApplication.exit(context, () -> 1);
            System.exit(exitCode);
        } catch (RuntimeException failure) {
            System.err.println("delivery batch failed; pending work remains visible: " + failure.getClass().getSimpleName());
            int exitCode = SpringApplication.exit(context, () -> 1);
            System.exit(exitCode);
        }
    }
}
