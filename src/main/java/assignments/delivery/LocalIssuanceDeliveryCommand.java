package assignments.delivery;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.PrintStream;

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
        int result = execute(args, System.out, System.err);
        int exitCode = SpringApplication.exit(context, () -> result);
        System.exit(exitCode);
    }

    int execute(ApplicationArguments args, PrintStream out, PrintStream err) {
        int limit = parseLimit(args, err);
        if (limit < 1) return 2;
        try {
            BatchDeliveryReport result = dispatcher.runOnce(limit);
            out.printf("delivery batch committed: captured=%d receipts=%d replayed=%d acknowledged=%d pending=%d%n",
                    result.captured(), result.receiptsCreated(), result.identicalReplays(),
                    result.acknowledged(), result.pendingAtStop());
            return 0;
        } catch (BatchDeliveryFailureException failure) {
            BatchDeliveryReport progress = failure.committedProgress();
            err.printf("delivery batch stopped: captured=%d receipts=%d replayed=%d acknowledged=%d pending=%d failure=%s%n",
                    progress.captured(), progress.receiptsCreated(), progress.identicalReplays(),
                    progress.acknowledged(), progress.pendingAtStop(), failure.getCause().getClass().getSimpleName());
            return 1;
        } catch (RuntimeException failure) {
            err.println("delivery batch failed; pending work remains visible: " + failure.getClass().getSimpleName());
            return 1;
        }
    }

    private static int parseLimit(ApplicationArguments args, PrintStream err) {
        var values = args.getOptionValues("coverstory.delivery.batch-size");
        if (values == null || values.isEmpty()) return 10;
        if (values.size() != 1) {
            err.println("invalid --coverstory.delivery.batch-size: supply one integer in [1,20]");
            return -1;
        }
        try {
            int limit = Integer.parseInt(values.getFirst());
            if (limit < 1 || limit > 20) throw new NumberFormatException("outside [1,20]");
            return limit;
        } catch (NumberFormatException invalid) {
            err.println("invalid --coverstory.delivery.batch-size: supply one integer in [1,20]");
            return -1;
        }
    }
}
