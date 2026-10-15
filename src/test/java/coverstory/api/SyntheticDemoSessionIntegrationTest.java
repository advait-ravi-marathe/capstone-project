package coverstory.api;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** Demonstrates login-return owner switching with fixed, unsigned local fixtures only. */
class SyntheticDemoSessionIntegrationTest {
    private final MockMvc mvc=MockMvcBuilders.standaloneSetup(new StarterApiController(),new SyntheticDemoSessionController())
            .addFilters(new SyntheticDemoSessionFilter()).build();

    @Test void fixedDemoSelectorSetsHttpOnlyCookieAndSessionEndpointReturnsTrustedPrincipal() throws Exception {
        MvcResult selected=mvc.perform(post("/api/demo/session").contentType("application/json").content("{\"account\":\"A\"}")).andReturn();
        assertEquals(200,selected.getResponse().getStatus());
        String cookie=selected.getResponse().getHeader("Set-Cookie");
        assertTrue(cookie.contains("HttpOnly")); assertTrue(cookie.contains("SameSite=Strict"));
        assertTrue(cookie.contains("Path=/"));
        String pair=cookie.split(";",2)[0];
        assertTrue(mvc.perform(get("/api/session").cookie(new jakarta.servlet.http.Cookie("coverstory_demo_owner",pair.split("=",2)[1]))).andReturn().getResponse().getContentAsString()
                .contains(SyntheticDemoSessionFilter.OWNER_A.toString()));
    }

    @Test void switchingAndLogoutReplaceTheOwnerAndUnknownCookiesNeverAuthenticate() throws Exception {
        MvcResult a=mvc.perform(post("/api/demo/session").contentType("application/json").content("{\"account\":\"A\"}")).andReturn();
        MvcResult b=mvc.perform(post("/api/demo/session").contentType("application/json").content("{\"account\":\"B\"}")).andReturn();
        String bPair=b.getResponse().getHeader("Set-Cookie").split(";",2)[0];
        var session=mvc.perform(get("/api/session").cookie(new jakarta.servlet.http.Cookie("coverstory_demo_owner",bPair.split("=",2)[1]))).andReturn();
        assertTrue(session.getResponse().getContentAsString().contains(SyntheticDemoSessionFilter.OWNER_B.toString()));
        MvcResult logout=mvc.perform(post("/api/demo/logout")).andReturn();
        assertTrue(logout.getResponse().getHeader("Set-Cookie").contains("Max-Age=0"));
        assertEquals(401,mvc.perform(get("/api/session").header("Cookie","coverstory_demo_owner=invalid")).andReturn().getResponse().getStatus());
        assertFalse(a.getResponse().getHeader("Set-Cookie").contains("Secure"));
    }

    @Test void unsupportedDemoAccountDoesNotSetAnIdentityCookie() throws Exception {
        MvcResult result=mvc.perform(post("/api/demo/session").contentType("application/json").content("{\"account\":\"admin\"}")).andReturn();
        assertEquals(400,result.getResponse().getStatus()); assertNull(result.getResponse().getHeader("Set-Cookie"));
    }

    @Test void demoCookieNeverReplacesAnAlreadyAuthenticatedPrincipal() throws Exception {
        var result=mvc.perform(get("/api/session")
                .principal(() -> "trusted-authenticated-owner")
                .cookie(new jakarta.servlet.http.Cookie("coverstory_demo_owner","A"))).andReturn();
        assertEquals(200,result.getResponse().getStatus());
        assertTrue(result.getResponse().getContentAsString().contains("trusted-authenticated-owner"));
        assertFalse(result.getResponse().getContentAsString().contains(SyntheticDemoSessionFilter.OWNER_A.toString()));
    }
}
