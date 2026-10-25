package assignments.oidc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Instant;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/** Actual Spring Security bearer/JWKS/discovery path against a loopback-only synthetic issuer. */
@SpringBootTest(classes = OidcContractLabInfrastructureTest.TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("oidc-contract-lab")
class OidcContractLabInfrastructureTest {
    private static final LocalOidcTestProvider PROVIDER = LocalOidcTestProvider.start();
    @Autowired private MockMvc mvc;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({OidcContractLabSecurityConfiguration.class, OidcContractLabController.class, ReferencePolicyConfiguration.class})
    static class TestApplication { }

    @TestConfiguration
    static class ReferencePolicyConfiguration {
        @Bean @Primary OidcTenantPolicy referenceTenantPolicy() { return new ReferenceOidcTenantPolicy(); }
    }

    @DynamicPropertySource
    static void oidcProperties(DynamicPropertyRegistry properties) {
        properties.add("assignments.oidc.issuer", PROVIDER::issuer);
        properties.add("spring.security.oauth2.client.registration.local.client-id", () -> "coverstory-login-lab");
        properties.add("spring.security.oauth2.client.registration.local.client-secret", () -> "synthetic-secret");
        properties.add("spring.security.oauth2.client.registration.local.authorization-grant-type", () -> "authorization_code");
        properties.add("spring.security.oauth2.client.registration.local.redirect-uri", () -> "{baseUrl}/login/oauth2/code/{registrationId}");
        properties.add("spring.security.oauth2.client.registration.local.scope", () -> "openid,profile");
        properties.add("spring.security.oauth2.client.provider.local.issuer-uri", PROVIDER::issuer);
    }

    @AfterAll static void stopProvider() { PROVIDER.close(); }

    @Test void signedTokenDiscoveredThroughLocalIssuerReachesProtectedHandler() throws Exception {
        mvc.perform(get("/api/oidc/tenant/tenant-a/read").header("Authorization", "Bearer " + PROVIDER.validToken("tenant-a")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.subject").value("synthetic-advait"))
                .andExpect(jsonPath("$.tenant").value("tenant-a"));
    }

    @Test void rotationAcceptsNewKidAndRejectsUnknownSigningKey() throws Exception {
        String first = PROVIDER.validToken("tenant-a");
        mvc.perform(get("/api/oidc/tenant/tenant-a/read").header("Authorization", "Bearer " + first))
                .andExpect(status().isOk());
        String rotated = PROVIDER.rotateAndIssue("tenant-a");
        mvc.perform(get("/api/oidc/tenant/tenant-a/read").header("Authorization", "Bearer " + first))
                .andExpect(status().isOk());
        mvc.perform(get("/api/oidc/tenant/tenant-a/read").header("Authorization", "Bearer " + rotated))
                .andExpect(status().isOk());
        // Refresh for the new kid must retain overlap verification for the previous key.
        mvc.perform(get("/api/oidc/tenant/tenant-a/read").header("Authorization", "Bearer " + first))
                .andExpect(status().isOk());
        mvc.perform(get("/api/oidc/tenant/tenant-a/read").header("Authorization", "Bearer " + PROVIDER.issueWithUnknownKid("tenant-a")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(not(containsString("synthetic-advait"))));
    }

    @Test void invalidTokenStructuresFailClosed() throws Exception {
        String unknownKid = PROVIDER.issueWithUnknownKid("tenant-a");
        String unsigned = PROVIDER.unsignedToken("tenant-a");
        String badSignature = PROVIDER.tokenWithInvalidSignature();
        String malformed = "not.a.jwt";
        for (String token : new String[]{unknownKid, unsigned, badSignature, malformed}) {
            mvc.perform(get("/api/oidc/tenant/tenant-a/read").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(not(containsString("synthetic-advait"))));
        }
    }

    @Test void issuerAudienceAndExpiryFailuresReturn401WithoutClaims() throws Exception {
        Instant now = Instant.now();
        String wrongIssuer = PROVIDER.issue("tenant-a", "coverstory-oidc-lab", "policy.read", PROVIDER.currentKid(),
                "http://127.0.0.1:1/wrong-issuer", now.minusSeconds(1), now.plusSeconds(60));
        String wrongAudience = PROVIDER.issue("tenant-a", "other-resource", "policy.read", PROVIDER.currentKid(),
                PROVIDER.issuer(), now.minusSeconds(1), now.plusSeconds(60));
        String expired = PROVIDER.issue("tenant-a", "coverstory-oidc-lab", "policy.read", PROVIDER.currentKid(),
                PROVIDER.issuer(), now.minusSeconds(120), now.minusSeconds(60));
        for (String token : new String[]{wrongIssuer, wrongAudience, expired, PROVIDER.tokenWithoutAudience("tenant-a"),
                PROVIDER.tokenWithInvalidSignature(), PROVIDER.unsignedToken("tenant-a"), "not.a.jwt"}) {
            mvc.perform(get("/api/oidc/tenant/tenant-a/read").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(not(containsString("synthetic-advait"))));
        }
    }

    @Test void springOauth2LoginPerformsStateNonceAuthorizationCodeAndTokenExchange() throws Exception {
        LoginFlow flow = authorizeThroughProvider();
        mvc.perform(get(flow.callbackPath()).param("code", flow.code()).param("state", flow.state()).session(flow.session()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/api/oidc/login").session(flow.session()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.subject").value("synthetic-advait"))
                .andExpect(jsonPath("$.issuer").value(PROVIDER.issuer()));
    }

    @Test void oauthCallbackRejectsMismatchedStateAndNeverCreatesAuthenticatedSession() throws Exception {
        LoginFlow flow = authorizeThroughProvider();
        mvc.perform(get(flow.callbackPath()).param("code", flow.code()).param("state", flow.state() + "-tampered")
                        .session(flow.session()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/api/oidc/login").session(flow.session()))
                .andExpect(status().is3xxRedirection());
    }

    @Test void oauthClientRejectsIdTokenWithWrongNonceAfterTokenExchange() throws Exception {
        LoginFlow flow = authorizeThroughProvider();
        PROVIDER.mismatchNextNonce();
        mvc.perform(get(flow.callbackPath()).param("code", flow.code()).param("state", flow.state()).session(flow.session()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/api/oidc/login").session(flow.session()))
                .andExpect(status().is3xxRedirection());
    }

    private LoginFlow authorizeThroughProvider() throws Exception {
        var start = mvc.perform(get("/oauth2/authorization/local")).andExpect(status().is3xxRedirection()).andReturn();
        MockHttpSession session = (MockHttpSession) start.getRequest().getSession(false);
        assertNotNull(session, "Spring must retain the generated state and nonce in the session");
        String authorization = start.getResponse().getRedirectedUrl();
        HttpResponse<Void> response = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build()
                .send(HttpRequest.newBuilder(URI.create(authorization)).GET().build(), HttpResponse.BodyHandlers.discarding());
        assertEquals(302, response.statusCode(), "local issuer must return an authorization-code redirect");
        URI callback = URI.create(response.headers().firstValue("Location").orElseThrow());
        Map<String, String> query = query(callback.getRawQuery());
        assertNotNull(query.get("code"));
        assertNotNull(query.get("state"));
        return new LoginFlow(session, callback.getPath(), query.get("code"), query.get("state"));
    }

    private static Map<String, String> query(String raw) {
        java.util.Map<String, String> values = new java.util.HashMap<>();
        for (String pair : raw.split("&")) {
            String[] parts = pair.split("=", 2);
            values.put(java.net.URLDecoder.decode(parts[0], java.nio.charset.StandardCharsets.UTF_8),
                    parts.length == 2 ? java.net.URLDecoder.decode(parts[1], java.nio.charset.StandardCharsets.UTF_8) : "");
        }
        return values;
    }

    private record LoginFlow(MockHttpSession session, String callbackPath, String code, String state) { }

    @Test void foreignTenantAndInsufficientScopeAreDeniedBeforePrivateResponse() throws Exception {
        String foreign = PROVIDER.validToken("tenant-a");
        String noScope = PROVIDER.validTokenWithScope("tenant-a", "policy.write");
        for (var attempt : new Object[][]{{"tenant-b", foreign}, {"tenant-a", noScope}}) {
            mvc.perform(get("/api/oidc/tenant/" + attempt[0] + "/read").header("Authorization", "Bearer " + attempt[1]))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(not(containsString("synthetic-advait"))));
        }
    }
}
