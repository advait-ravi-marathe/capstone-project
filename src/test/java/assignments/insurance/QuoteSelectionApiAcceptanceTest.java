package assignments.insurance;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fixed HTTP contract; expected to fail until the domain policy TODO is implemented. */
@Tag("learnerAcceptance")
class QuoteSelectionApiAcceptanceTest {
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new QuoteSelectionController(new QuoteSelectionPolicy())).build();

    @Test
    void validSelectionReturnsExactCompositeKey() throws Exception {
        mvc.perform(post("/api/quote-selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selections":[{"journeyId":"journey-B","quoteVersionId":"home-v2"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].journeyId").value("journey-B"))
                .andExpect(jsonPath("$[0].quoteVersionId").value("home-v2"));
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
