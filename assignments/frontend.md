# Front-End Engineer II — FBA: 30 dated assignments

Amazon job ID: **10575321**. Each day takes 60 minutes: 35 minutes implement/solve, 15 minutes role-specific design or review, and 10 minutes Leadership Principle STAR practice. Dates and case fixtures are in [`../SPRINT-MANIFEST.json`](../SPRINT-MANIFEST.json).

## Day 01 — 2026-10-16: Validate seller registration fields
**Pattern:** forms + hashing. **Leadership Principle:** Customer Obsession.

**35 min — code:** Return field errors for empty trimmed name/email and malformed email; output object maps field names to error strings, omitting valid fields. Implement `day01`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Sketch an accessible seller registration form, labels, error summary, focus placement, and server validation boundary.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Customer Obsession. Do not invent work history or metrics.

## Day 02 — 2026-10-17: Filter inventory responsively
**Pattern:** filtering + two pointers. **Leadership Principle:** Ownership.

**35 min — code:** Filter items by case-insensitive name substring and min/max quantity; preserve input order. Implement `day02`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Explain table filtering on narrow screens and keyboard-accessible filter controls.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Ownership. Do not invent work history or metrics.

## Day 03 — 2026-10-18: Model accessible async form state
**Pattern:** async state machine. **Leadership Principle:** Learn and Be Curious.

**35 min — code:** Given event sequence, return final state among idle, submitting, success, error. SUBMIT->submitting, RESOLVE->success, REJECT->error, RESET->idle. Implement `day03`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Design disabled/loading/error states and announce state changes without trapping keyboard users.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Learn and Be Curious. Do not invent work history or metrics.

## Day 04 — 2026-10-19: Locate a price band
**Pattern:** binary search. **Leadership Principle:** Dive Deep.

**35 min — code:** Return lower-bound index in sorted numeric placement bands. Implement `day04`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Describe a searchable catalog API and client behavior for empty/loading/error responses.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Dive Deep. Do not invent work history or metrics.

## Day 05 — 2026-10-20: Stable component key selection
**Pattern:** stable keys. **Leadership Principle:** Invent and Simplify.

**35 min — code:** Return stable item keys, preferring id and falling back to sku; return null for missing both. Implement `day05`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Propose reusable component props that remain stable across pagination and refresh.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Invent and Simplify. Do not invent work history or metrics.

## Day 06 — 2026-10-21: Flatten fulfillment navigation
**Pattern:** tree navigation. **Leadership Principle:** Deliver Results.

**35 min — code:** Flatten nested nodes to labels in preorder, skipping disabled nodes and their children. Implement `day06`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Make nested fulfillment navigation usable with keyboard and screen readers.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Deliver Results. Do not invent work history or metrics.

## Day 07 — 2026-10-22: Sort inventory rows
**Pattern:** sorting. **Leadership Principle:** Insist on Highest Standards.

**35 min — code:** Sort by requested key; nulls last; tie-break by id ascending. Direction is asc or desc. Implement `day07`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Discuss sorting large lists: server vs client, virtualization, and accessibility announcement.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Insist on Highest Standards. Do not invent work history or metrics.

## Day 08 — 2026-10-23: Build placement timeline segments
**Pattern:** interval timeline. **Leadership Principle:** Are Right, A Lot.

**35 min — code:** Merge overlapping/touching availability windows for a timeline. Implement `day08`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Design a timeline that communicates timezone, unavailable periods, and mobile layout.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Are Right, A Lot. Do not invent work history or metrics.

## Day 09 — 2026-10-24: Order dependent wizard steps
**Pattern:** dependency UI. **Leadership Principle:** Think Big.

**35 min — code:** Return lexical topological order for step dependencies; return null on cycle. Implement `day09`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** How should a wizard recover if backend policy changes while the user is on a step?

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Think Big. Do not invent work history or metrics.

## Day 10 — 2026-10-25: Compute facet combinations
**Pattern:** facets. **Leadership Principle:** Bias for Action.

**35 min — code:** Return all unique pairs from selected facet values whose numeric sum equals target. Implement `day10`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Design filter facets that remain understandable with many combinations and clear-all behavior.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Bias for Action. Do not invent work history or metrics.

