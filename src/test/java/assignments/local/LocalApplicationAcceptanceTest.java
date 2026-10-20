package assignments.local;

import assignments.insurance.PolicyIssueRequest;
import assignments.insurance.PolicyLifecycleService;
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

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = LocalCoverStoryApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "server.port=0")
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Tag("learnerAcceptance")
class LocalApplicationAcceptanceTest {
    private static EmbeddedPostgres postgres;

    @Autowired LocalApplicationComposition.Components components;
    @Autowired Clock clock;
    @Autowired DataSource dataSource;

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
    }

    @Test
    @Tag("learnerAcceptance")
    void day60OwnerScopedPreviewReadDoesNotWrite() {
        long before = countPolicies();
        assertTrue(components.renewalReadStore().findByPolicyIdAndOwnerId("policy-renewal-a", "owner-a").isPresent());
        assertTrue(components.renewalReadStore().findByPolicyIdAndOwnerId("policy-renewal-a", "owner-b").isEmpty());
        assertEquals(before, countPolicies());
    }

    @Test
    void day64IssueAdapterDelegatesExactlyOnceAndReturnsTheServiceSnapshot() {
        PolicyLifecycleService lifecycle = mock(PolicyLifecycleService.class);
        LocalPolicyOwnerResolver ownerResolver = mock(LocalPolicyOwnerResolver.class);
        DataSource source = mock(DataSource.class);
        Authentication authentication = mock(Authentication.class);
        PolicyIssueRequest request = new PolicyIssueRequest("journey-a", "quote-a", "request-a",
                LocalDate.parse("2026-12-20"));
        PolicySnapshot expected = new PolicySnapshot("policy-a", "owner-a", "journey-a", "quote-a",
                "home-secure-v1", "home-secure-rate-v1", new BigDecimal("125.00"), "INR",
                request.effectiveDate(), PolicyStatus.ISSUED, 1, List.of(), null);
        when(ownerResolver.ownerId(authentication)).thenReturn(Optional.of("owner-a"));
        when(lifecycle.persistIssueAtomically(source, request, "owner-a")).thenReturn(expected);

        var response = new LocalPolicyIssueController(lifecycle, ownerResolver, source).issue(request, authentication);

        assertEquals(201, response.getStatusCode().value());
        assertSame(expected, response.getBody());
        verify(lifecycle, times(1)).persistIssueAtomically(source, request, "owner-a");
    }

    private long countPolicies() {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) {
            assertTrue(rows.next());
            return rows.getLong(1);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
