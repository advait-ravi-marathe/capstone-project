package coverstory.modules.payments;

import coverstory.modules.payments.api.CAP80ReplayAction;
import coverstory.modules.payments.api.CAP80ReplayCommand;
import coverstory.modules.payments.api.CAP80ReplayController;
import coverstory.modules.payments.api.CAP80Seam;
import coverstory.modules.payments.api.MockConfirmationFeedPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.util.Optional;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Full authenticated HTTP -> feed -> action -> PostgreSQL transaction witness.
 * The positive action is instructor reference code; default production wiring still calls CAP80Seam.
 */
class CAP80ReplayRuntimePathIntegrationTest {
    private static final UUID A = UUID.fromString("a1000000-0000-4000-8000-000000000001");
    private static final UUID APP_A = UUID.fromString("a2000000-0000-4000-8000-000000000001");
    private static final UUID B = UUID.fromString("b1000000-0000-4000-8000-000000000002");
    private static final UUID APP_B = UUID.fromString("b2000000-0000-4000-8000-000000000002");

    @Test void realJdbcFeedReachesTheLearnerSeamAndReturns501WithoutEffects() throws Exception {
        try (RuntimeDatabase db = new RuntimeDatabase()) {
            MockMvc mvc = mvc(db.feed(), new CAP80Seam()::applyConfirmation);
            assertEquals(501, replay(mvc, A, "evt-3").getResponse().getStatus());
            assertEquals(2, db.checkpoint(A));
            assertEquals(0, db.count("payments.mock_capture_effect"));
            assertEquals("AWAITING_PAYMENT", db.state(APP_A));
        }
    }

    @Test void realJdbcFeedRollsBackAtInjectedFaultThenRetriesOnceThroughTheHttpCommand() throws Exception {
        try (RuntimeDatabase db = new RuntimeDatabase()) {
            AtomicReference<Boolean> failBeforeCheckpoint = new AtomicReference<>(true);
            MockConfirmationFeedPort feed = withFault(db.feed(), failBeforeCheckpoint);
            MockMvc mvc = mvc(feed, CAP80ReplayRuntimePathIntegrationTest::referenceApply);
            assertEquals(503, replay(mvc, A, "evt-3").getResponse().getStatus());
            assertEquals(2, db.checkpoint(A));
            assertEquals(0, db.count("payments.mock_capture_effect"));
            assertEquals(0, db.count("application.onboarding_timeline"));
            assertEquals("AWAITING_PAYMENT", db.state(APP_A));
            failBeforeCheckpoint.set(false);
            assertEquals(200, replay(mvc, A, "evt-3").getResponse().getStatus());
            assertEquals(3, db.checkpoint(A));
            assertEquals(1, db.count("payments.mock_capture_effect"));
            assertEquals(1, db.count("application.onboarding_timeline"));
            assertEquals("ONBOARDING", db.state(APP_A));
            assertTrue(replay(mvc, A, "evt-3").getResponse().getContentAsString().contains("ALREADY_APPLIED"));
            assertEquals(1, db.count("payments.mock_capture_effect"));
        }
    }

    @Test void realJdbcFeedIsOwnerScopedBeforeAnyReplayTransaction() throws Exception {
        try (RuntimeDatabase db = new RuntimeDatabase()) {
            MockMvc mvc = mvc(db.feed(), CAP80ReplayRuntimePathIntegrationTest::referenceApply);
            assertEquals(404, replay(mvc, B, "evt-3").getResponse().getStatus());
            assertEquals(2, db.checkpoint(B));
            assertEquals(2, db.checkpoint(A));
            assertEquals(0, db.count("payments.mock_capture_effect"));
        }
    }

    @Test void httpReplayCommitsOnceAndDuplicateAndGapHaveNoAdditionalEffects() throws Exception {
        try (CAP80JdbcHarness db = new CAP80JdbcHarness(A, APP_A, B, APP_B)) {
            MockMvc mvc = mvc(new Feed(db, CAP80JdbcHarness.Fault.NONE), CAP80ReplayRuntimePathIntegrationTest::referenceApply);
            assertEquals(200, replay(mvc, A, "evt-3").getResponse().getStatus());
            assertTrue(replay(mvc, A, "evt-3").getResponse().getContentAsString().contains("ALREADY_APPLIED"));
            assertTrue(replay(mvc, A, "evt-5").getResponse().getContentAsString().contains("GAP"));
            assertEquals(3, db.checkpoint(A));
            assertEquals(1, db.mockCaptureCount(A));
            assertEquals(1, db.onboardingCount(A));
            assertEquals(1, db.timelineCount(A));
            assertEquals("100000|INR", db.mockCaptureMoney(A));
            assertEquals(0, db.mockCaptureCount(B));
        }
    }

