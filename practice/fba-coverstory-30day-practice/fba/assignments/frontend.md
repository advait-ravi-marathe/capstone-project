# Front-End Engineer II — FBA: 30 dated assignments

Amazon job ID: **10575321**. Each day is one 90-minute FBA practice block: 35 minutes implementation, 15 minutes design/review, 10 minutes truthful STAR practice, and 30 minutes focused component plus adversarial testing. The final 30 minutes are task-specific: extend `frontend/src/ui.test.jsx` with the named failure/boundary behavior, add the corresponding synthetic case to `fixtures/cases.json` where it is a pure contract vector, and run both the focused component suite and that day's contract command. Dates and exact fixtures live in [`../SPRINT-MANIFEST.json`](../SPRINT-MANIFEST.json).

Implementation surface: `frontend/src/ui.jsx and frontend/src/assignments.js`. Test command: `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)`. The copied FBA React workspace has 30 per-day learner functions wired into its navigator, with async service ports for the specified tasks. Implement and review only the assignment for the current day.

## Day 01 — 2026-10-17: Seller registration form with errors
**Pattern:** forms / accessible validation. **Leadership Principle:** Customer Obsession. **Implementation:** Day 01 component in `frontend/src/ui.jsx` and learner helper `day01` in `frontend/src/assignments.js`.

**35 min — implementation:** Build a controlled registration form with name and email fields. On submit, show field errors next to controls, focus the first invalid field, and show a success status for valid input. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=1)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the existing invalid-email and blank-name vectors. Add a focused UI test that checks their inline error messages and focus behavior, then correct the invalid field and assert the success status. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_01` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=1)`.

**15 min — design/review:** Sketch an accessible seller registration form, labels, error summary, focus placement, and server validation boundary.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Customer Obsession. Never invent work or metrics.

## Day 02 — 2026-10-18: Responsive inventory filter
**Pattern:** controlled input / list rendering. **Leadership Principle:** Ownership. **Implementation:** Day 02 component in `frontend/src/ui.jsx` and learner helper `day02` in `frontend/src/assignments.js`.

**35 min — implementation:** Add controlled search and min/max quantity filters to the inventory list. Preserve order, show an empty state, and keep the filter usable at a narrow viewport. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=2)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the existing apple/pear fixture. Add a UI test for inclusive min/max filtering with the query, preserve original row order, and assert the empty-results message at a narrow viewport. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_02` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=2)`.

**15 min — design/review:** Explain table filtering on narrow screens and keyboard-accessible filter controls.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Ownership. Never invent work or metrics.

## Day 03 — 2026-10-19: Async onboarding form state
**Pattern:** async submission / state machine. **Leadership Principle:** Learn and Be Curious. **Implementation:** Day 03 component in `frontend/src/ui.jsx` and learner helper `day03` in `frontend/src/assignments.js`.

**35 min — implementation:** Submit the form through an injected Promise-based API. Show pending, success, retryable error, and validation states; prevent duplicate submits while pending. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=3)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the existing SUBMIT/REJECT/RESET and SUBMIT/RESOLVE traces. Hold the injected API promise open, prove a second submit is suppressed, then verify retry after rejection reaches success. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_03` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=3)`.

**15 min — design/review:** Design disabled/loading/error states and announce state changes without trapping keyboard users.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Learn and Be Curious. Never invent work or metrics.

## Day 04 — 2026-10-20: Price-band selector
**Pattern:** binary search / selection control. **Leadership Principle:** Dive Deep. **Implementation:** Day 04 component in `frontend/src/ui.jsx` and learner helper `day04` in `frontend/src/assignments.js`.

**35 min — implementation:** Build a keyboard-operable price-band selector. Keep selected value in state, update the description, and gracefully handle a value outside all bands. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=4)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the exact-band and above-last-band vectors. Test the exact band boundary description and the above-range state, then use arrow keys and assert the selected band announcement updates. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_04` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=4)`.

