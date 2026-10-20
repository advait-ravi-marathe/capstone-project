package assignments.insurance;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fixed HTTP contract; expected to fail until the domain policy TODO is implemented. */
@Tag("learnerAcceptance")
class QuoteSelectionApiAcceptanceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new QuoteSelectionController(new QuoteSelectionPolicy())).build();

    @Test
    void validSelectionReturnsExactOrderedArrayAndRemovesDuplicates() throws Exception {
        MvcResult result = mvc.perform(post("/api/quote-selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selections":[
                                  {"journeyId":"journey-B","quoteVersionId":"home-secure-v1"},
                                  {"journeyId":"journey-A","quoteVersionId":"home-secure-v1"},
                                  {"journeyId":"journey-B","quoteVersionId":"home-secure-v1"}
                                ]}
                                """))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(objectMapper.readTree("""
                        [
                          {"journeyId":"journey-B","quoteVersionId":"home-secure-v1"},
                          {"journeyId":"journey-A","quoteVersionId":"home-secure-v1"}
                        ]
                        """), objectMapper.readTree(result.getResponse().getContentAsString()));
    }

    @Test
    void emptySelectionsReturnExactEmptyArray() throws Exception {
        MvcResult result = mvc.perform(post("/api/quote-selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selections\":[]}"))
                .andReturn();
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(objectMapper.readTree("[]"), objectMapper.readTree(result.getResponse().getContentAsString()));
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

    @Test
    void nullCompositeKeyPartsReturnBadRequest() throws Exception {
        mvc.perform(post("/api/quote-selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selections":[{"journeyId":null,"quoteVersionId":"home-secure-v1"}]}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/quote-selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"selections":[{"journeyId":"journey-A","quoteVersionId":null}]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingOrNullSelectionsReturnBadRequest() throws Exception {
        mvc.perform(post("/api/quote-selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/quote-selections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selections\":null}"))
                .andExpect(status().isBadRequest());
    }
}
