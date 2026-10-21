# Day 65 — Observe CSRF rejection without weakening protection

**Study: 25 minutes.** Spring Security CSRF → “Understanding CSRF Protection's Components” and “Using the CookieCsrfTokenRepository”; compare the gallery filter reference with the native cookie/session trace.

## Worked cases

The gallery reference proves two distinct gates: a valid token cannot authenticate an anonymous request; an authenticated request without or with a wrong token is rejected before the handler; a valid authenticated request reaches the handler exactly once. This mock proves filter behavior, not native policy persistence.

Native instructor test creates a real synthetic session, submits the same issue request with absent then wrong token, expects 403 both times, and compares complete ordered policy/event/idempotency rows after each. A valid-token request is the positive control; if it reaches an earlier learner TODO, report that exact blocker rather than substituting a 201.

**Practice 85:** 10 hypothesis matrix, 45 requests, 20 snapshots, 10 explanation. **Selector:** `./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalSecurityAcceptanceTest#day65InvalidCsrfCannotCreatePolicyRows test`.
