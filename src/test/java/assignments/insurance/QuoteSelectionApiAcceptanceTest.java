package assignments.insurance;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fixed HTTP contract; expected to fail until the domain policy TODO is implemented. */
@Tag("learnerAcceptance")
class QuoteSelectionApiAcceptanceTest {
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new QuoteSelectionController(new QuoteSelectionPolicy())).build();

    @Test
    void validSelectionReturnsExactCompositeKey() throws Exception {
        MvcResult result = mvc.perform(post("/api/quote-selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selections":[{"journeyId":"journey-B","quoteVersionId":"home-secure-v1"}]}
                                """))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsString().contains("\"journeyId\":\"journey-B\""));
        assertTrue(result.getResponse().getContentAsString().contains("\"quoteVersionId\":\"home-secure-v1\""));
    }

    @Test
    void blankSelectionReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/quote-selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selections":[{"journeyId":"journey-A","quoteVersionId":" "}]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mvc.perform(post("/api/quote-selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{broken"))
                .andExpect(status().isBadRequest());
    }
}
