package assignments.delivery.recovery;

import assignments.delivery.IssuanceSourceKey;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.PrintStream;
import java.util.List;

/** Explicit one-key local recovery command. Ordinary startup is read-only and does no work. */
public final class LocalManualRecoveryCommand implements ApplicationRunner {
    private final ManualRecoveryService service;
    private final ConfigurableApplicationContext context;
    public LocalManualRecoveryCommand(ManualRecoveryService service, ConfigurableApplicationContext context) {
        this.service=service; this.context=context;
    }
    @Override public void run(ApplicationArguments args) {
        if (!args.containsOption("coverstory.recovery.action")) return;
        if (args.containsOption("coverstory.delivery.run-once")) throw new IllegalArgumentException("delivery and recovery modes are mutually exclusive");
        int code = execute(args,System.out,System.err);
        System.exit(SpringApplication.exit(context,()->code));
    }
    int execute(ApplicationArguments args, PrintStream out, PrintStream err) {
        // LEARNER TODO Day 124: validate a single explicit action/key and its exact required arguments;
        // reject ambiguous modes and malformed inputs before any store call.
        throw new UnsupportedOperationException("Day 124 learner TODO: validate one-action recovery command arguments");
    }
    private static String single(ApplicationArguments args,String option) {
        List<String> values=args.getOptionValues(option);
        if(values==null || values.size()!=1 || values.getFirst().isBlank()) throw new IllegalArgumentException("one --"+option+" value required");
        return values.getFirst();
    }
}
