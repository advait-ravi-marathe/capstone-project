# Day 21 — Cookie and CSRF boundaries

## Token, session, identity, and ownership trace

Use inside the existing 25-minute study block: required owned-course “Step 08 - Exploring Spring Security - Cross Site Request Forgery - CSRF” (11:08), then 3:00 focused official reading, 6:00 worked trace, 3:00 prediction, and 1:52 pause buffer. The REST-CSRF and CORS lessons are optional later references, not cumulative requirements.

An authenticated session cookie and a CSRF token are different values. For the synthetic browser write, the browser sends its session cookie and the client copies the current server-issued `XSRF-TOKEN` value into `X-XSRF-TOKEN`. Missing or wrong token is rejected before the write handler. A matching token only passes the CSRF filter; authentication and object authorization still apply afterward. CORS does not replace CSRF protection.

The focused primary reference is Spring Security's [CSRF protection guide](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html), specifically `CookieCsrfTokenRepository` cookie/header names and the SPA token acquisition section. The test fixture uses a simple raw echo handler to demonstrate the HTTP boundary; it does not recommend replacing production token handling with custom cryptography.

Run the real-HTTP instructor security exercise:

```sh
./mvnw -Dtest=Day21SecurityInfrastructureTest test
```

The test-only form-login harness obtains a real `XSRF-TOKEN` cookie from the server, logs in with the current token in `X-XSRF-TOKEN`, then fetches a fresh token because authentication clears the previous one. Its HTTP client retains the session cookie. Missing and wrong headers return 403 before the write handler; the matching cookie/header pair reaches the handler. This harness does not configure production authentication.

The owner acceptance now exists as `Day21ContractTest` and uses that same HTTP session and token flow before calling the real learner-owned `JourneyAccessPolicy`. Run it separately:

```sh
./mvnw -Plearner-tests -Dtest=Day21ContractTest test
```

It currently remains red at the existing Day 12 owner-policy TODO. The counter assertion verifies valid CSRF reached that policy before the expected TODO failure. This test is not evidence of production authentication or learner completion.

Predict separately: (1) valid session, missing token → 403, no handler call; (2) valid session, wrong token → 403, no handler call; (3) valid session and current token → CSRF passes, later authorization/business checks still decide. A valid token with the wrong owner must still be denied. Do not disable CSRF or invent a constant token.

**Readiness prompt:** Which boundary checks the token, which establishes identity, and which checks resource ownership? Engineering remains within 25/85/10.
