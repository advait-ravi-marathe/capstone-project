package assignments.events;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/** Learner acceptance tests; incomplete validation is expected to fail. */
@Tag("learnerAcceptance")
class EventsControllerAcceptanceTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new EventsController()).build();
    }

    @Test
    void missingBodyReturnsBadRequest() throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingRequiredFieldReturnsBadRequest() throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"owner\",\"eventId\":\"id\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingOwnerIdReturnsBadRequest() throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventId\":\"id\",\"payload\":\"payload\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingEventIdReturnsBadRequest() throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"owner\",\"payload\":\"payload\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nullOwnerIdReturnsBadRequest() throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":null,\"eventId\":\"id\",\"payload\":\"payload\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nullEventIdReturnsBadRequest() throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"owner\",\"eventId\":null,\"payload\":\"payload\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nullPayloadReturnsBadRequest() throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"owner\",\"eventId\":\"id\",\"payload\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankOwnerIdReturnsBadRequest() throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\" \",\"eventId\":\"id\",\"payload\":\"payload\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankEventIdReturnsBadRequest() throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"owner\",\"eventId\":\" \",\"payload\":\"payload\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankRequiredFieldReturnsBadRequest() throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"owner\",\"eventId\":\"id\",\"payload\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validRequestReturnsSuccess() throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":\"owner\",\"eventId\":\"id\",\"payload\":\"payload\"}"))
                .andExpect(status().is2xxSuccessful());
    }
}
