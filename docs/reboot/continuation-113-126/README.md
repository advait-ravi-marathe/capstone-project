# CoverStory Days 113–126: manual recovery of retained local work

Status: scaffold/learning plan; learner completion is not recorded. This batch follows the event-log-backed local delivery work in Days 99–112. It addresses the missing operator decision surface: exact-key inspection, reversible quarantine/release, and explicit one-key retry/reconciliation.

## Scope and boundaries

Keep the existing immutable ISSUED source event, captured work identity, receipt payload and timestamp. V005 adds only quarantine marker/reason, a control revision, and append-only applied-decision history. Control revision advances on quarantine/release only; it is not a lifecycle version and does not imply receipt/acknowledgement is unchanged. Receipt creation and acknowledgment remain separate commits.

All operations are explicit and local. There is no broker, scheduler, lease, timer retry, bulk repair, deletion, payload editing, new HTTP route, UI, cloud dependency, or real customer data. Recovery depends on repeated operator invocation and retained rows; no eventual or external delivery guarantee is claimed. A held conflict stays visible. Release does not repair it.

The receipt and acknowledgment paths and recovery transitions share a lock on the work row. After waiting for that lock, code reads the receipt in a fresh READ COMMITTED statement. Quarantined rows are filtered in SQL before ordering and LIMIT. The existing dispatcher remains fail-fast.

## Daily budget

Daily total is 180/195/210 minutes: 60/75/90 minutes of DSA by exercise count and unchanged 120 minutes engineering (one shared 25-minute study cap, 85 minutes build/test, 10 minutes evidence). Video, pauses, worked traces, focused reading and teach-back all fit inside the same 25 minutes. Carry unfinished study forward and reduce implementation scope. No new mandatory DSA anchor is introduced; the actual-attempt queue runs first.

The optional reference course is **Master Hibernate and JPA with Spring Boot in 100 Steps**, in28Minutes Official, course ID 1341538. Access is unconfirmed; course videos were not audited. Exact catalog titles, section titles, item IDs and durations appear in the manifest and individual contracts. These are JDBC/transaction foundations, not lessons on this exact recovery implementation. Use the worked guide and focused official reading if inaccessible. No purchase or enrollment is implied.

## Instructor scaffolding and learner seams

V005 is instructor-supplied additive scaffolding. It preserves V004 and gives existing work rows revision 0. The learner work is the evidence classification, exact-key inspection mapping, transition preconditions, eligible SQL filtering, one-key recovery/result handling, and specified acceptance assertions. Existing Days 99–112 TODOs remain learner-owned. Scaffold status, test green, or temporary conformance witnesses do not count as learner completion.

## Acceptance limits

Acceptance uses disposable PostgreSQL and independent transactions/connections. Snapshot assertions compare stable identities, payload and timestamps, not only counts. Concurrency uses barriers/latches and finite watchdogs; watchdog duration is not a correctness or production deadline. Fresh-JVM tests keep PostgreSQL running and prove application-process restart behavior only.

Days 119 and 126 are recovery/remediation days. They rerun only already introduced work and do not add features. Full commands and exact selectors live in `manifest.json` and each daily contract. Ticket-ready direct course and worked-resource blocks are in `scrum-owner-manifest.json`; no board write is included here.

## DSA source of truth

`docs/reboot/continuation-31-44/dsa/actual-attempt-log.json` remains canonical. Run `python3 scripts/dsa-review-queue.py --on-date YYYY-MM-DD` for the corresponding day. Empty published attempts mean unrecorded here, not proof of no private work.

When an explicit `--coverstory.recovery.action=...` argument is supplied, the executable JAR selects a minimal non-web recovery context containing the same JDBC store, shared lock guard, transaction manager, and manual service. This avoids starting unrelated app routes for a one-shot CLI action. Ordinary web startup does not run recovery. The packaged selector exercises the explicit CLI and PostgreSQL only; protected issuance replay and renewal preview remain the existing compatibility regression, not new UI coverage.
FDE alignment: See [the North Star amendment](../fde-north-star-amendment.md) and [reference-only alignment overlay](../fde-alignment.json). Existing curriculum, prerequisites, budgets, DSA history and learner status remain unchanged; this adds purpose and evidence boundaries, not completion credit.