    @Test void failureAfterEffectRollsBackAndTheSameAuthoritativeEventCanRecover() throws Exception {
        try (CAP80JdbcHarness db = new CAP80JdbcHarness(A, APP_A, B, APP_B)) {
            AtomicReference<CAP80JdbcHarness.Fault> fault = new AtomicReference<>(CAP80JdbcHarness.Fault.BEFORE_CHECKPOINT);
            MockMvc mvc = mvc(new Feed(db, fault), CAP80ReplayRuntimePathIntegrationTest::referenceApply);
            assertEquals(503, replay(mvc, A, "evt-3").getResponse().getStatus());
            assertEquals(2, db.checkpoint(A));
            assertEquals(0, db.mockCaptureCount(A));
            assertEquals(0, db.onboardingCount(A));
            assertEquals(0, db.timelineCount(A));
            fault.set(CAP80JdbcHarness.Fault.NONE);
            assertEquals(200, replay(mvc, A, "evt-3").getResponse().getStatus());
            assertEquals(3, db.checkpoint(A));
            assertEquals(1, db.mockCaptureCount(A));
            assertEquals(1, db.onboardingCount(A));
            assertEquals(1, db.timelineCount(A));
        }
    }

    @Test void anotherOwnerCannotResolveTheConfirmationOrReadItsCheckpoint() throws Exception {
        try (CAP80JdbcHarness db = new CAP80JdbcHarness(A, APP_A, B, APP_B)) {
            Feed feed = new Feed(db, CAP80JdbcHarness.Fault.NONE);
            MockMvc mvc = mvc(feed, CAP80ReplayRuntimePathIntegrationTest::referenceApply);
            assertEquals(404, replay(mvc, B, "evt-3").getResponse().getStatus());
            assertEquals(0, db.transactionCalls());
            assertEquals(0, db.mockCaptureCount(B));
            assertEquals(0, db.mockCaptureCount(A));
        }
    }

    private static CAP80Seam.ReplayResult referenceApply(CAP80Seam.OwnerPaymentItem item,
            CAP80Seam.PaymentCheckpoint supplied, CAP80Seam.PaymentEffectPort port) {
        if (!item.actorOwnerId().equals(item.feedOwnerId()) || !item.actorOwnerId().equals(supplied.ownerId()))
            return new CAP80Seam.ReplayResult(CAP80Seam.Status.FORBIDDEN, supplied.lastAppliedSequence());
        return port.inUnitOfWork(item.feedOwnerId(), tx -> {
            CAP80Seam.PaymentCheckpoint current = tx.lockCheckpoint(item.feedOwnerId());
            if (current == null || !current.ownerId().equals(item.feedOwnerId()))
                return new CAP80Seam.ReplayResult(CAP80Seam.Status.FORBIDDEN, 0);
            if (item.sequence() <= current.lastAppliedSequence())
                return new CAP80Seam.ReplayResult(CAP80Seam.Status.ALREADY_APPLIED, current.lastAppliedSequence());
            if (item.sequence() != current.lastAppliedSequence() + 1)
                return new CAP80Seam.ReplayResult(CAP80Seam.Status.GAP, current.lastAppliedSequence());
            tx.appendMockCapture(item);
            tx.unlockOnboarding(item.feedOwnerId(), item);
            tx.advanceCheckpoint(item.feedOwnerId(), item.sequence());
            return new CAP80Seam.ReplayResult(CAP80Seam.Status.APPLIED, item.sequence());
        });
    }

    private static MockMvc mvc(MockConfirmationFeedPort feed, CAP80ReplayAction action) {
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        beans.registerSingleton("command", new CAP80ReplayCommand(feed, action));
        return MockMvcBuilders.standaloneSetup(new CAP80ReplayController(beans.getBeanProvider(CAP80ReplayCommand.class))).build();
    }

    private static MockConfirmationFeedPort withFault(MockConfirmationFeedPort delegate, AtomicReference<Boolean> fail) {
        return new MockConfirmationFeedPort() {
            public Optional<CAP80Seam.OwnerPaymentItem> findOwned(UUID owner, String event) { return delegate.findOwned(owner,event); }
            public Optional<CAP80Seam.PaymentCheckpoint> currentCheckpoint(UUID owner) { return delegate.currentCheckpoint(owner); }
            public CAP80Seam.PaymentEffectPort effectPort() {
                var underlying = delegate.effectPort();
                return (owner, work) -> underlying.inUnitOfWork(owner, tx -> work.apply(new CAP80Seam.ReplayTransaction() {
                    public CAP80Seam.PaymentCheckpoint lockCheckpoint(UUID id) { return tx.lockCheckpoint(id); }
                    public void appendMockCapture(CAP80Seam.OwnerPaymentItem item) { tx.appendMockCapture(item); }
                    public void unlockOnboarding(UUID id, CAP80Seam.OwnerPaymentItem item) { tx.unlockOnboarding(id,item); }
                    public void advanceCheckpoint(UUID id, int sequence) {
                        if (fail.compareAndSet(true,false)) throw new IllegalStateException("injected post-effect failure");
                        tx.advanceCheckpoint(id,sequence);
                    }
                }));
            }
        };
    }
    private static MvcResult replay(MockMvc mvc, UUID owner, String event) throws Exception {
        return mvc.perform(post("/api/v1/me/payments/mock-confirmations/{event}/replay", event).with(auth(owner))).andReturn();
    }
    private static RequestPostProcessor auth(UUID owner) { return request -> { request.setUserPrincipal(owner::toString); return request; }; }