**15 min — design/review:** Describe a searchable catalog API and client behavior for empty/loading/error responses.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Dive Deep. Never invent work or metrics.

## Day 05 — 2026-10-21: Stable inventory row identity
**Pattern:** keys / selection state. **Leadership Principle:** Invent and Simplify. **Implementation:** Day 05 component in `frontend/src/ui.jsx` and learner helper `day05` in `frontend/src/assignments.js`.

**35 min — implementation:** Render selectable inventory rows using stable IDs with SKU fallback. Selection must survive reorder and data refresh. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=5)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the stable ID/SKU/missing-key fixture. Select a row, reorder refreshed rows, and assert selection follows its key rather than array position; an unkeyed row must not inherit selection. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_05` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=5)`.

**15 min — design/review:** Propose reusable component props that remain stable across pagination and refresh.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Invent and Simplify. Never invent work or metrics.

## Day 06 — 2026-10-22: Fulfillment tree navigation
**Pattern:** tree / keyboard navigation. **Leadership Principle:** Deliver Results. **Implementation:** Day 06 component in `frontend/src/ui.jsx` and learner helper `day06` in `frontend/src/assignments.js`.

**35 min — implementation:** Render nested fulfillment choices with keyboard-operable expand/collapse, visible focus, and disabled-node handling. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=6)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the enabled/disabled tree fixture. Test keyboard expansion, visible focus, inability to activate the disabled node, and the empty tree state. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_06` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=6)`.

**15 min — design/review:** Make nested fulfillment navigation usable with keyboard and screen readers.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Deliver Results. Never invent work or metrics.

## Day 07 — 2026-10-23: Sortable inventory table
**Pattern:** sort controls / null handling. **Leadership Principle:** Insist on Highest Standards. **Implementation:** Day 07 component in `frontend/src/ui.jsx` and learner helper `day07` in `frontend/src/assignments.js`.

**35 min — implementation:** Add sortable column headers with accessible ascending/descending state. Null values stay last and ID breaks ties. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=7)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the tied-quantity fixture. Test ascending/descending `aria-sort`, null-last behavior in both directions, and deterministic ID tie-breaking. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_07` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=7)`.

**15 min — design/review:** Discuss sorting large lists: server vs client, virtualization, and accessibility announcement.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Insist on Highest Standards. Never invent work or metrics.

## Day 08 — 2026-10-24: Placement timeline
**Pattern:** interval merging / responsive visualization. **Leadership Principle:** Are Right, A Lot. **Implementation:** Day 08 component in `frontend/src/ui.jsx` and learner helper `day08` in `frontend/src/assignments.js`.

**35 min — implementation:** Render availability intervals on a responsive timeline, merge overlaps, label time spans, and provide a text alternative. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=8)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the touching-window fixture. Test adjacent intervals render as one span, accessible text reports that merged span, and empty input gives an explicit empty state. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_08` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=8)`.

**15 min — design/review:** Design a timeline that communicates timezone, unavailable periods, and mobile layout.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Are Right, A Lot. Never invent work or metrics.

## Day 09 — 2026-10-25: Dependency-driven wizard
**Pattern:** topological order / guarded navigation. **Leadership Principle:** Think Big. **Implementation:** Day 09 component in `frontend/src/ui.jsx` and learner helper `day09` in `frontend/src/assignments.js`.

**35 min — implementation:** Show only steps whose prerequisites are complete. Prevent next-step navigation around missing dependencies and explain why a step is blocked. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=9)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the prerequisite and cycle fixtures. Test an unmet prerequisite visibly blocks navigation with an explanation, cycles stay blocked, and eligible navigation works by keyboard. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_09` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=9)`.

**15 min — design/review:** How should a wizard recover if backend policy changes while the user is on a step?

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Think Big. Never invent work or metrics.

## Day 10 — 2026-10-26: Inventory facet controls
**Pattern:** multi-select filtering. **Leadership Principle:** Bias for Action. **Implementation:** Day 10 component in `frontend/src/ui.jsx` and learner helper `day10` in `frontend/src/assignments.js`.

