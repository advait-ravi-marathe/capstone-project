package assignments.reboot;

import assignments.insurance.InsuranceDayOneApplication;
import assignments.reboot.Day21SecurityHarness.Counters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Learner acceptance: CSRF must pass before the actual owner policy decides access. */
@Tag("learnerAcceptance")
@SpringBootTest(classes = {InsuranceDayOneApplication.class, Day21SecurityHarness.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Day21ContractTest {
    @LocalServerPort int port;
    @Autowired Counters counters;

    @BeforeEach void resetCounters() { counters.reset(); }

    @Test
    void authenticatedOwnerIsTakenFromSessionRatherThanRequestField() throws Exception {
        Day21HttpTestSession owner = Day21HttpTestSession.login(port, "owner-a");
        int before = counters.ownerChecks.get();
        int status = owner.ownerRead("journey-a", "attacker").statusCode();
        assertEquals(before + 1, counters.ownerChecks.get(), "valid CSRF must reach the owner policy");
        assertEquals(200, status, "authenticated owner's request should pass CSRF and reach the real policy");
    }

    @Test
    void foreignAuthenticatedOwnerIsConcealedByTheRealOwnerPolicy() throws Exception {
        Day21HttpTestSession foreign = Day21HttpTestSession.login(port, "owner-b");
        int before = counters.ownerChecks.get();
        int status = foreign.ownerRead("journey-a", "owner-a").statusCode();
        assertEquals(before + 1, counters.ownerChecks.get(), "valid CSRF must reach the owner policy");
        assertEquals(404, status, "foreign owner's request should reach the concealed owner-policy outcome");
    }
}
