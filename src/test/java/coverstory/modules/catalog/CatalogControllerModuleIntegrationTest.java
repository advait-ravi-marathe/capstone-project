package coverstory.modules.catalog;

import coverstory.modules.catalog.api.CatalogController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises the real public port -> synthetic adapter -> HTTP projection path. */
class CatalogControllerModuleIntegrationTest {
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new CatalogController(new SyntheticCatalogAdapter())).build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void publicHealthSearchUsesCatalogPortAndReturnsOnlySyntheticSafeFields() throws Exception {
        var result = mvc.perform(get("/api/catalog").param("category", "health"))
                .andExpect(status().isOk()).andReturn();
        JsonNode response = mapper.readTree(result.getResponse().getContentAsString());
        JsonNode offers = response.path("offers");
        assertEquals(1, response.path("schemaMajor").asInt());
        assertEquals(3, offers.size());
        assertEquals("10000000-0000-4000-8000-000000000001", offers.get(0).path("offerId").asText());
        assertEquals("SYNTHETIC", offers.get(0).path("evidenceStatus").asText());
        assertEquals("UNKNOWN", offers.get(0).path("waitingPeriodStatus").asText());
        assertFalse(offers.get(0).has("price"));
        assertFalse(offers.get(0).has("terms"));
        assertFalse(offers.get(0).has("consumer"));
    }

    @Test
    void queryIsBoundedAndOtherCategoriesDoNotInventOffers() throws Exception {
        var empty = mvc.perform(get("/api/catalog").param("category", "motor"))
                .andExpect(status().isOk()).andReturn();
        assertEquals(0, mapper.readTree(empty.getResponse().getContentAsString()).path("offers").size());
        mvc.perform(get("/api/catalog").param("category", "unknown"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/catalog").param("query", "x".repeat(101)))
                .andExpect(status().isBadRequest());
    }
}
