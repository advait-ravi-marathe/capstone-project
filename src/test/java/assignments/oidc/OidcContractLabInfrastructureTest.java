package assignments.oidc;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Instant;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        mvc.perform(get("/api/oidc/tenant/tenant-a/read").header("Authorization", "Bearer " + PROVIDER.issueWithUnknownKid("tenant-a")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(not(containsString("synthetic-advait"))));
    }

    @Test void issuerAudienceAndExpiryFailuresReturn401WithoutClaims() throws Exception {
        Instant now = Instant.now();
        String wrongIssuer = PROVIDER.issue("tenant-a", "coverstory-oidc-lab", "policy.read", PROVIDER.currentKid(),
                "http://127.0.0.1:1/wrong-issuer", now.minusSeconds(1), now.plusSeconds(60));
        String wrongAudience = PROVIDER.issue("tenant-a", "other-resource", "policy.read", PROVIDER.currentKid(),
                PROVIDER.issuer(), now.minusSeconds(1), now.plusSeconds(60));
        String expired = PROVIDER.issue("tenant-a", "coverstory-oidc-lab", "policy.read", PROVIDER.currentKid(),
                PROVIDER.issuer(), now.minusSeconds(120), now.minusSeconds(60));
        for (String token : new String[]{wrongIssuer, wrongAudience, expired, "not.a.jwt"}) {
            mvc.perform(get("/api/oidc/tenant/tenant-a/read").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(not(containsString("synthetic-advait"))));
        }
    }

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
