package assignments.reboot;

import assignments.insurance.ApiError;
import assignments.insurance.QuoteSelectionKey;
import assignments.insurance.QuoteSelectionRequest;
import assignments.insurance.QuoteSelectionRequestValidation;
import assignments.insurance.QuoteSelectionService;
import assignments.insurance.QuoteSelectionController;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Controller wiring check with test-only validator and service; no business solution is supplied. */
class Day02ApiAcceptanceTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void validRequestCallsSelectionServiceExactlyOnce() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        QuoteSelectionService service = selections -> {
            calls.incrementAndGet();
            return List.of(new QuoteSelectionKey("journey-A", "version-1"));
        };
        MockMvc mvc = mvc(service, this::fixtureValidation);

        var response = mvc.perform(post("/api/quote-selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selections\":[{\"journeyId\":\"journey-A\",\"quoteVersionId\":\"version-1\"}]}"))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(1, calls.get());
        assertEquals(mapper.readTree("[{\"journeyId\":\"journey-A\",\"quoteVersionId\":\"version-1\"}]"),
                mapper.readTree(response.getResponse().getContentAsString()));
    }

    @Test
    void invalidRequestReturnsStable400BodyAndDoesNotCallService() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        QuoteSelectionService service = selections -> {
            calls.incrementAndGet();
            return List.of();
        };
        MockMvc mvc = mvc(service, request -> {
            throw new IllegalArgumentException("canned rejection");
        });

        var response = mvc.perform(post("/api/quote-selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selections\":[{\"journeyId\":\"journey-A\",\"quoteVersionId\":\" \"}]}"))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(0, calls.get());
        assertEquals(new ApiError("INVALID_REQUEST", "Request validation failed"),
                mapper.readValue(response.getResponse().getContentAsString(), ApiError.class));
    }

    @Test
    void malformedJsonReturns400WithoutCallingService() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        MockMvc mvc = mvc(selections -> {
            calls.incrementAndGet();
            return List.of();
        }, this::fixtureValidation);

        mvc.perform(post("/api/quote-selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{broken"))
                .andExpect(status().isBadRequest());
        assertEquals(0, calls.get());
    }

    private MockMvc mvc(QuoteSelectionService service, QuoteSelectionRequestValidation validation) {
        return MockMvcBuilders.standaloneSetup(new QuoteSelectionController(service, validation)).build();
    }

    private void fixtureValidation(QuoteSelectionRequest request) {
        if (request == null || request.selections() == null) {
            throw new IllegalArgumentException("test fixture rejected request");
        }
    }
}
