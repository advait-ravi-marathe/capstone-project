package coverstory.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.*;

/** Learner behavior checks against the mapper TODO and prebuilt test-only exception sources. */
@Tag("learnerAcceptance")
class CAP83AcceptanceTest {
    private final ObjectMapper json = new ObjectMapper();
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new CAP83MockController())
            .setControllerAdvice(new CoverStoryExceptionHandler(new ReturnSafeCheckoutAndDraftErrors()))
            .build();

    @Test
    void malformedPayloadReturns400Req1AndDoesNotEchoPayload() throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/test/malformed").header("X-Request-Id", "req1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answer\":\"RAW_ANSWER_SENTINEL\"," )).andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsByteArray());
        assertEquals(400, result.getResponse().getStatus());
        assertEquals("INVALID_REQUEST", body.path("code").asText());
        assertEquals("req1", body.path("requestId").asText());
        assertFalse(result.getResponse().getContentAsString().contains("RAW_ANSWER_SENTINEL"));
    }

    @Test
    void foreignAndAbsentResourcesShareTheSame404() throws Exception {
        MvcResult absent = mvc.perform(get("/api/v1/test/absent").header("X-Request-Id", "same-id")).andReturn();
        MvcResult foreign = mvc.perform(get("/api/v1/test/foreign").header("X-Request-Id", "same-id")).andReturn();
        assertEquals(404, absent.getResponse().getStatus());
        assertEquals(404, foreign.getResponse().getStatus());
        assertEquals(absent.getResponse().getContentAsString(), foreign.getResponse().getContentAsString());
    }

    @Test
    void staleIfMatchMapsTo412() throws Exception {
        MvcResult result = mvc.perform(put("/api/v1/test/stale").header("If-Match", "v3").header("X-Request-Id", "stale-id")).andReturn();
        assertEquals(412, result.getResponse().getStatus());
        assertEquals("STALE_VERSION", json.readTree(result.getResponse().getContentAsByteArray()).path("code").asText());
        // The current React shell has no saved-draft compare flow, so unsaved-edit retention is not asserted here.
    }

    @Test
    void sqlFailureMapsToGeneric500WithoutSecretSentinel() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/test/sql").header("X-Request-Id", "sql-id")).andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsByteArray());
        assertEquals(500, result.getResponse().getStatus());
        assertEquals("INTERNAL_ERROR", body.path("code").asText());
        assertEquals("An unexpected error occurred.", body.path("message").asText());
        assertFalse(result.getResponse().getContentAsString().contains("SECRET_SENTINEL"));
    }

    @Test
    void missingRequestIdGetsGeneratedAndValidationErrorsStayFieldLinked() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/test/incomplete")).andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsByteArray());
        assertEquals(422, result.getResponse().getStatus());
        assertFalse(body.path("requestId").asText().isBlank());
        assertEquals("This field is required.", body.path("fieldErrors").path("fullName").asText());
    }

    @Test
    void legacyCorrelationHeaderIsReturnedOnlyAsRequestId() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/test/session").header("X-Correlation-Id", "legacy-id")).andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsByteArray());
        assertEquals(401, result.getResponse().getStatus());
        assertEquals("legacy-id", body.path("requestId").asText());
        assertFalse(body.has("correlationId"));
    }

    @Test
    void statusMapIncludesQuoteAndIdempotencyConflicts() throws Exception {
        MvcResult quote = mvc.perform(get("/api/v1/test/quote-conflict")).andReturn();
        MvcResult idempotency = mvc.perform(get("/api/v1/test/idempotency-conflict")).andReturn();
        assertEquals(409, quote.getResponse().getStatus());
        assertEquals("QUOTE_CONFLICT", json.readTree(quote.getResponse().getContentAsByteArray()).path("code").asText());
        assertEquals(409, idempotency.getResponse().getStatus());
        assertEquals("IDEMPOTENCY_CONFLICT", json.readTree(idempotency.getResponse().getContentAsByteArray()).path("code").asText());
    }
}
