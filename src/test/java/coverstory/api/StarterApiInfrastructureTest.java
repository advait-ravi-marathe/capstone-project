package coverstory.api;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Confirms the local API shell fails closed until auth and learner catalog logic exist. */
class StarterApiInfrastructureTest {
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new StarterApiController()).build();

    @Test
    void sessionEndpointDoesNotInventAnAuthenticatedOwner() throws Exception {
        mvc.perform(get("/api/session")).andExpect(status().isUnauthorized());
    }

    @Test
    void catalogEndpointIsExplicitlyUnimplemented() throws Exception {
        mvc.perform(get("/api/catalog")).andExpect(status().isNotImplemented());
    }
}
