package coverstory.modules.shortlist.refresh;

import java.util.UUID;

/** CAP-640 server-side current-authorization generation guard. */
public final class RefreshAccessGuard {
    public interface AuthorizationState { long currentGeneration(UUID ownerId, UUID shortlistId); }
    public record Permit(long observedGeneration, long serializedGeneration, boolean allowed) { }
    private final AuthorizationState state;
    public RefreshAccessGuard(AuthorizationState state) { this.state=state; }
    public Permit authorizeAtDecision(UUID owner, UUID shortlist, long previewGeneration) {
        // TODO(CAP-640): perform the authoritative current-generation check at
        // the actual serialized mutation decision, not only during preview.
        throw new UnsupportedOperationException("TODO(CAP-640): guard refresh acceptance against revocation");
    }
}
