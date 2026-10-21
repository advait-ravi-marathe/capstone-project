# Day 21 — Cookie and CSRF boundaries

## Token, session, identity, and ownership trace

Use inside the existing 25-minute study block: required owned-course “Step 08 - Exploring Spring Security - Cross Site Request Forgery - CSRF” (11:08), then 3:00 focused official reading, 6:00 worked trace, 3:00 prediction, and 1:52 pause buffer. The REST-CSRF and CORS lessons are optional later references, not cumulative requirements.

An authenticated session cookie and a CSRF token are different values. For the synthetic browser write, the browser sends its session cookie and the client copies the current server-issued `XSRF-TOKEN` value into `X-XSRF-TOKEN`. Missing or wrong token is rejected before the write handler. A matching token only passes the CSRF filter; authentication and object authorization still apply afterward. CORS does not replace CSRF protection.

Run the supporting test actually present in this repository:

```sh
./mvnw -Dtest=Day12SecurityHarnessTest#realSpringSecurityEnforcesCsrfOnWrites test
```

It verifies missing and invalid tokens return 403 and a valid token reaches a synthetic test handler. The harness is form-login/session based. The broader `Day21ContractTest` selector documented in the contract has no corresponding source file in this revision; the full Day 21 acceptance remains blocked until that harness exists. This supporting selector is not Day 21 completion and does not prove production authentication or ownership.

Predict separately: (1) valid session, missing token → 403, no handler call; (2) valid session, wrong token → 403, no handler call; (3) valid session and current token → CSRF passes, later authorization/business checks still decide. A valid token with the wrong owner must still be denied. Do not disable CSRF or invent a constant token.

**Readiness prompt:** Which boundary checks the token, which establishes identity, and which checks resource ownership? DSA is unchanged; engineering remains 25/85/10.
