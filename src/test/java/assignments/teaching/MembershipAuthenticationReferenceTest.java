package assignments.teaching;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Different-domain example of authenticating state before resolving a library member. */
class MembershipAuthenticationReferenceTest {
    private static final Set<String> MEMBERS = Set.of("reader-a", "reader-b");

    static Optional<String> memberId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) return Optional.empty();
        String name = authentication.getName();
        return MEMBERS.contains(name) ? Optional.of(name) : Optional.empty();
    }

    static Optional<String> memberId(Authentication authentication, String ignoredRequestMemberId) {
        return memberId(authentication);
    }

    @Test void onlyKnownAuthenticatedMembersResolveAndRequestValueNeverGrantsIdentity() {
        Authentication readerA = UsernamePasswordAuthenticationToken.authenticated(
                "reader-a", "", AuthorityUtils.NO_AUTHORITIES);
        Authentication readerB = UsernamePasswordAuthenticationToken.authenticated(
                "reader-b", "", AuthorityUtils.NO_AUTHORITIES);
        assertEquals(Optional.of("reader-a"), memberId(readerA));
        assertEquals(Optional.of("reader-b"), memberId(readerB));
        assertEquals(Optional.of("reader-a"), memberId(readerA, "reader-b"));
        assertEquals(Optional.of("reader-b"), memberId(readerB, "reader-a"));

        Authentication anonymous = new AnonymousAuthenticationToken("reference", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        Authentication unauthenticated = UsernamePasswordAuthenticationToken.unauthenticated("reader-a", "");
        Authentication unknown = UsernamePasswordAuthenticationToken.authenticated("reader-c", "", AuthorityUtils.NO_AUTHORITIES);
        Authentication blank = UsernamePasswordAuthenticationToken.authenticated(" ", "", AuthorityUtils.NO_AUTHORITIES);
        for (Authentication rejected : new Authentication[]{null, anonymous, unauthenticated, unknown, blank}) {
            assertTrue(memberId(rejected).isEmpty());
            assertTrue(memberId(rejected, "reader-a").isEmpty());
            assertTrue(memberId(rejected, "reader-b").isEmpty());
        }
        assertEquals("reader-a", readerA.getName(), "resolution does not mutate authentication");
    }
}
