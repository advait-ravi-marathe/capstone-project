package assignments.local;

/** Thin instructor-owned Spring-to-local-Python adapter boundary. */
public interface LocalAiAssistantGateway {
    LocalAiAssistantResponse answer(String question, String authenticatedTenantId);
}
