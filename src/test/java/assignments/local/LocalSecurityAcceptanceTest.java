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
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LocalCoverStoryApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "server.port=0")
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Tag("learnerAcceptance")
class LocalSecurityAcceptanceTest {
    private static EmbeddedPostgres postgres;

    @Autowired MockMvc mvc;
    @Autowired LocalPolicyOwnerResolver ownerResolver;
    @Autowired javax.sql.DataSource dataSource;

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
        mvc.perform(get("/api/policies/policy-renewal-a/renewal-preview"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CoverStory Local Training")));
        mvc.perform(post("/api/session").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"owner-a\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ownerId").value("owner-a"))
                .andExpect(result -> assertTrue(result.getRequest().getSession(false) instanceof MockHttpSession));
        mvc.perform(post("/api/session").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"someone-real\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("UNKNOWN_SYNTHETIC_OWNER"));
    }

    @Test
    void day62PrincipalOwnsReadsAndForgedOwnerIsIgnored() {
        var principal = UsernamePasswordAuthenticationToken.authenticated(
                "owner-a", "", AuthorityUtils.createAuthorityList("ROLE_TRAINING_USER"));
        assertEquals("owner-a", ownerResolver.ownerId(principal).orElseThrow());
        assertEquals("owner-a", ownerResolver.ownerId(principal, "owner-b").orElseThrow());
    }

    @Test
    void day65InvalidCsrfCannotCreatePolicyRows() throws Exception {
        long before = countPolicies();
        mvc.perform(post("/api/policies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"journeyId\":\"journey-new\",\"quoteVersionId\":\"quote-new\",\"clientRequestId\":\"request-new\",\"effectiveDate\":\"2026-12-20\"}"))
                .andExpect(status().isForbidden());
        assertEquals(before, countPolicies());
    }

    private long countPolicies() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT count(*) FROM training_policy_lifecycle")) {
            assertTrue(rows.next());
            return rows.getLong(1);
        }
    }
}
