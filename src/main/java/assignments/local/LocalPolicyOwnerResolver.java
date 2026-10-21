package assignments.local;

import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.Set;

/** Maps only the two permitted synthetic principals to owner identifiers. */
public final class LocalPolicyOwnerResolver {
    private static final Set<String> SYNTHETIC_OWNERS = Set.of("owner-a", "owner-b");

    public Optional<String> ownerId(Authentication authentication) {
        // LEARNER TODO Day 62: trust only the authenticated principal; ignore caller-supplied owner fields.
        throw new UnsupportedOperationException("Day 62 learner TODO: resolve authenticated owner");
    }

    public Optional<String> ownerId(Authentication authentication, String untrustedRequestedOwnerId) {
        // LEARNER TODO Day 62: caller-supplied owner IDs must never override the principal.
        throw new UnsupportedOperationException("Day 62 learner TODO: ignore forged owner identifier");
    }

    static boolean isKnownSyntheticOwner(String ownerId) { return SYNTHETIC_OWNERS.contains(ownerId); }
}
