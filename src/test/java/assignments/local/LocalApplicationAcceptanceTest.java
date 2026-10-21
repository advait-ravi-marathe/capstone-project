package assignments.local;

import assignments.insurance.PolicyIssueRequest;
import assignments.insurance.PolicyLifecycleService;
import assignments.insurance.PolicyReadStore;
import assignments.insurance.PolicySnapshot;
import assignments.insurance.PolicyStatus;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import assignments.sprint5.Sprint5LocalApplication;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = Sprint5LocalApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "server.port=0")
@AutoConfigureMockMvc
@ActiveProfiles({"local", "sprint5-local"})
@Tag("learnerAcceptance")
class LocalApplicationAcceptanceTest {
    private static EmbeddedPostgres postgres;

    @Autowired LocalApplicationComposition.Components components;
    @Autowired Clock clock;
    @Autowired DataSource dataSource;
    @Autowired org.springframework.context.ApplicationContext context;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        try {
            if (postgres == null) postgres = EmbeddedPostgres.builder().start();
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "");
    }

    @AfterAll
    static void closeDatabase() throws Exception {
        if (postgres != null) postgres.close();
    }

    @Test
    void day58DedicatedEntryPointWiresClockAndDataSourceExplicitly() {
        assertSame(clock, components.clock());
        assertSame(dataSource, components.dataSource());
        assertNotNull(components.lifecycleService());
        assertNotNull(components.renewalPreviewPolicy());
        assertNotNull(components.policyReadStore());
        assertNotNull(components.renewalReadStore());
        assertFalse(context.containsBean("previewAdmission"));
        assertFalse(context.containsBean("previewReadBudgetPolicy"));
        assertFalse(context.containsBean("oneBatchDispatcher"));
        assertFalse(context.containsBean("manualRecoveryService"));
    }

    @Test
    @Tag("learnerAcceptance")
    void day60OwnerScopedPreviewReadDoesNotWrite() throws Exception {
        var renewalFixtureSnapshot = lifecycleRows();
        assertTrue(components.renewalReadStore().findByPolicyIdAndOwnerId("policy-renewal-a", "owner-a").isPresent());
        assertEquals(renewalFixtureSnapshot, lifecycleRows(), "first preview read must preserve every stored field");
        assertTrue(components.renewalReadStore().findByPolicyIdAndOwnerId("policy-renewal-a", "owner-b").isEmpty());
        assertEquals(renewalFixtureSnapshot, lifecycleRows(), "foreign preview read must preserve every stored field");

        try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO training_policy_lifecycle
                      (policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,
                       annual_premium_rupees,currency,effective_date,status,version,cancellation_effective_date)
                    VALUES ('policy-day60-b','owner-b','journey-day60-b','quote-day60-b','home-secure-v2',
                       'day60-rate-v2',987.40,'INR',DATE '2026-11-21','ISSUED',7,NULL)
                    """);
            statement.executeUpdate("INSERT INTO training_policy_lifecycle_event(policy_id,version,event_type,effective_date) VALUES ('policy-day60-b',7,'ISSUED',DATE '2026-11-21')");
            statement.executeUpdate("INSERT INTO training_policy_idempotency(owner_id,client_request_id,request_fingerprint,policy_id) VALUES ('owner-b','day60-sentinel','fingerprint-day60','policy-day60-b')");
        }
        var beforePolicyReads = lifecycleRows();
        assertEquals(new PolicySnapshot("policy-demo-1", "owner-a", "journey-demo-a", "quote-demo-a",
                        "home-secure-v1", "home-secure-rate-v1", new BigDecimal("125.00"), "INR",
                        LocalDate.parse("2026-12-01"), PolicyStatus.ISSUED, 1, List.of(), null),
                components.policyReadStore().findByPolicyIdAndOwnerId("policy-demo-1", "owner-a").orElseThrow());
        assertEquals(beforePolicyReads, lifecycleRows(), "original-row read must preserve all three full tables");
        assertEquals(new PolicySnapshot("policy-day60-b", "owner-b", "journey-day60-b", "quote-day60-b",
                        "home-secure-v2", "day60-rate-v2", new BigDecimal("987.40"), "INR",
                        LocalDate.parse("2026-11-21"), PolicyStatus.ISSUED, 7, List.of(), null),
                components.policyReadStore().findByPolicyIdAndOwnerId("policy-day60-b", "owner-b").orElseThrow());
        assertEquals(beforePolicyReads, lifecycleRows(), "changed-row read must preserve all three full tables");
        assertTrue(components.policyReadStore().findByPolicyIdAndOwnerId("policy-day60-b", "owner-a").isEmpty());
        assertEquals(beforePolicyReads, lifecycleRows(), "foreign policy read must preserve all three full tables");
        assertTrue(components.policyReadStore().findByPolicyIdAndOwnerId("policy-not-present", "owner-a").isEmpty());
        assertEquals(beforePolicyReads, lifecycleRows(), "missing policy read must preserve all three full tables");
    }

    @Test
    void day64IssueAdapterDelegatesExactlyOnceAndReturnsTheServiceSnapshot() {
        PolicyLifecycleService lifecycle = mock(PolicyLifecycleService.class);
        LocalPolicyOwnerResolver ownerResolver = mock(LocalPolicyOwnerResolver.class);
        DataSource source = mock(DataSource.class);
        PolicyReadStore readStore = mock(PolicyReadStore.class);
        PolicyIssueReceiptReadStore receiptReadStore = mock(PolicyIssueReceiptReadStore.class);
        Authentication authentication = mock(Authentication.class);
        PolicyIssueRequest request = new PolicyIssueRequest("journey-a", "quote-a", "request-a",
                LocalDate.parse("2026-12-20"));
        PolicySnapshot expected = new PolicySnapshot("policy-a", "owner-a", "journey-a", "quote-a",
                "home-secure-v1", "home-secure-rate-v1", new BigDecimal("125.00"), "INR",
                request.effectiveDate(), PolicyStatus.ISSUED, 1, List.of(), null);
        when(ownerResolver.ownerId(authentication)).thenReturn(Optional.of("owner-a"));
        when(receiptReadStore.find("owner-a", "request-a")).thenReturn(Optional.empty());
        when(lifecycle.issueHttpStatus(request, Optional.empty())).thenReturn(201);
        when(lifecycle.persistIssueAtomically(source, request, "owner-a")).thenReturn(expected);

        var response = new LocalPolicyIssueController(lifecycle, ownerResolver, source, readStore, receiptReadStore)
                .issue(request, authentication);

        assertEquals(201, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        verify(lifecycle, times(1)).persistIssueAtomically(source, request, "owner-a");
        verify(receiptReadStore).find("owner-a", "request-a");
    }

    private String lifecycleRows() throws Exception {
        return snapshotTable("training_policy_lifecycle", "policy_id") + "\n"
                + snapshotTable("training_policy_lifecycle_event", "policy_id,version") + "\n"
                + snapshotTable("training_policy_idempotency", "owner_id,client_request_id");
    }

    private String snapshotTable(String table, String order) throws Exception {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT COALESCE(jsonb_agg(to_jsonb(t) ORDER BY " + order
                     + "), '[]'::jsonb)::text FROM " + table + " t")) {
            assertTrue(rows.next());
            return rows.getString(1);
        }
    }
}
