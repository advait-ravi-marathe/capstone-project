# Day 61 — Trace identity, CSRF and session separately

**Study: 25 minutes.** Spring Security “Setting Up MockMvc and Spring Security”, “Testing with CSRF Protection”, “Authentication”, “SecurityContextHolder”, “SecurityContext”, and Spring logout “Understanding Logout's Architecture”. Optional testing-course clips replace this study time; no purchase or repeat video is required.

## Worked reference

`GallerySecurityReferenceTest` uses a real Spring Security filter chain and a counter-only gallery handler. Public GET succeeds; protected anonymous GET is 401; authenticated POST without or with an invalid CSRF token is 403 and leaves the handler counter at zero; authenticated POST with a valid token is 204 and increments once. Run `./mvnw -Dtest=GallerySecurityReferenceTest test`. `with(user(...))` is a test identity, not login or session persistence.

Native trace: GET `/api/session` is handler-level 401 when no saved synthetic session exists. A protected policy GET is filter-level 401. POST `/api/session` is permitted by authorization but still CSRF-protected. After selecting a synthetic owner, a second request using the same session must read that saved identity. Identity selection is not password authentication.

**Transfer:** inspect exact `localSecurityFilterChain(HttpSecurity, SecurityContextRepository)` and test missing-token selection; do not claim standard logout token clearing for this custom handler. **Practice 85:** predict 10, inspect 25, run bounded checks 35, explain 15. Selector: `./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalSecurityAcceptanceTest#day61AnonymousApiDeniedAssetsAllowedAndOnlySyntheticSessionAccepted test`.
