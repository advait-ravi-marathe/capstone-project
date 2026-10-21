package assignments.reboot;

import assignments.reboot.Day21SecurityHarness.Counters;
import assignments.insurance.InsuranceDayOneApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Real loopback HTTP exercise of server-issued XSRF-TOKEN cookie and matching header. */
@SpringBootTest(classes = {InsuranceDayOneApplication.class, Day21SecurityHarness.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Day21SecurityInfrastructureTest {
    @LocalServerPort int port;
    @Autowired Counters counters;

    @BeforeEach void resetCounters() { counters.reset(); }

    @Test
    void serverIssuedCookieMissingAndInvalidHeadersAreRejectedBeforeWrite() throws Exception {
        Day21HttpTestSession anonymous = Day21HttpTestSession.anonymous(port);
        assertEquals(302, anonymous.csrfWrite(anonymous.csrfToken()).statusCode(),
                "a valid CSRF token does not authenticate an anonymous session");
        assertEquals(0, counters.writes.get(), "anonymous session must not reach the handler");

        Day21HttpTestSession session = Day21HttpTestSession.login(port, "owner-a");
        assertFalse(session.csrfToken().isBlank());

        assertEquals(403, session.csrfWrite(null).statusCode());
        assertEquals(0, counters.writes.get(), "missing token must stop before handler");

        assertEquals(403, session.csrfWrite("wrong-token").statusCode());
        assertEquals(0, counters.writes.get(), "invalid token must stop before handler");

        assertEquals(204, session.csrfWrite(session.csrfToken()).statusCode());
        assertEquals(1, counters.writes.get(), "matching server-issued cookie/header reaches handler once");
    }
}
