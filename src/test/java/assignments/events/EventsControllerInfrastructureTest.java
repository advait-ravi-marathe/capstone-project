package assignments.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/** Confirms Spring request binding and MockMvc are available before learner validation. */
class EventsControllerInfrastructureTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new EventsController()).build();
    }

    @Test
    void validJsonReachesLearnerValidationSeam() throws Exception {
        mvc.perform(post("/events").contentType("application/json")
                        .content("{\"ownerId\":\"owner\",\"eventId\":\"id\",\"payload\":\"payload\"}"))
                .andExpect(status().isNotImplemented());
    }
}