**35 min — implementation:** Build checkbox facets with counts, selected-state chips, clear-all, and an empty-results message. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=10)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the duplicate-value/target fixture. Test facet counts and selected chips, clear-all, and no-results feedback while preserving the expected distinct-pair output. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_10` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=10)`.

**15 min — design/review:** Design filter facets that remain understandable with many combinations and clear-all behavior.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Bias for Action. Never invent work or metrics.

## Day 11 — 2026-10-27: Debounced autocomplete
**Pattern:** async search / cancellation. **Leadership Principle:** Customer Obsession. **Implementation:** Day 11 component in `frontend/src/ui.jsx` and learner helper `day11` in `frontend/src/assignments.js`.

**35 min — implementation:** Implement an accessible combobox using an injected async search API, debounce typing, cancel stale requests, and support arrow/enter/escape. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=11)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the matching/no-match fixtures. With deferred search promises, type quickly and resolve the stale request last; assert it is ignored and ArrowDown/Enter/Escape work. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_11` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=11)`.

**15 min — design/review:** Specify combobox keyboard behavior and safe rendering of matched text.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Customer Obsession. Never invent work or metrics.

## Day 12 — 2026-10-28: Async placement cost summary
**Pattern:** derived server state. **Leadership Principle:** Frugality. **Implementation:** Day 12 component in `frontend/src/ui.jsx` and learner helper `day12` in `frontend/src/assignments.js`.

**35 min — implementation:** Fetch a fee quote through an injected async port and render integer-cent subtotal/fee/total. Show stale, loading, and error states. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=12)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the 250+300-cent and empty quote vectors. Assert integer-cent subtotal/fee/total, loading and stale states, and that a rejected refresh preserves prior data with recovery. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_12` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=12)`.

**15 min — design/review:** Keep price explanation consistent when quotes refresh asynchronously.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Frugality. Never invent work or metrics.

## Day 13 — 2026-10-29: Lazy placement list
**Pattern:** incremental rendering. **Leadership Principle:** Ownership. **Implementation:** Day 13 component in `frontend/src/ui.jsx` and learner helper `day13` in `frontend/src/assignments.js`.

**35 min — implementation:** Load the next page with a visible control and loading status; preserve prior rows and announce completion or failure. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=13)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the visible/hidden priority fixture. Test Load More retains existing cards, announces loading/completion/failure, and shows an accessible empty state. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_13` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=13)`.

**15 min — design/review:** Balance eager loading and lazy loading on a slow mobile network.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Ownership. Never invent work or metrics.

## Day 14 — 2026-10-30: Marketplace group picker
**Pattern:** connected graph / selection UI. **Leadership Principle:** Earn Trust. **Implementation:** Day 14 component in `frontend/src/ui.jsx` and learner helper `day14` in `frontend/src/assignments.js`.

**35 min — implementation:** Render connected marketplace groups as selectable chips, include isolated marketplaces, and keep selection stable when groups reorder. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=14)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the connected US/CA and isolated UK fixture. Test selection remains stable after group reordering and isolated marketplaces remain independently keyboard selectable. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_14` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=14)`.

**15 min — design/review:** Represent a seller operating across marketplaces without conflating account identity.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Earn Trust. Never invent work or metrics.

## Day 15 — 2026-10-31: URL-synced placement filters
**Pattern:** URL state / React state. **Leadership Principle:** Have Backbone; Disagree and Commit. **Implementation:** Day 15 component in `frontend/src/ui.jsx` and learner helper `day15` in `frontend/src/assignments.js`.

**35 min — implementation:** Read filter state from query parameters and write changes back without losing unrelated parameters. Normalize duplicate filters. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=15)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the padded lowercase market and duplicate-filter fixture. Test query-parameter normalization/deduplication and preserve an unrelated URL parameter after changing filters. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_15` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=15)`.

**15 min — design/review:** Explain query-cache ownership and when an optimistic update must be rolled back.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Have Backbone; Disagree and Commit. Never invent work or metrics.

