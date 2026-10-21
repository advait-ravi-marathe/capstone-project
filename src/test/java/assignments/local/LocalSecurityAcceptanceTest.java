package assignments.local;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import assignments.sprint5.Sprint5LocalApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import assignments.insurance.PolicyLifecycleService;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Sprint5LocalApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "server.port=0")
@AutoConfigureMockMvc
@ActiveProfiles({"local", "sprint5-local"})
@Tag("learnerAcceptance")
class LocalSecurityAcceptanceTest {
    private static EmbeddedPostgres postgres;

    @Autowired MockMvc mvc;
    @Autowired LocalPolicyOwnerResolver ownerResolver;
    @Autowired javax.sql.DataSource dataSource;
    @MockitoSpyBean LocalPolicyIssueController issueController;
    @MockitoSpyBean PolicyLifecycleService lifecycleService;

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

    @AfterAll static void closeDatabase() throws Exception { if (postgres != null) postgres.close(); }

    @Test
    void day61AnonymousApiDeniedAssetsAllowedAndOnlySyntheticSessionAccepted() throws Exception {
        mvc.perform(get("/api/session")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/policies/policy-renewal-a/renewal-preview"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/session").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"owner-a\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
        MvcResult ownerASelection = mvc.perform(post("/api/session").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"owner-a\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ownerId").value("owner-a"))
                .andReturn();
        MockHttpSession ownerASession = (MockHttpSession) ownerASelection.getRequest().getSession(false);
        assertNotNull(ownerASession);
        String ownerASessionId = ownerASession.getId();
        MvcResult selected = mvc.perform(post("/api/session").session(ownerASession).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"owner-b\"}"))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession saved = (MockHttpSession) selected.getRequest().getSession(false);
        assertNotNull(saved);
        assertNotEquals(ownerASessionId, saved.getId(), "switching owner rotates the current session ID");
        mvc.perform(get("/api/session").session(saved)).andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value("owner-b"));
        mvc.perform(post("/api/session").session(saved).with(csrf().asHeader().useInvalidToken())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ownerId\":\"owner-a\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/session").session(saved).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"someone-real\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("UNKNOWN_SYNTHETIC_OWNER"));
        mvc.perform(post("/api/session").session(saved).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":null}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("UNKNOWN_SYNTHETIC_OWNER"));
        mvc.perform(post("/api/session").session(saved).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("UNKNOWN_SYNTHETIC_OWNER"));
        mvc.perform(get("/api/session").session(saved)).andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerId").value("owner-b"));
        mvc.perform(post("/api/session").session(saved).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"owner-a\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void day62PrincipalOwnsReadsAndForgedOwnerIsIgnored() {
        var principal = UsernamePasswordAuthenticationToken.authenticated(
                "owner-a", "", AuthorityUtils.createAuthorityList("ROLE_TRAINING_USER"));
        assertEquals("owner-a", ownerResolver.ownerId(principal).orElseThrow());
        assertEquals("owner-a", ownerResolver.ownerId(principal, "owner-b").orElseThrow());
        assertEquals("owner-a", ownerResolver.ownerId(principal, null).orElseThrow());
        assertEquals("owner-a", ownerResolver.ownerId(principal, " ").orElseThrow());
        var ownerB = UsernamePasswordAuthenticationToken.authenticated(
                "owner-b", "", AuthorityUtils.createAuthorityList("ROLE_TRAINING_USER"));
        assertEquals("owner-b", ownerResolver.ownerId(ownerB).orElseThrow());
        assertEquals("owner-b", ownerResolver.ownerId(ownerB, "owner-a").orElseThrow());
        assertEquals("owner-b", ownerResolver.ownerId(ownerB, null).orElseThrow());
        var anonymous = new AnonymousAuthenticationToken("test-key", "anonymousUser",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        assertTrue(ownerResolver.ownerId(null).isEmpty());
        assertTrue(ownerResolver.ownerId(UsernamePasswordAuthenticationToken.unauthenticated("owner-a", "")).isEmpty());
        assertTrue(ownerResolver.ownerId(anonymous).isEmpty());
        assertTrue(ownerResolver.ownerId(UsernamePasswordAuthenticationToken.authenticated(
                "unknown", "", AuthorityUtils.NO_AUTHORITIES)).isEmpty());
        assertTrue(ownerResolver.ownerId(UsernamePasswordAuthenticationToken.authenticated(
                " ", "", AuthorityUtils.NO_AUTHORITIES)).isEmpty());
        for (var rejected : java.util.List.of(
                UsernamePasswordAuthenticationToken.unauthenticated("owner-a", ""), anonymous,
                UsernamePasswordAuthenticationToken.authenticated("unknown", "", AuthorityUtils.NO_AUTHORITIES),
                UsernamePasswordAuthenticationToken.authenticated(" ", "", AuthorityUtils.NO_AUTHORITIES))) {
            assertTrue(ownerResolver.ownerId(rejected, "owner-a").isEmpty(),
                    "untrusted requested owner cannot make an invalid principal valid");
            assertTrue(ownerResolver.ownerId(rejected, "owner-b").isEmpty());
        }
        assertTrue(ownerResolver.ownerId(null, "owner-a").isEmpty());
        assertTrue(ownerResolver.ownerId(null, null).isEmpty());
    }

    @Test
    void day65InvalidCsrfCannotCreatePolicyRows() throws Exception {
        org.mockito.Mockito.clearInvocations(issueController, lifecycleService);
        MvcResult sessionResult = mvc.perform(post("/api/session").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"owner-a\"}"))
                .andExpect(status().isOk()).andReturn();
        MockHttpSession session = (MockHttpSession) sessionResult.getRequest().getSession(false);
        String request = "{\"journeyId\":\"journey-a\",\"quoteVersionId\":\"quote-a\",\"clientRequestId\":\"day65-no-token\",\"effectiveDate\":\"2026-12-20\"}";
        String beforeMissing = allLifecycleRows();
        mvc.perform(post("/api/policies").session(session).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isForbidden());
        assertEquals(beforeMissing, allLifecycleRows());
        org.mockito.Mockito.verify(issueController, org.mockito.Mockito.never())
                .issue(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        org.mockito.Mockito.verify(lifecycleService, org.mockito.Mockito.never())
                .issueHttpStatus(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        String beforeWrong = allLifecycleRows();
        mvc.perform(post("/api/policies").session(session).with(csrf().asHeader().useInvalidToken())
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isForbidden());
        assertEquals(beforeWrong, allLifecycleRows());
        org.mockito.Mockito.verify(issueController, org.mockito.Mockito.never())
                .issue(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        org.mockito.Mockito.verify(lifecycleService, org.mockito.Mockito.never())
                .issueHttpStatus(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        String beforeAnonymousWithToken = allLifecycleRows();
        mvc.perform(post("/api/policies").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isUnauthorized());
        assertEquals(beforeAnonymousWithToken, allLifecycleRows());
        org.mockito.Mockito.verify(issueController, org.mockito.Mockito.never())
                .issue(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        org.mockito.Mockito.verify(lifecycleService, org.mockito.Mockito.never())
                .issueHttpStatus(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        // Valid-token positive control reaches the real route; unresolved earlier learner TODOs fail here.
        mvc.perform(post("/api/policies").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated());
    }

    private long countPolicies() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) {
            assertTrue(rows.next());
            return rows.getLong(1);
        }
    }

    private String allLifecycleRows() throws Exception {
        return table("training_policy_lifecycle", "policy_id") + "\n"
                + table("training_policy_lifecycle_event", "policy_id, version") + "\n"
                + table("training_policy_idempotency", "owner_id, client_request_id");
    }

    private String table(String name, String order) throws Exception {
        try (var connection = dataSource.getConnection(); var statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT COALESCE(jsonb_agg(to_jsonb(t) ORDER BY " + order
                     + "), '[]'::jsonb)::text FROM " + name + " t")) {
            assertTrue(rows.next());
            return rows.getString(1);
        }
    }
}