## Day 11 — 2026-10-26: Autocomplete with safe highlighting
**Pattern:** autocomplete. **Leadership Principle:** Customer Obsession.

**35 min — code:** Return matching options by case-insensitive prefix, sorted; do not emit HTML, return plain text values. Implement `day11`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Specify combobox keyboard behavior and safe rendering of matched text.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Customer Obsession. Do not invent work history or metrics.

## Day 12 — 2026-10-27: Compute placement cost summary
**Pattern:** derived state. **Leadership Principle:** Frugality.

**35 min — code:** Return subtotal and total using integer cents; total adds fee cents; empty rows total fee only. Implement `day12`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Keep price explanation consistent when quotes refresh asynchronously.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Frugality. Do not invent work history or metrics.

## Day 13 — 2026-10-28: Prioritize visible content
**Pattern:** lazy loading. **Leadership Principle:** Ownership.

**35 min — code:** Order cards with visible=true first, then priority descending, then id ascending. Implement `day13`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Balance eager loading and lazy loading on a slow mobile network.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Ownership. Do not invent work history or metrics.

## Day 14 — 2026-10-29: Group marketplaces
**Pattern:** graph connectivity. **Leadership Principle:** Earn Trust.

**35 min — code:** Return connected marketplace groups from undirected links, including isolated IDs; sort IDs/groups. Implement `day14`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Represent a seller operating across marketplaces without conflating account identity.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Earn Trust. Do not invent work history or metrics.

## Day 15 — 2026-10-30: Build stable query keys
**Pattern:** cache keys. **Leadership Principle:** Have Backbone; Disagree and Commit.

**35 min — code:** Return canonical query key ["placements", normalized market, sorted unique filters]. Implement `day15`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Explain query-cache ownership and when an optimistic update must be rolled back.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Have Backbone; Disagree and Commit. Do not invent work history or metrics.

## Day 16 — 2026-10-31: Guard stale policy version
**Pattern:** cache invalidation. **Leadership Principle:** Ownership.

**35 min — code:** Given current policy version and cached version, return true only if equal and data age <= maxAge. Implement `day16`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Plan cache invalidation and private response clearing on user/account change.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Ownership. Do not invent work history or metrics.

## Day 17 — 2026-11-01: Ignore stale response
**Pattern:** async race. **Leadership Principle:** Insist on Highest Standards.

**35 min — code:** Given response versions in completion order, return last response only if its request sequence is newest. Implement `day17`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Describe cancellation, late-response guards, and skeleton/error behavior during navigation.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Insist on Highest Standards. Do not invent work history or metrics.

## Day 18 — 2026-11-02: Choose quote UI state
**Pattern:** API resilience. **Leadership Principle:** Customer Obsession.

**35 min — code:** Map status: loading->loading, 200->ready, 429/503->retryable, other 4xx->error; preserve order. Implement `day18`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Design a quote API view resilient to timeout, partial data, and repeated refresh.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Customer Obsession. Do not invent work history or metrics.

## Day 19 — 2026-11-03: Normalize placement payload
**Pattern:** payload mapping. **Leadership Principle:** Dive Deep.

**35 min — code:** Return rows with id, quantity default 0, and title trimmed; skip rows missing id. Implement `day19`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Define typed UI/API models and prevent display-only fields from becoming authority.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Dive Deep. Do not invent work history or metrics.

## Day 20 — 2026-11-04: Escape user supplied text
**Pattern:** XSS / validation. **Leadership Principle:** Earn Trust.

**35 min — code:** Return text with &, <, >, ", and apostrophe escaped as entities in that order-safe manner. Implement `day20`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Review XSS/CSRF and safe URL handling in a seller-facing UI.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Earn Trust. Do not invent work history or metrics.

## Day 21 — 2026-11-05: Rank viable placement options
**Pattern:** system design. **Leadership Principle:** Think Big.

**35 min — code:** Given placement options, exclude unavailable or stale records and return remaining IDs sorted by ascending totalCents, then ID. Treat only `available: true` and `fresh: true` options as viable. Implement `day21`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Draw components and data flow; cover server state, URL filters, and error recovery.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Think Big. Do not invent work history or metrics.