## Day 16 — 2026-11-01: Policy version cache guard
**Pattern:** React Query / invalidation. **Leadership Principle:** Ownership. **Implementation:** Day 16 component in `frontend/src/ui.jsx` and learner helper `day16` in `frontend/src/assignments.js`.

**35 min — implementation:** Use TanStack Query for policy-versioned data. Invalidate on version change and prevent stale cached data from appearing as current. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=16)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the same-version and mismatched-version cache vectors. Test version mismatch invalidates/refetches policy data and stale cached content is never shown as current. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_16` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=16)`.

**15 min — design/review:** Plan cache invalidation and private response clearing on user/account change.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Ownership. Never invent work or metrics.

## Day 17 — 2026-11-02: Stale-response guard
**Pattern:** async effects / AbortController. **Leadership Principle:** Insist on Highest Standards. **Implementation:** Day 17 component in `frontend/src/ui.jsx` and learner helper `day17` in `frontend/src/assignments.js`.

**35 min — implementation:** Fetch placement results as filters change. Abort obsolete requests and only commit the response with the highest request sequence, even if an older request resolves later; test this delayed-response race. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=17)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the seq-2-new/seq-1-old response fixture. Resolve the older request last; assert the newest response stays rendered and obsolete requests are aborted. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_17` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=17)`.

**15 min — design/review:** Describe cancellation, late-response guards, and skeleton/error behavior during navigation.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Insist on Highest Standards. Never invent work or metrics.

## Day 18 — 2026-11-03: Resilient quote status
**Pattern:** async API / loading-error states. **Leadership Principle:** Customer Obsession. **Implementation:** Day 18 component in `frontend/src/ui.jsx` and learner helper `day18` in `frontend/src/assignments.js`.

**35 min — implementation:** Fetch quotes and render loading, ready, retryable, and permanent-error views with a retry control and preserved prior data. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=18)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the loading/200/503/404 sequence. Test retryable and permanent-error views differ, retry recovers, and prior quote data remains visible during refresh. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_18` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=18)`.

**15 min — design/review:** Design a quote API view resilient to timeout, partial data, and repeated refresh.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Customer Obsession. Never invent work or metrics.

## Day 19 — 2026-11-04: Typed payload boundary
**Pattern:** API decoding / rendering. **Leadership Principle:** Dive Deep. **Implementation:** Day 19 component in `frontend/src/ui.jsx` and learner helper `day19` in `frontend/src/assignments.js`.

**35 min — implementation:** Decode an unknown placement payload before rendering. Reject malformed rows, supply documented defaults, and show a safe data-error state. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=19)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the valid/malformed-row fixture. Test malformed data is rejected before rendering, documented defaults apply only to omitted optional values, and safe data-error feedback appears. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_19` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=19)`.

**15 min — design/review:** Define typed UI/API models and prevent display-only fields from becoming authority.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Dive Deep. Never invent work or metrics.

## Day 20 — 2026-11-05: Safe seller text rendering
**Pattern:** XSS / text content. **Leadership Principle:** Earn Trust. **Implementation:** Day 20 component in `frontend/src/ui.jsx` and learner helper `day20` in `frontend/src/assignments.js`.

**35 min — implementation:** Render untrusted seller-provided text as text, never HTML. Include a fixture with markup-looking content and verify it remains inert. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=20)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the markup-looking and ampersand text fixture. Assert seller text stays inert, creates no HTML element, and special characters remain readable. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_20` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=20)`.

**15 min — design/review:** Review XSS/CSRF and safe URL handling in a seller-facing UI.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Earn Trust. Never invent work or metrics.

## Day 21 — 2026-11-06: FBA placement compare flow
**Pattern:** system design / component integration. **Leadership Principle:** Think Big. **Implementation:** Day 21 component in `frontend/src/ui.jsx` and learner helper `day21` in `frontend/src/assignments.js`.