    private static final class Feed implements MockConfirmationFeedPort {
        private final CAP80JdbcHarness db;
        private final AtomicReference<CAP80JdbcHarness.Fault> fault;
        Feed(CAP80JdbcHarness db, CAP80JdbcHarness.Fault fault) { this(db, new AtomicReference<>(fault)); }
        Feed(CAP80JdbcHarness db, AtomicReference<CAP80JdbcHarness.Fault> fault) { this.db=db; this.fault=fault; }
        @Override public Optional<CAP80Seam.OwnerPaymentItem> findOwned(UUID owner, String event) {
            if (!A.equals(owner) || !(event.equals("evt-3") || event.equals("evt-5"))) return Optional.empty();
            int sequence = event.equals("evt-3") ? 3 : 5;
            return Optional.of(new CAP80Seam.OwnerPaymentItem(owner, owner, event, sequence, APP_A,
                    "quote-health-A-v1", 100000, "INR", true));
        }
        @Override public Optional<CAP80Seam.PaymentCheckpoint> currentCheckpoint(UUID owner) {
            if (!A.equals(owner)) return Optional.empty();
            try { return Optional.of(new CAP80Seam.PaymentCheckpoint(owner, db.checkpoint(owner))); }
            catch (java.sql.SQLException failure) { throw new IllegalStateException(failure); }
        }
        @Override public CAP80Seam.PaymentEffectPort effectPort() { return (owner, work) -> db.port(fault.get()).inUnitOfWork(owner, work); }
    }

    private static final class RuntimeDatabase implements AutoCloseable {
        private final EmbeddedPostgres pg;
        private final JdbcTemplate jdbc;
        private final JdbcMockConfirmationFeedAdapter feed;
        RuntimeDatabase() throws Exception {
            pg=EmbeddedPostgres.builder().start();
            String url;
            try (Connection c=pg.getPostgresDatabase().getConnection()) { url=c.getMetaData().getURL(); }
            var ds=new SimpleDriverDataSource(new org.postgresql.Driver(),url,"postgres","postgres");
            jdbc=new JdbcTemplate(ds);
            try (Connection c=pg.getPostgresDatabase().getConnection(); Statement s=c.createStatement();
                 var in=getClass().getResourceAsStream("/db/schema/cap80-runtime.sql")) {
                String schema=new String(in.readAllBytes(), StandardCharsets.UTF_8);
                for (String sql:schema.split(";")) if(!sql.isBlank()) s.execute(sql);
            }
            seed(A,APP_A,true); seed(B,APP_B,false);
            feed=new JdbcMockConfirmationFeedAdapter(jdbc,new DataSourceTransactionManager(ds),new coverstory.modules.application.OnboardingPaymentTransitionAdapter(jdbc));
        }
        private void seed(UUID owner, UUID app, boolean hasConfirmation) {
            jdbc.update("INSERT INTO payments.payment_checkpoint VALUES (?,2)",owner);
            if(hasConfirmation) {
                jdbc.update("INSERT INTO payments.mock_confirmation_feed VALUES ('evt-3',?,3,?,'quote-health-A-v1',100000,'INR',true)",owner,app);
                jdbc.update("INSERT INTO payments.mock_confirmation_feed VALUES ('evt-5',?,5,?,'quote-health-A-v1',100000,'INR',true)",owner,app);
            }
            jdbc.update("INSERT INTO application.onboarding_case VALUES (?,?,'quote-health-A-v1','AWAITING_PAYMENT','evt-pending')",app,owner);
        }
        MockConfirmationFeedPort feed(){return feed;}
        int count(String table){return jdbc.queryForObject("SELECT count(*) FROM "+table,Integer.class);}
        int checkpoint(UUID owner){return jdbc.queryForObject("SELECT last_sequence FROM payments.payment_checkpoint WHERE owner_id=?",Integer.class,owner);}
        String state(UUID app){return jdbc.queryForObject("SELECT state FROM application.onboarding_case WHERE application_id=?",String.class,app);}
        public void close() throws Exception { pg.close(); }
    }
}
