package coverstory.modules.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import coverstory.modules.catalog.api.CAP52Controller;
import coverstory.modules.catalog.api.CAP52Seam;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.concurrent.atomic.AtomicInteger;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class CAP52ControllerInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-52/synthetic-v1.json");
    private static final String VALID = "{\"sourceId\":\"synthetic-source\",\"sourceVersion\":\"v1\",\"canonicalHash\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"}";
    private static final CAP52Seam.Principal EDITOR = new CAP52Seam.Principal("editor-1", CAP52Seam.Role.EDITOR);
    private static final CAP52Seam.Principal APPLICANT = new CAP52Seam.Principal("applicant-1", CAP52Seam.Role.APPLICANT);

    @Test void fixtureStatesItsSyntheticAndNonDurableScope() throws Exception {
        JsonNode fixture = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_IMPORT_CONTRACT", fixture.path("classification").asText());
        assertEquals(201, fixture.path("expected").path("first").asInt());
        assertEquals(409, fixture.path("expected").path("sameKeyChangedHash").asInt());
        assertFalse(fixture.path("durable_persistence").asBoolean());
        assertFalse(fixture.path("authentication_adapter_wired").asBoolean());
        assertFalse(fixture.path("consumer_data").asBoolean());
    }

    @Test void untrustedOrWrongRoleRequestsAreRejectedBeforeTheLearnerSeam() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        MockMvc mvc = mvc(calls);
        mvc.perform(post("/api/v1/catalog/imports").contentType("application/json").content(VALID))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/catalog/imports").with(actor(APPLICANT)).contentType("application/json").content(VALID))
                .andExpect(status().isForbidden());
        assertEquals(0, calls.get());
    }

    @Test void callerIdentityOverridesAndMalformedHashesAreRejectedBeforeTheLearnerSeam() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        MockMvc mvc = mvc(calls);
        mvc.perform(post("/api/v1/catalog/imports").with(actor(EDITOR)).contentType("application/json")
                        .content(VALID.substring(0, VALID.length() - 1) + ",\"ownerId\":\"forged\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/catalog/imports").with(actor(EDITOR)).contentType("application/json")
                        .content("{\"sourceId\":\"s\",\"sourceVersion\":\"v1\",\"canonicalHash\":\"blank\"}"))
                .andExpect(status().isBadRequest());
        assertEquals(0, calls.get());
    }

    @Test void thinTransportMapsTheFakeSeamResultAndCallsItOnce() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        MockMvc mvc = standaloneSetup(new CAP52Controller((principal, request) -> {
                assertEquals(EDITOR, principal);
                assertEquals("synthetic-source", request.sourceId());
                calls.incrementAndGet();
                return CAP52Seam.Decision.ACCEPTED;
        })).build();
        mvc.perform(post("/api/v1/catalog/imports").with(actor(EDITOR)).contentType("application/json").content(VALID))
                .andExpect(status().isCreated());
        assertEquals(1, calls.get());
    }

    private static MockMvc mvc(AtomicInteger calls) {
        return standaloneSetup(new CAP52Controller((principal, request) -> {
                calls.incrementAndGet();
                return CAP52Seam.Decision.ACCEPTED;
        })).build();
    }

    private static RequestPostProcessor actor(CAP52Seam.Principal principal) {
        return request -> { request.setAttribute("trustedEditor", principal); return request; };
    }
}