**35 min — implementation:** Build a placement comparison view that shows options, freshness, fees, selection, and recovery for partial or stale quotes. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=21)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the fresh/stale/fee options fixture. Test only fresh available choices are selectable, stale low-fee data is marked stale, and empty choices show recovery guidance. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_21` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=21)`.

**15 min — design/review:** Draw components and data flow; cover server state, URL filters, and error recovery.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Think Big. Never invent work or metrics.

## Day 22 — 2026-11-07: Accessible field component
**Pattern:** labels / aria-describedby. **Leadership Principle:** Invent and Simplify. **Implementation:** Day 22 component in `frontend/src/ui.jsx` and learner helper `day22` in `frontend/src/assignments.js`.

**35 min — implementation:** Create a reusable field component with label, hint, error, required state, and correct `aria-describedby`; verify with role-based tests. Use the synthetic cases in `fixtures/cases.json` (3 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=22)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the hint/error ID fixture. Test `aria-describedby` contains only present IDs, required state is exposed, and there are no dangling references. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_22` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=22)`.

**15 min — design/review:** Describe component boundaries, focus/validation, and how consumers customize safely.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Invent and Simplify. Never invent work or metrics.

## Day 23 — 2026-11-08: Bounded quote polling
**Pattern:** async polling / cancellation. **Leadership Principle:** Ownership. **Implementation:** Day 23 component in `frontend/src/ui.jsx` and learner helper `day23` in `frontend/src/assignments.js`.

**35 min — implementation:** Poll quotes with capped exponential delays and a maximum attempt count. Stop after success, cancellation, or max attempts. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=23)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the capped-delay fixture. With fake timers, assert backoff caps at the configured maximum, stops at max attempts, and cancellation clears scheduled work. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_23` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=23)`.

**15 min — design/review:** Choose polling vs push for quote updates and explain freshness/battery tradeoff.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Ownership. Never invent work or metrics.

## Day 24 — 2026-11-09: Web-vitals panel
**Pattern:** performance / privacy-safe telemetry. **Leadership Principle:** Success and Scale Bring Broad Responsibility. **Implementation:** Day 24 component in `frontend/src/ui.jsx` and learner helper `day24` in `frontend/src/assignments.js`.

**35 min — implementation:** Render LCP samples into accessible good/needs-improvement/poor states; never send raw URLs or user identifiers. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=24)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the LCP threshold and empty-sample fixtures. Assert accessible good/needs-improvement/poor states, no fabricated empty metric, and no raw URL or user ID reaches telemetry. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_24` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=24)`.

**15 min — design/review:** Pick a frontend SLO and privacy-safe telemetry fields for it.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Success and Scale Bring Broad Responsibility. Never invent work or metrics.

## Day 25 — 2026-11-10: Recommendation explanation panel
**Pattern:** human-readable reasons / safe text. **Leadership Principle:** Learn and Be Curious. **Implementation:** Day 25 component in `frontend/src/ui.jsx` and learner helper `day25` in `frontend/src/assignments.js`.

**35 min — implementation:** Render only allowlisted explanation codes as plain text; cap content and show source/freshness metadata. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=25)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the CAPACITY/SECRET allowlist fixture. Test disallowed codes are omitted, allowed text is rendered inertly within the content cap, and source/freshness metadata is visible. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_25` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=25)`.

**15 min — design/review:** Design explanation UI for generated recommendations with source, freshness, and human override.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Learn and Be Curious. Never invent work or metrics.

## Day 26 — 2026-11-11: Quote contract tests
**Pattern:** component/API contract testing. **Leadership Principle:** Insist on Highest Standards. **Implementation:** Day 26 component in `frontend/src/ui.jsx` and learner helper `day26` in `frontend/src/assignments.js`.

**35 min — implementation:** Add contract cases for valid, empty, malformed, stale, and partial quote responses. Render safe states for each. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=26)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the zero-cent-valid and negative-cent-invalid quote fixtures. Test zero is not treated as missing, malformed/stale/partial data fails safely, and invalid data cannot be selected. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_26` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=26)`.