## Day 22 — 2026-11-06: Build accessible field description IDs
**Pattern:** component design. **Leadership Principle:** Invent and Simplify.

**35 min — code:** Return the input ID followed by non-empty hintId and errorId, joined by spaces. Do not include absent IDs. Use this helper as the coding artifact; design the reusable component separately. Implement `day22`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Describe component boundaries, focus/validation, and how consumers customize safely.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Invent and Simplify. Do not invent work history or metrics.

## Day 23 — 2026-11-07: Bound quote refreshes
**Pattern:** polling / push. **Leadership Principle:** Ownership.

**35 min — code:** Return polling delays starting at base, doubling and capped; stop after maxAttempts. Implement `day23`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Choose polling vs push for quote updates and explain freshness/battery tradeoff.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Ownership. Do not invent work history or metrics.

## Day 24 — 2026-11-08: Classify performance budget
**Pattern:** web vitals. **Leadership Principle:** Success and Scale Bring Broad Responsibility.

**35 min — code:** Classify LCP ms: <=2500 good, <=4000 needs-improvement, else poor. Implement `day24`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Pick a frontend SLO and privacy-safe telemetry fields for it.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Success and Scale Bring Broad Responsibility. Do not invent work history or metrics.

## Day 25 — 2026-11-09: Render safe recommendation reasons
**Pattern:** explainability. **Leadership Principle:** Learn and Be Curious.

**35 min — code:** Keep reasons whose code is allowlisted; return plain text capped at 120 chars. Implement `day25`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Design explanation UI for generated recommendations with source, freshness, and human override.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Learn and Be Curious. Do not invent work history or metrics.

## Day 26 — 2026-11-10: Validate quote response shape
**Pattern:** contract testing. **Leadership Principle:** Insist on Highest Standards.

**35 min — code:** Return true only when each quote has string id, integer cents >=0, and currency string length 3. Implement `day26`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Create component/API contract cases for empty, malformed, stale, and partial quote payloads.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Insist on Highest Standards. Do not invent work history or metrics.

## Day 27 — 2026-11-11: Find responsive image issues
**Pattern:** performance + accessibility. **Leadership Principle:** Insist on Highest Standards.

**35 min — code:** Return sorted issue codes for image descriptors: missing alt -> ALT, width<=0 -> SIZE. Implement `day27`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Audit a responsive list for keyboard flow, contrast, image alternatives, and rendering cost.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Insist on Highest Standards. Do not invent work history or metrics.

## Day 28 — 2026-11-12: Tab bar state helper
**Pattern:** timed frontend mock. **Leadership Principle:** Earn Trust.

**35 min — code:** Given tabs and active ID, return active index or 0 when missing. Implement `day28`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Use a timed exercise, then explain keyboard semantics, history behavior, and a novel edge case.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Earn Trust. Do not invent work history or metrics.

## Day 29 — 2026-11-13: FBA mock: summarize visible placement choices
**Pattern:** full role mock. **Leadership Principle:** Deliver Results.

**35 min — code:** Return the visible option count, minimum fee cents among visible options (null if none), and IDs of visible options sorted by ID. The separate mock block tests timed implementation, design explanation and STAR. Implement `day29`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** 15-minute design explanation plus 10-minute truthful STAR story; note one tradeoff to revisit.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Deliver Results. Do not invent work history or metrics.

## Day 30 — 2026-11-14: Weakest frontend pattern redo
**Pattern:** retrospective. **Leadership Principle:** Customer Obsession & Ownership.

**35 min — code:** Return a safe quantity label: null/negative -> "Unavailable", else singular/plural units. Implement `day30`. Use the synthetic acceptance examples in `fixtures/cases.json`; add an edge case before you stop.

**15 min — design/review:** Redo your weakest UI/API pattern, state accessibility/performance tradeoffs, and plan next 30 days.

**10 min — STAR:** Give one truthful situation, your individual action, measurable result, and what you learned related to Customer Obsession & Ownership. Do not invent work history or metrics.
