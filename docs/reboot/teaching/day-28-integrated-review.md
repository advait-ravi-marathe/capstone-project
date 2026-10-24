# Day 28 — Review the real browser-to-database gate

Review only. No new lecture bundle or implementation feature is assigned. The integrated gate requires genuinely completed Days 14–27 prerequisites. The installed learner test is expected to stop at unfinished seams; opening a browser is not a pass. Use 25 minutes for review, 45 minutes for the Q14 interview-first mock, 40 minutes for the existing browser/database EAI slice, and 10 minutes for evidence. The mock and slice share the existing 85-minute practice allocation; see the [Sprint 2 session scope](../interview/sprint2-session-scope.md).

## Worked trace

Use a synthetic library reservation as a reminder of previously taught boundaries:

1. A user submits a request through the existing session and CSRF boundary.
2. The controller validates the request and checks the owner.
3. A transaction commits one reservation row.
4. The response carries the server-issued reservation ID.
5. The UI confirms that ID; after reload, a fresh read returns the same persisted ID.

The fresh read and row count establish persistence. The UI's pending guard only reduces duplicate clicks; a database uniqueness rule arbitrates multiple writers. A stale revision rejects without a new row. A foreign owner's record is concealed. An outage is an error, not confirmation.

Blocked example: an owner-policy TODO throws before SQL. The honest observation is an error response, no successful write, no new row, and no reservation after reload. Report “blocked at ownership”; do not substitute a canned success into this integrated gate.

## Apply to the installed harness

Inspect `Day28BrowserToDatabaseTest` and its actual first failed assertion. Trace one observed browser/API/database case and one controlled failure. If a prerequisite is incomplete, name it and the next diagnostic instead of claiming downstream behavior. The production integrated result remains blocked until the real components and PostgreSQL path run.
