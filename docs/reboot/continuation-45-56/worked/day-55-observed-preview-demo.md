# Day 55 — Observe a real browser read

The supplied parcel browser helper demonstrates a bounded observation sequence: register an exact path+GET response wait before clicking; observe actual status and JSON; wait for the semantic panel values to render; reject mutation controls/requests; record body and visible text. The helper is in the instructor UI reference source and the real renewal browser runner follows the same order. A mocked fetch unit test is not transport evidence.

Transfer: use the existing Check preview button, `/api/policies/{id}/renewal-preview`, fixed principal test filter, Spring server, JDBC store and disposable PostgreSQL. Assert the exact public response and matching product, premium, date, version and notice in the rendered panel. Observe no POST/PUT/PATCH/DELETE. The test filter injects a synthetic owner; it does not prove authentication/session/CSRF. This is a functional rehearsal, not customer UAT.

Study25: 6:00 focused plain-fetch and Playwright response sources,15:00 complete supplied request/response/panel trace,4:00 prediction. TanStack Query lectures are removed; that package is absent and this harness uses plain fetch. Practice85: 10 build/prerequisite gate,45 browser integration,20 three observed criteria,10 explanation. Evidence10: save actual response/UI observation and first blocker.

Official: [Fetch response handling](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch#handling_the_response), [Playwright waitForResponse](https://playwright.dev/docs/api/class-page#page-wait-for-response), [Response status/json](https://playwright.dev/docs/api/class-response). Command: `npm --prefix frontend run build && CHROMIUM_BIN=/usr/bin/chromium ./mvnw -Plearner-tests -Dtest=RenewalPreviewTransportInfrastructureTest#day55RealBrowserLoadsOwnerScopedPreviewFromSpringAndPostgres test`.

### Complete parcel browser helper (trace only)

The [complete Playwright helper](../../../../frontend/e2e/teaching/observe-parcel-read-reference.mjs) registers the exact GET wait before clicking, records a non-200 as a failure, compares the public JSON shape, waits for the matching panel values, and checks that only GET occurred. It is a complete source trace, not an executed browser example: no parcel page/server is supplied by this project. The renewal acceptance uses its own real Spring/PostgreSQL harness.
