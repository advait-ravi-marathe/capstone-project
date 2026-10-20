# Day 17 — 2026-11-05: Render product choices in React/TypeScript

**State:** Contract defined; instructor fixture/test scaffold and learner attempt are pending. This page is a planned contract, not a test result.

## Learner seam

`ProductPanel.tsx` in UI workspace if established. Keep the business rule in this focused subject. Do not implement later-day behavior early.

## Acceptance invariants

Typed fixture renders labels/options; loading/error/empty explicit; accessible names; no hard-coded production URL. Test adapter avoids dependency on earlier live backend.

## Required edge cases

Loading, error, empty, product options, accessibility query, no real fetch.

## Prerequisite and isolation

Day 4 contract; canned fetch adapter. Earlier unfinished collaborators are replaced by explicit test-only canned values in this day's isolated test; do not copy their business solution into production. A later integrated gate remains blocked until its listed prerequisites are genuinely completed.

## Concept (25 minutes)

Typed props and discriminated UI state [K,L].

## Evidence (10 minutes)

Component tests and screenshot. Record dated attempt state (`not attempted`, `attempted`, `contract passed`, or `integrated passed`), exact command/exit status, changed paths, assertion before/after where applicable, and an invariant/trade-off explanation. DSA notes include Java attempt, edge cases, complexity and independent-solve status.

## Planned isolated command

`./mvnw -Plearner-tests -Dtest=Day17ContractTest test`

This exact selector is part of the contract, but this test class is not installed yet. The command must not be reported as passing until the instructor scaffold exists and is actually run. Infrastructure checks are separate.

## Separate Java DSA block (60 minutes)

LeetCode 125 Valid Palindrome. Use 10 minutes recall/example, 35 minutes independent attempt, 10 minutes tests/complexity and 5 minutes error log. An unfinished attempt is valid work, not a solved claim. Prior solved history is preserved and must not be inferred from a scaffold.
