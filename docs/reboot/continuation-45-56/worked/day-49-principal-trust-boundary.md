# Day 49 — A caller cannot choose their own owner identity

The independent membership endpoint accepts a path member id and a server-provided `Principal`; it has no owner body field. An argument-recording lookup proves the exact boundary: unauthenticated request with a forged owner header returns401 and performs zero reads; principal `reader-a` with query/header `reader-b` calls the store with `(m7, reader-a)` and, for an empty fixture, returns404.

The complete standalone MVC controller/test is `RenewalTeachingReferenceTest#principalWinsAndAnonymousDoesNotRead`. `MockMvc.principal(...)` supplies a test identity only. It does not test login, password/session validation, CSRF, or production middleware.

Transfer: implement only `authenticatedOwner` and strengthen the actual route acceptance using an empty recording store. Assert missing/blank principal returns401 with no lookup; forged query/header cannot change the captured owner; stop at404 so Day50/51 are not invoked.

Study25: 4:17 authentication lesson,3:00 focused Principal/PathVariable entries,13:43 worked request/store trace,4:00 prediction. Practice85: 10 trust-boundary sketch,55 `authenticatedOwner`,20 tests. Evidence10. Official: [Spring MVC method arguments](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/arguments.html).

Commands: `./mvnw -Plearner-tests -Dtest=PolicyRenewalControllerAcceptanceTest#day49OwnerComesOnlyFromPrincipalAndUnauthenticatedDoesNotRead+day49MockMvcPassesPrincipalRatherThanForgedQueryOrHeaderToTheReadStore test`; `./mvnw -Dtest=RenewalTeachingReferenceTest#principalWinsAndAnonymousDoesNotRead test`.
