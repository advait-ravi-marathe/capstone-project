package assignments.local;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class LocalAiAssistantControllerTest {
    @Test
    void derivesTenantFromAuthenticatedSessionAndNeverFromRequest() {
        var gateway = new RecordingGateway();
        var controller = new LocalAiAssistantController(gateway);
        var auth = UsernamePasswordAuthenticationToken.authenticated("owner-a", "",
                List.of(new SimpleGrantedAuthority("ROLE_TRAINING_USER")));
        ResponseEntity<?> response = controller.ask(new LocalAiAssistantController.QuestionRequest("Is hail covered?"), auth);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("owner-a", gateway.tenant);
        assertEquals("Is hail covered?", gateway.question);
        LocalAiAssistantResponse body = (LocalAiAssistantResponse) response.getBody();
        assertEquals("offline_synthetic_fixture", body.executionMode());
        assertFalse(body.providerRequestMade());
        assertFalse(body.liveQualityEvaluated());
    }

    @Test
    void unauthorizedMalformedAndUnavailableOutcomesAreCompleteAndRedacted() {
        var auth = UsernamePasswordAuthenticationToken.authenticated("owner-a", "",
                List.of(new SimpleGrantedAuthority("ROLE_TRAINING_USER")));
        var gateway = new RecordingGateway();
        var controller = new LocalAiAssistantController(gateway);
        ResponseEntity<?> unauthenticated = controller.ask(new LocalAiAssistantController.QuestionRequest("x"), null);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthenticated.getStatusCode());
        assertEquals(Map.of("code", "UNAUTHENTICATED"), unauthenticated.getBody());
        ResponseEntity<?> malformed = controller.ask(new LocalAiAssistantController.QuestionRequest(" "), auth);
        assertEquals(HttpStatus.BAD_REQUEST, malformed.getStatusCode());
        assertEquals(Map.of("code", "INVALID_QUESTION"), malformed.getBody());
        gateway.fail = true;
        ResponseEntity<?> unavailable = controller.ask(new LocalAiAssistantController.QuestionRequest("question text"), auth);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, unavailable.getStatusCode());
        assertEquals(Map.of("code", "AI_PROVIDER_UNAVAILABLE"), unavailable.getBody());
        assertFalse(String.valueOf(unavailable.getBody()).contains("question text"));
        assertNull(gateway.lastFailureBody);
    }

    private static final class RecordingGateway implements LocalAiAssistantGateway {
        private String tenant;
        private String question;
        private boolean fail;
        private String lastFailureBody;
        @Override public LocalAiAssistantResponse answer(String q, String authenticatedTenantId) {
            question = q;
            tenant = authenticatedTenantId;
            if (fail) throw new AiAssistantUnavailableException();
            return new LocalAiAssistantResponse("Hail damage is excluded.",
                    List.of(new LocalAiAssistantResponse.Citation("SYN-POL-A", "v1", "Exclusions")),
                    false, "offline_synthetic_fixture", false, false);
        }
    }
}
