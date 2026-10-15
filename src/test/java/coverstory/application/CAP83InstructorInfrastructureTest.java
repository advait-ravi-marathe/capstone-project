package coverstory.application;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/** Independent reference mapping and working MockMvc setup, separate from learner TODO. */
class CAP83InstructorInfrastructureTest {
    private final ObjectMapper json = new ObjectMapper();
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new CAP83MockController())
            .setControllerAdvice(new CoverStoryExceptionHandler(CAP83InstructorInfrastructureTest::referenceMap))
            .build();

    private static CoverStoryProblemDetail referenceMap(KnownException ex, RequestContext ctx) {
        HttpStatus status;
        String code;
        String message;
        Map<String, String> fields = Map.of();
        if (ex instanceof KnownException.MalformedRequest) {
            status = HttpStatus.BAD_REQUEST; code = "INVALID_REQUEST"; message = "The request is invalid.";
        } else if (ex instanceof KnownException.SessionRequired) {
            status = HttpStatus.UNAUTHORIZED; code = "UNAUTHENTICATED"; message = "Sign in to continue.";
        } else if (ex instanceof KnownException.ResourceNotFound) {
            status = HttpStatus.NOT_FOUND; code = "NOT_FOUND"; message = "The requested resource was not found.";
        } else if (ex instanceof KnownException.QuoteConflict) {
            status = HttpStatus.CONFLICT; code = "QUOTE_CONFLICT"; message = "The quote changed. Review it before continuing.";
        } else if (ex instanceof KnownException.IdempotencyConflict) {
            status = HttpStatus.CONFLICT; code = "IDEMPOTENCY_CONFLICT"; message = "This request key was used with different details.";
        } else if (ex instanceof KnownException.StaleVersion) {
            status = HttpStatus.PRECONDITION_FAILED; code = "STALE_VERSION"; message = "This resource changed. Reload and compare your edits.";
        } else if (ex instanceof KnownException.IncompleteFields incomplete) {
            status = HttpStatus.UNPROCESSABLE_ENTITY; code = "INCOMPLETE_FIELDS"; message = "Complete the highlighted fields.";
            fields = incomplete.fields().entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                    entry -> entry.getKey().wireName(),
                    entry -> entry.getValue() == KnownException.FieldProblem.REQUIRED
                            ? "This field is required." : "Check this field's format."));
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR; code = "INTERNAL_ERROR"; message = "An unexpected error occurred.";
        }
        return new CoverStoryProblemDetail(status, code, message, fields, ctx.effectiveRequestId(), false);
    }

    @Test
    void malformedPayloadReturnsSafe400AndDoesNotEchoBody() throws Exception {
        String malformed = "{\"answer\":\"RAW_ANSWER_SENTINEL\",";
        MvcResult result = mvc.perform(post("/api/v1/test/malformed").header("X-Request-Id", "req1")
                        .contentType(MediaType.APPLICATION_JSON).content(malformed)).andReturn();
        assertEquals(400, result.getResponse().getStatus());
        JsonNode body = json.readTree(result.getResponse().getContentAsByteArray());
        assertEquals("INVALID_REQUEST", body.path("code").asText());
        assertEquals("req1", body.path("requestId").asText());
        assertEquals("The request is invalid.", body.path("message").asText());
        assertEquals(body.path("message").asText(), body.path("detail").asText());
        assertFalse(result.getResponse().getContentAsString().contains("RAW_ANSWER_SENTINEL"));
    }

    @Test
    void foreignAndAbsentResourcesHaveIdenticalNotFoundResponse() throws Exception {
        MvcResult absentResult = mvc.perform(get("/api/v1/test/absent").header("X-Request-Id", "same-request")).andReturn();
        MvcResult foreignResult = mvc.perform(get("/api/v1/test/foreign").header("X-Request-Id", "same-request")).andReturn();
        assertEquals(404, absentResult.getResponse().getStatus());
        assertEquals(404, foreignResult.getResponse().getStatus());
        String absent = absentResult.getResponse().getContentAsString();
        String foreign = foreignResult.getResponse().getContentAsString();
        assertEquals(absent, foreign);
        assertEquals("NOT_FOUND", json.readTree(absent).get("code").asText());
    }

    @Test
    void staleIfMatchUses412AndSafeCompareMessage() throws Exception {
        MvcResult result = mvc.perform(put("/api/v1/test/stale").header("If-Match", "v3").header("X-Request-Id", "stale-1")).andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsByteArray());
        assertEquals(412, result.getResponse().getStatus());
        assertEquals("STALE_VERSION", body.path("code").asText());
        assertEquals("This resource changed. Reload and compare your edits.", body.path("message").asText());
    }

    @Test
    void sqlSentinelIsAbsentFromResponseAndCapturedConsoleLogs() throws Exception {
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream captureStream = new PrintStream(captured, true, StandardCharsets.UTF_8);
        MvcResult result;
        try {
            System.setOut(captureStream);
            System.setErr(captureStream);
            result = mvc.perform(get("/api/v1/test/sql").header("X-Request-Id", "sql-safe-1")).andReturn();
        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
            captureStream.close();
        }
        JsonNode body = json.readTree(result.getResponse().getContentAsByteArray());
        assertEquals(500, result.getResponse().getStatus());
        assertEquals("INTERNAL_ERROR", body.path("code").asText());
        assertEquals("An unexpected error occurred.", body.path("message").asText());
        assertFalse(result.getResponse().getContentAsString().contains("SECRET_SENTINEL"));
        assertFalse(captured.toString(StandardCharsets.UTF_8).contains("SECRET_SENTINEL"));
    }

    @Test
    void missingRequestIdIsGeneratedAndValidationErrorsAreFieldLinked() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/test/incomplete")).andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsByteArray());
        assertEquals(422, result.getResponse().getStatus());
        assertEquals("INCOMPLETE_FIELDS", body.path("code").asText());
        assertEquals("This field is required.", body.path("fieldErrors").path("fullName").asText());
        String requestId = body.path("requestId").asText();
        assertTrue(Pattern.matches("[A-Za-z0-9._:-]{1,80}", requestId));
    }

    @Test
    void legacyCorrelationIdIsEmittedAsCanonicalRequestId() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/test/session").header("X-Correlation-Id", "legacy-req-2")).andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsByteArray());
        assertEquals(401, result.getResponse().getStatus());
        assertEquals("legacy-req-2", body.path("requestId").asText());
        assertFalse(body.has("correlationId"));
    }

    @Test
    void conflictAndStaleCategoriesKeepStableStatusCodes() throws Exception {
        MvcResult quote = mvc.perform(get("/api/v1/test/quote-conflict")).andReturn();
        MvcResult idempotency = mvc.perform(get("/api/v1/test/idempotency-conflict")).andReturn();
        assertEquals(409, quote.getResponse().getStatus());
        assertEquals("QUOTE_CONFLICT", json.readTree(quote.getResponse().getContentAsByteArray()).path("code").asText());
        assertEquals(409, idempotency.getResponse().getStatus());
        assertEquals("IDEMPOTENCY_CONFLICT", json.readTree(idempotency.getResponse().getContentAsByteArray()).path("code").asText());
    }

    @Test
    void problemResponseHasRequiredStableEnvelopeFields() throws Exception {
        MvcResult result = mvc.perform(get("/api/v1/test/session").header("X-Request-Id", "fields-1")).andReturn();
        JsonNode body = json.readTree(result.getResponse().getContentAsByteArray());
        assertEquals(401, result.getResponse().getStatus());
        assertTrue(body.has("type"), body.toString()); assertTrue(body.has("title"), body.toString());
        assertEquals(401, body.path("status").asInt()); assertTrue(body.has("detail"));
        assertEquals("UNAUTHENTICATED", body.path("code").asText());
        assertTrue(body.has("message")); assertTrue(body.path("fieldErrors").isObject());
        assertEquals("fields-1", body.path("requestId").asText());
        assertFalse(body.path("retryable").asBoolean());
        assertFalse(body.has("correlationId"));
    }

    @Test
    void savedFixtureListsAllStableStatusCategories() throws Exception {
        JsonNode fixture = json.readTree(Files.readString(Path.of("fixtures/coverstory/CAP-83/synthetic-v1.json")));
        assertTrue(fixture.path("synthetic").asBoolean());
        assertEquals(5, fixture.path("error_cases").size());
        assertEquals(8, fixture.path("status_map").size());
        assertEquals("INVALID_REQUEST", fixture.path("status_map").get(0).path("code").asText());
        assertEquals(404, fixture.path("status_map").get(2).path("status").asInt());
        assertEquals("STALE_VERSION", fixture.path("status_map").get(5).path("code").asText());
        assertEquals("INTERNAL_ERROR", fixture.path("status_map").get(7).path("code").asText());
    }

    @Test
    void learnerMapperIsStillAnExplicitTodoWhileIndependentMockMvcOracleRuns() {
        ReturnSafeCheckoutAndDraftErrors learner = new ReturnSafeCheckoutAndDraftErrors();
        assertThrows(UnsupportedOperationException.class,
                () -> learner.mapException(new KnownException.MalformedRequest(), new RequestContext("req1", null)));
    }
}
