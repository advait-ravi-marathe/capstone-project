package assignments.delivery.recovery;

import assignments.delivery.IssuanceSourceKey;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;

/** Executes one explicit read-only inspection and emits one framed report after complete encoding. */
public final class ReleaseInspectionCommandRunner implements ApplicationRunner {
    private final ReleaseInspectionRequestPolicy requests;
    private final ReleaseSchemaVerifier schema;
    private final ManualRecoveryService recovery;
    private final InspectionReportMapper reports;
    private final InspectionReportCodec codec;
    private final ConfigurableApplicationContext context;

    public ReleaseInspectionCommandRunner(ReleaseInspectionRequestPolicy requests, ReleaseSchemaVerifier schema,
            ManualRecoveryService recovery, InspectionReportMapper reports, InspectionReportCodec codec,
            ConfigurableApplicationContext context) {
        this.requests=requests; this.schema=schema; this.recovery=recovery; this.reports=reports; this.codec=codec; this.context=context;
    }

    @Override public void run(ApplicationArguments args) {
        if (!args.containsOption("coverstory.release.inspect")) return;
        int code = 1;
        try {
            ReleaseInspectionRequest request = requests.parse(toArgs(args));
            InspectionReport report = inspectOnce(request);
            System.out.println(codec.encode(report));
            code = 0;
        } catch (Exception failure) {
            // Keep diagnostics generic: no URL, credentials, SQL, or raw exception content.
            System.err.println("release inspection failed (" + failure.getClass().getSimpleName() + "); no success report was emitted");
        }
        int exitCode = code;
        System.exit(SpringApplication.exit(context, () -> exitCode));
    }

    InspectionReport inspectOnce(ReleaseInspectionRequest request) {
        schema.requireV001ThroughV005();
        RecoveryReadResult result = recovery.inspect(request.key());
        return reports.map(result);
    }

    private static String[] toArgs(ApplicationArguments args) {
        return args.getSourceArgs();
    }
}
