# Starter coverage and verification

## Assignment contract coverage

The requested final plan contains 594 consecutive daily primary assignments and six linked component cards (600 contracts total, 44 sprints). Phase 1 is 14 October 2026 through 29 May 2027 (228 days), followed by 366 days of deeper mastery. The authoritative saved-contract archive and final operating record were not available as local bytes in this executor, so this checkout does **not** contain a verified mapping of those 600 contracts. Do not infer missing titles, dates, tests, acceptance criteria, or completion state. Materialize and integrity-check the specified package before claiming complete assignment coverage.

| Area | Runnable scaffolds present | Learner work preserved | Current status |
| --- | --- | --- | --- |
| CAP-42 PostgreSQL aggregation | Java harness, schema, fixture | `learner-query.sql` | Maven/PostgreSQL harness not executed in this Linux cloud checkout |
| CAP-43 event policy and request validation | Spring MVC scaffolds and acceptance tests | Java TODO methods | Maven checks blocked by missing Maven runtime |
| CAP-44 retrieval ranking | Python types, fixture, infrastructure and acceptance tests | `rank_runbooks` | Python infrastructure suite runnable; acceptance is expected to fail on TODO |
| CAP-45 event reconciliation | PostgreSQL harness, schema, fixture, tests | `learner-query.sql` | Maven/PostgreSQL harness not executed in this checkout |
| CoverStory React app | None present in baseline | N/A | Deferred pending authoritative design archive and assignment contracts |
| CoverStory Spring/PostgreSQL modules | Architecture guide only; no product implementation | N/A | Deferred pending final contracts and executable scaffold design |
| Security, retention, payment and dispatch contracts | Principles documented in architecture guide | Assessed business logic remains learner work | Not yet encoded as executable contracts or checks |
| Daily primary/component mapping | No verified contract files present | N/A | Blocked on authoritative contract archive |

## Verified preflight

- Repository path: `/workspace/capstone-project`.
- Remote: `origin https://github.com/advait-ravi-marathe/capstone-project.git`.
- Branch: `work`; HEAD `4f362eb30739d73a4e90405a30aada6401145afe`; initial worktree clean.
- The checkout is isolated from the Mac task. No Mac files were read or synchronized.
- Java 21.0.12 and Python 3.12.14 are present. `mvn` and a cached wrapper distribution are absent.
- No repository `AGENTS.md`, `.agents`, or `.codex` instructions were found.
- The input archive record resolves to the expected name and size (1,761,461 bytes), but local materialization failed twice in the bundled Library download flow with `hosted apps tools/list request failed: network`. The fallback read route reports no readable content for this ZIP. No archive bytes or SHA-256 have been verified in this executor.
- The original design/source archive has not been materialized; no visual source assets were inspected.

## Commands and evidence

Run the independent Python setup suite with:

```sh
python3 -m unittest discover -s ai/tests -p test_infrastructure.py
```

Run the learner ranking suite separately to observe expected TODO failures:

```sh
python3 -m unittest discover -s ai/tests -p test_ranking.py
```

Maven commands are documented in the root README, but cannot run until Maven 3.9+ or a cached wrapper distribution exists. No runtime services, credentials, deployments, remote pushes, or Scrum edits were performed.
