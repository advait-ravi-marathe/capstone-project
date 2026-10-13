package assignments.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Confirms Spring request binding and MockMvc are available before learner validation. */
class EventsControllerInfrastructureTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new EventsController()).build();
    }

    @Test
    void validJsonBindsAndEndpointIsAvailable() throws Exception {
        MvcResult result = mvc.perform(post("/events").contentType("application/json")
                        .content("{\"ownerId\":\"owner\",\"eventId\":\"id\",\"payload\":\"payload\"}"))
                .andReturn();
        int responseStatus = result.getResponse().getStatus();
        assertTrue(responseStatus == 501 || (responseStatus >= 200 && responseStatus < 300),
                "valid JSON must bind; accept the starter TODO response or completed success response");
    }
}
