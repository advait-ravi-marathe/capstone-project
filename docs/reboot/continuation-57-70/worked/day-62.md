# Day 62 — Use the authenticated owner

**Study: 25 minutes.** Retrieve Spring Security authentication/authorization foundations, inspect `Authentication`, then study the supplied membership recording-store trace.

## Worked example

`RenewalTeachingReferenceTest#principalWinsAndAnonymousDoesNotRead` sends no identity with a forged owner header and observes 401 with zero lookup calls. A second request supplies server-side `reader-a` plus query/header `reader-b`; the recording lookup receives `reader-a`, returns empty, and the route responds 404. This standalone MVC example tests argument propagation only; it does not install authentication or implement database ownership.

## Transfer

Implement both native resolver overloads. Require owner-a and owner-b to map to themselves; reject null, unauthenticated, anonymous, unknown and blank identities. The untrusted requested owner must not override either identity. Keep HTTP/database ownership claims conditional on the prior real read gate.

**Practice 85:** 10 cases, 50 resolver, 25 checks. **Selector:** `./mvnw -Plocal-app,learner-tests -Dtest=LocalSecurityAcceptanceTest#day62PrincipalOwnsReadsAndForgedOwnerIsIgnored test`.
