package coverstory.modules.shortlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import coverstory.modules.shortlist.api.CAP40Seam;
import coverstory.modules.shortlist.api.SelectionInput;
import coverstory.modules.shortlist.api.SelectionKey;
import coverstory.modules.shortlist.api.SelectionOrderer;
import coverstory.modules.shortlist.api.ShortlistSelectionController;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HTTP boundary tests use an instructor-only witness; production CAP-40 remains a TODO. */
class ShortlistSelectionControllerInfrastructureTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void compareRouteForwardsPublicReferencesThroughTheShortlistPortAndPreservesFirstSeenOrder() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new ShortlistSelectionController(instructorWitness())).build();
        String request = """
                {"selections":[
                  {"deviceScope":"tab-A","catalogVersionId":"health-Z@v1"},
                  {"deviceScope":"tab-A","catalogVersionId":"health-A@v1"},
                  {"deviceScope":"tab-A","catalogVersionId":"health-Z@v1"}
                ]}
                """;
        var result = mvc.perform(post("/api/shortlist/selection-order")
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk()).andReturn();
        JsonNode payload = mapper.readTree(result.getResponse().getContentAsString());
        assertEquals(1, payload.path("schemaMajor").asInt());
        assertEquals(List.of(
                new SelectionKey("tab-A", "health-Z@v1"),
                new SelectionKey("tab-A", "health-A@v1")),
                mapper.convertValue(payload.path("selections"),
                        mapper.getTypeFactory().constructCollectionType(List.class, SelectionKey.class)));

        var scopeResult = mvc.perform(post("/api/shortlist/selection-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selections\":["
                                + "{\"deviceScope\":\"tab-A\",\"catalogVersionId\":\"health-Z@v1\"},"
                                + "{\"deviceScope\":\"tab-B\",\"catalogVersionId\":\"health-Z@v1\"}]}"))
                .andExpect(status().isOk()).andReturn();
        JsonNode scopePayload = mapper.readTree(scopeResult.getResponse().getContentAsString());
        assertEquals(List.of(new SelectionKey("tab-A", "health-Z@v1"),
                        new SelectionKey("tab-B", "health-Z@v1")),
                mapper.convertValue(scopePayload.path("selections"),
                        mapper.getTypeFactory().constructCollectionType(List.class, SelectionKey.class)));
    }

    @Test
    void actualLearnerSeamReportsNotImplementedUntilTheLearnerCompletesIt() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new ShortlistSelectionController(new CAP40Seam())).build();
        mvc.perform(post("/api/shortlist/selection-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selections\":[{\"deviceScope\":\"tab-A\",\"catalogVersionId\":\"health-A@v1\"}]}"))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void malformedBodyIsRejectedBeforeTheLearnerSeam() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new ShortlistSelectionController(instructorWitness())).build();
        mvc.perform(post("/api/shortlist/selection-order")
                        .contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicCompareRequestHasAThreeReferenceBoundary() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new ShortlistSelectionController(input -> { throw new AssertionError("oversized request reached seam"); }))
                .build();
        mvc.perform(post("/api/shortlist/selection-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selections\":["
                                + "{\"deviceScope\":\"tab-A\",\"catalogVersionId\":\"a@v1\"},"
                                + "{\"deviceScope\":\"tab-A\",\"catalogVersionId\":\"b@v1\"},"
                                + "{\"deviceScope\":\"tab-A\",\"catalogVersionId\":\"c@v1\"},"
                                + "{\"deviceScope\":\"tab-A\",\"catalogVersionId\":\"d@v1\"}]}"))
                .andExpect(status().isBadRequest());
    }

    private static SelectionOrderer instructorWitness() {
        return input -> {
            Set<SelectionKey> seen = new LinkedHashSet<>();
            List<SelectionKey> ordered = new ArrayList<>();
            for (SelectionInput row : input) {
                SelectionKey key = new SelectionKey(row.deviceScope(), row.catalogVersionId());
                if (seen.add(key)) ordered.add(key);
            }
            return List.copyOf(ordered);
        };
    }
}
