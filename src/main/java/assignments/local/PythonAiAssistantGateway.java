package assignments.local;

import java.net.URI;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Calls only the explicitly loopback-bound synthetic AI service. */
@Component
public final class PythonAiAssistantGateway implements LocalAiAssistantGateway {
    private final RestClient client;

    public PythonAiAssistantGateway(
            @org.springframework.beans.factory.annotation.Value("${coverstory.ai.service-url:http://127.0.0.1:8765}") String serviceUrl) {
        URI uri = URI.create(serviceUrl);
        boolean loopback = uri.getHost() != null && (uri.getHost().equals("127.0.0.1")
                || uri.getHost().equals("localhost") || uri.getHost().equals("::1"));
        if (!loopback || !"http".equals(uri.getScheme()) || uri.getUserInfo() != null
                || uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("AI service must be an HTTP loopback origin without credentials or query");
        }
        this.client = RestClient.builder().baseUrl(uri.toString()).build();
    }

    @Override
    public LocalAiAssistantResponse answer(String question, String authenticatedTenantId) {
        try {
            LocalAiAssistantResponse result = client.post()
                    .uri("/answer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("question", question, "tenant_id", authenticatedTenantId))
                    .retrieve()
                    .body(LocalAiAssistantResponse.class);
            if (result == null) throw new AiAssistantUnavailableException();
            return result;
        } catch (RestClientException ex) {
            // Provider/Python error bodies may contain user content; discard them.
            throw new AiAssistantUnavailableException();
        }
    }
}