**15 min — design/review:** Create component/API contract cases for empty, malformed, stale, and partial quote payloads.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Insist on Highest Standards. Never invent work or metrics.

## Day 27 — 2026-11-12: Responsive image gallery
**Pattern:** responsive image / accessibility. **Leadership Principle:** Insist on Highest Standards. **Implementation:** Day 27 component in `frontend/src/ui.jsx` and learner helper `day27` in `frontend/src/assignments.js`.

**35 min — implementation:** Render placement images with responsive sizing and meaningful alt text; identify and handle missing or invalid descriptors. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=27)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the empty-alt/zero-width and valid-image fixtures. Test each descriptor error, meaningful alt text, responsive sizing, and a safe fallback for missing image data. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_27` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=27)`.

**15 min — design/review:** Audit a responsive list for keyboard flow, contrast, image alternatives, and rendering cost.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Insist on Highest Standards. Never invent work or metrics.

## Day 28 — 2026-11-13: Keyboard tab bar
**Pattern:** ARIA tabs / focus management. **Leadership Principle:** Earn Trust. **Implementation:** Day 28 component in `frontend/src/ui.jsx` and learner helper `day28` in `frontend/src/assignments.js`.

**35 min — implementation:** Implement roving tabindex, arrow-key movement, Home/End, and panel selection for a three-tab placement view. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=28)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the three-tab and invalid-active fixtures. Test Arrow/Home/End, roving tabindex, selected state, and exposure of only the active panel. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_28` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=28)`.

**15 min — design/review:** Use a timed exercise, then explain keyboard semantics, history behavior, and a novel edge case.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Earn Trust. Never invent work or metrics.

## Day 29 — 2026-11-14: Timed mock: quote comparison feature
**Pattern:** role mock / async integration. **Leadership Principle:** Deliver Results. **Implementation:** Day 29 component in `frontend/src/ui.jsx` and learner helper `day29` in `frontend/src/assignments.js`.

**35 min — implementation:** Build a small comparison panel using the injected API: loading, partial results, selection, refresh, and error recovery. Keep the UI responsive while requests overlap. Use the synthetic cases in `fixtures/cases.json` (2 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=29)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the visible/hidden fee options fixture. Test a hidden cheaper option never wins, IDs/minimum fee match the fixture, and partial refresh failure preserves safe prior results. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_29` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=29)`.

**15 min — design/review:** Explain the comparison panel states, async retry boundary, keyboard flow, and one tradeoff to revisit. Reserve the separate 10-minute STAR block below for truthful reflection.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Deliver Results. Never invent work or metrics.

## Day 30 — 2026-11-15: Practice retrospective panel
**Pattern:** progress visualization / reflection. **Leadership Principle:** Customer Obsession & Ownership. **Implementation:** Day 30 component in `frontend/src/ui.jsx` and learner helper `day30` in `frontend/src/assignments.js`.

**35 min — implementation:** Render attempted versus passed counts per topic, highlight all tied weakest topics, and capture one next-step note without claiming mastery. Use the synthetic cases in `fixtures/cases.json` (3 cases). Add at least one adversarial edge case and explain why it matters.
**Run this day:** `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=30)`. While this task is a TODO, the command reports the learner gap; after implementation it checks just this day.

**30 min — component/adversarial test:** Use the attempted/passed and tied-weakest fixtures. Test all tied weakest topics are shown, counts remain distinct including zero attempts, and next-step notes do not claim mastery. Add the learner-owned interaction assertion in `frontend/src/ui.test.jsx`; use the authoritative `frontend_30` cases from `fixtures/cases.json`; run `(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)` and `(cd practice/fba-coverstory-30day-practice/fba && make test-frontend-day DAY=30)`.

**15 min — design/review:** Redo your weakest UI/API pattern, state accessibility/performance tradeoffs, and plan next 30 days.

**10 min — STAR:** Write a truthful situation, your personal action, evidence/result, and learning connected to Customer Obsession & Ownership. Never invent work or metrics.
