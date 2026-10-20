package assignments.insurance;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Confirms the supplied HTTP adapter is registered and returns bad request for a missing body. */
class QuoteSelectionInfrastructureTest {
    @Test
    void missingBodyReturnsBadRequestWithoutStartingAWebServer() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new QuoteSelectionController(new QuoteSelectionPolicy()))
                .build();
        mvc.perform(post("/api/quote-selections"))
                .andExpect(status().isBadRequest());
    }
}
