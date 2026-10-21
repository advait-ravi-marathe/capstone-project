# Day 62 — Use the authenticated owner

**Study: 25 minutes.** Retrieve Spring Security authentication/authorization foundations, inspect `Authentication`, then study the supplied membership recording-store trace.

## Worked example

The independent [`MembershipAuthenticationReferenceTest`](../../../../src/test/java/assignments/teaching/MembershipAuthenticationReferenceTest.java) demonstrates the identity-state boundary before the native TODO. Its resolver accepts only authenticated `reader-a` and `reader-b`; null, unauthenticated, anonymous-token, blank, and unknown identities are rejected by both overloads. The requested-owner argument never grants identity, and the input authentication object is not changed. The older `RenewalTeachingReferenceTest#principalWinsAndAnonymousDoesNotRead` adds a standalone MVC argument-propagation example; it does not install authentication or implement database ownership.

## Transfer

Implement both native resolver overloads. Require owner-a and owner-b to map to themselves; reject null, unauthenticated, anonymous, unknown and blank identities. The untrusted requested owner must not override either identity. Keep HTTP/database ownership claims conditional on the prior real read gate.

**Practice 85:** 10 cases, 50 resolver, 25 checks. **Selector:** `./mvnw -Plocal-app,sprint5-local,learner-tests -Dtest=LocalSecurityAcceptanceTest#day62PrincipalOwnsReadsAndForgedOwnerIsIgnored test`.
