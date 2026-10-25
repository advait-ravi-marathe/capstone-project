# Outcome roadmap amendment: stable engineering units through calendar Study Day 774

**Approved planning amendment; not learner completion.** Add the six identified gaps at their prerequisite points: OpenAPI compatibility and local OIDC after engineering unit 70; schema evolution after unit 154; AI data governance before unit 439; and offline IaC plus enterprise launch gates before unit 523. This adds 42 calendar study days and extends the horizon from 732 to **Study Day 774, 7 December 2028**. Engineering-unit IDs, paths, source records, learner statuses and evidence remain stable. The dated [study-calendar crosswalk](../calendar/study-calendar.json) is authoritative for the changed chronology; the old `Days N` labels identify units, not calendar positions. The first 30 days and the 26 October 2026 start remain unchanged. The separate 26 October 2029 professional-ownership review remains an external evidence horizon, not another year of assignments.

## Fixed capacity

| Window | Days | Engineering | One shared block | Total |
|---|---:|---:|---:|---:|
| Calendar Days 271–774 | 504 | 83,160 min / **1,386 h** | 22,680 min / **378 h** | **1,764 h** |
| Per full sprint | 14 | 2,310 min / 38.5 h | 630 min / 10.5 h | 49 h |
| Reserve inside engineering | 20% | 16,632 min / **277.2 h** | — | included above |
| Planned deep work after reserve | — | 66,528 min / **1,108.8 h** | — | included above |

From calendar Study Day 172 onward, each day remains 165 engineering minutes plus exactly one shared 45-minute question-bank/review block. Calendar Study Day 172 is 15 April 2027. Through Study Day 171, engineering remains 120 minutes; DSA is separate. The 20% reserve is inside engineering time, not overtime. Gaps add 102.75 engineering hours and 18.75 shared hours; existing engineering units 155–171 cross the fixed budget boundary and add 25.5 hours, for 147 total hours of calendar capacity after insertion. Those 17 units use the additional time for named recovery, integration, question-bank review, or observation—not feature expansion. The DSA repository, problem cadence, attempt dates, and D+3/+7/+21 review logic remain unchanged.

## Inserted gap sequence and gates

| Stable inserted units | Calendar study days / dates | Capacity regime | Required evidence and boundaries |
|---|---|---|---|
| `GAP-OPENAPI-01..05` | 71–75 · 4–8 Jan 2027 | 120 engineering min/day | Version one existing API contract; an independent consumer checker accepts an additive compatible change and rejects a breaking response/field change. |
| `GAP-OIDC-01..09` | 76–84 · 9–17 Jan 2027 | 120 engineering min/day | Exercise the real Spring Security resource-server path against one disposable local OIDC test IdP. Validate issuer/audience/time/signature, key rotation, and tenant isolation. No external IdP, production credentials, or real identity deployment. |
| `GAP-SCHEMA-01..07` | 169–175 · 12–18 Apr 2027 | Days 169–171: 120 engineering min; 172–175: 165 engineering + one shared 45 | On synthetic PostgreSQL data, rehearse additive expand/contract, old/new version compatibility, checkpointed restartable backfill, interruption/restart idempotence, row reconciliation, and forward recovery. Do not contract while old readers/writers remain. |
| `GAP-AIGOV-01..07` | 460–466 · 28 Jan–3 Feb 2028 | 165 engineering + one shared 45/day | Define data inventory/minimization and provider boundaries; test retention/deletion across synthetic source, cache/index, and evaluation artifacts; measure relevant access/tenant/deletion evaluation slices with independent expected results. No consequential underwriting or provider call. |
| `GAP-IAC-01..07` | 551–557 · 28 Apr–4 May 2028 | 165 engineering + one shared 45/day | Use one IaC tool for offline validation, plan review, drift detection, recreation, rollback and cleanup rehearsals. A valid plan is not a deployment; no credentials, cloud resources, or spending. |
| `GAP-ENTERPRISE-01..07` | 558–564 · 5–11 May 2028 | 165 engineering + one shared 45/day | Simulate named security, privacy, procurement, service and operations owners; a missing/denied control blocks launch. Simulation never satisfies actual enterprise approval, production operation, or customer evidence. |

Live execution remains blocked by the existing external authorization rules even before the simulated enterprise gate: no model/provider calls, external data, credentials, cloud account/resources, costs, or deployment without separate authorization. The later AI, IaC and enterprise blocks are outcome-based, not executable labs or pre-created daily Stories.

## Outcome phases and realistic envelopes

CoverStory remains the primary flagship; StreamForge remains gated by actual prior learner evidence. Phases are overlapping emphases, not isolated prerequisites or one-technology checklists. Envelopes include the same 20% reserve and shared block.

| Days / planned dates | Envelope | Emphasis and measurable exit evidence |
|---|---:|---|
| **Unit phase 271–354 · Study Days 292–375 · 13 Aug–4 Nov 2027** | 84 d · 231 engineering h · 63 shared h | Java/Spring runtime behavior, JVM/OS/concurrency, profiling/JFR, PostgreSQL internals and measured query behavior. Build repeatable workload/plan/latency baselines with exact result correctness; diagnose unfamiliar contention or query regressions. Start deployment readiness early; begin Python/math before model work. Exit: reproduce diagnosis on an unfamiliar workload. **Existing detailed units 271–284 map to Study Days 292–305.** |
| **Unit phase 355–438 · Study Days 376–459 · 5 Nov 2027–27 Jan 2028** | 84 d · 231 engineering h · 63 shared h | Distributed consistency, transaction boundaries, idempotency, event delivery, failure/recovery, capacity and observability. Compare Redis, Kafka and RabbitMQ against workload and operational-cost cases. Exit: deterministic failure injection, repeated recovery/restore cycles, documented limits and reviewed use/no-use decisions. |
| **Unit phase 439–522 · Study Days 467–550 · 4 Feb–27 Apr 2028** | 84 d · 231 engineering h · 63 shared h | Python/ML grounding; one justified live-provider RAG or bounded-agent path only after offline baselines, authorization and safety controls. Measure retrieval, abstention, uncertainty slices, prompt-injection and tenant-isolation cases, timeout/fallback, latency and cost. No consequential underwriting/claims decisions. |
| **Unit phase 523–606 · Study Days 565–648 · 12 May–3 Aug 2028** | 84 d · 231 engineering h · 63 shared h | AWS deployment/security/operations: least privilege, secrets, network boundaries, CI/CD, telemetry, SLOs, incident response, rollback and cost controls. Rehearse offline first; actual deployment and sustained operations require separate authorization. A public endpoint alone is not ownership. |
| **Unit phase 607–676 · Study Days 649–718 · 4 Aug–12 Oct 2028** | 70 d · 192.5 engineering h · 52.5 shared h | Second materially distinct enterprise-integration opportunity. StreamForge remains gated. Without an authorized real opportunity, use synthetic contracts to test transfer, not customer delivery. Exit: changed identity/data contracts, compatibility/handoff tests, and external review/use only if real. |
| **Unit phase 677–732 · Study Days 719–774 · 13 Oct–7 Dec 2028** | 56 d · 154 engineering h · 42 shared h | Reusable platform capabilities across CoverStory and eligible StreamForge. Measure two-context reuse, reliability/cost/regression improvement, independent review/use, and support boundaries. Portfolio reuse is not professional Staff/FDE ownership. |

Operations-readiness and math foundations start early while deep AI follows foundations and baseline evidence. Dates are plans; current evidence and authorization gates remain dynamic. Unmet work moves within the envelope.

## Verified source families for future sprint planning

These are primary references checked on 25 October 2026. The first 21 inserted units use exact sources below; all titles, versions and headings must be rechecked before the assignment date. Later outcomes are not executable labs. Keep existing confirmed AWS and in28Minutes providers when exact available lessons are verified; otherwise use the official documentation. Do not invent lectures.

| Area | Official reference checked | Planned use and boundary |
|---|---|---|
| JVM profiling | Oracle, [The jfr Command](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jfr.html), `jfr summary` and `jfr print` subcommands | Local JFR files and profiling practice; do not infer service behavior from a tiny lab. |
| PostgreSQL | PostgreSQL 18, [14.1 Using EXPLAIN](https://www.postgresql.org/docs/18/using-explain.html), basics, `EXPLAIN ANALYZE`, caveats | Compare estimates, rows and buffers; never set CI timing or node-type expectations from one environment. |
| Spring operations | Spring Boot 4.1.2, [Observability](https://docs.spring.io/spring-boot/reference/actuator/observability.html) and [Metrics](https://docs.spring.io/spring-boot/reference/actuator/metrics.html) | Logging, metrics and traces concepts; exporting telemetry or exposing endpoints requires separate deployment/security review. |
| Python/math | Python 3.12, [statistics — Mathematical statistics functions](https://docs.python.org/3.12/library/statistics.html), averages and spread | Foundation for distributions and uncertainty; examples are not a substitute for broader probability/statistics study. |
| Redis | Redis, [Redis data types](https://redis.io/docs/latest/develop/data-types/) | Compare candidate data structures only when a workload justifies cache/state use; recheck Redis version and operational semantics. |
| RabbitMQ | RabbitMQ 4.4, [Reliability Guide](https://www.rabbitmq.com/docs/reliability), overview | Delivery/failure concepts; local experiments do not imply broker operation in production. |
| Kafka | Apache Kafka official [documentation index](https://kafka.apache.org/documentation/) with Design, Operations and Security guides | Verify the release-specific official page and exact section before assigning; the index is not a lecture or an instruction to adopt Kafka. |
| AWS operations | AWS, [AWS Well-Architected Framework](https://docs.aws.amazon.com/wellarchitected/latest/framework/welcome.html), Introduction | Offline review only until deployment is separately authorized; no account, resource, credential or cost is implied. |
| AI evaluation | OpenAI API, [Working with evals](https://developers.openai.com/api/docs/guides/evals) | Evaluation methodology reference; deterministic independent expected results remain the local oracle and no provider call is required. |
| API contract | OpenAPI Specification v3.1.1, [Paths Object](https://spec.openapis.org/oas/v3.1.1.html#paths-object) and Path Item Object | Describe a single existing API and check consumer compatibility; do not treat generated documentation as a contract test. |
| OIDC validation | Spring Security 7.1.2, [OAuth 2.0 Resource Server JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html), `Specifying the Authorization Server`, `Runtime Expectations`, `Supplying Audiences` | Validate actual JWTs through Spring Security; recheck dependency-compatible version at study time. |
| OIDC test IdP | Keycloak 26.8.0, [Server Administration Guide](https://www.keycloak.org/docs/latest/server_admin/index.html), `Configuring realm keys`, `Rotating keys`; [Securing applications and services with OpenID Connect](https://www.keycloak.org/securing-apps/oidc-layers), `Certificate endpoint` | Local disposable issuer/JWKS and key-rotation rehearsal only. No production identity setup. |
| Schema evolution | PostgreSQL 18, [ALTER TABLE](https://www.postgresql.org/docs/18/sql-altertable.html), [UPDATE](https://www.postgresql.org/docs/18/sql-update.html); Flyway, [Working with data](https://documentation.red-gate.com/flyway/database-development-using-flyway/working-with-data) | Synthetic expand/backfill/restart/forward-recovery rehearsal; no real customer migration. |
| AI governance | NIST, [AI RMF Core](https://airc.nist.gov/airmf-resources/airmf/) and [Playbook: Measure](https://airc.nist.gov/airmf-resources/playbook/measure/); OpenAI, [Data controls in the OpenAI platform](https://developers.openai.com/api/docs/guides/your-data) | Build synthetic inventory, retention/deletion and evaluation evidence; no provider calls or compliance claims. |
| IaC | Terraform CLI v1.17.x, [`terraform validate`](https://developer.hashicorp.com/terraform/cli/commands/validate) and [`terraform plan`](https://developer.hashicorp.com/terraform/cli/commands/plan) | Offline validation and plan review; the plan does not validate live services or authorize deployment. |
| Enterprise rollout | AWS Well-Architected Security Pillar, [SEC01-BP03 Identify and validate control objectives](https://docs.aws.amazon.com/wellarchitected/latest/security-pillar/sec_securely_operate_control_objectives.html); [OPS07-BP02 Operational Readiness Reviews](https://docs.aws.amazon.com/wellarchitected/latest/framework/ops_ready_to_support_const_orr.html) | Simulate named owner decisions and a deny/defer launch path; no actual procurement/security approval is claimed. |

These source families support the outcome map; they do not silently add courses, certification requirements, credentials, AWS deployment, a model provider or a broker.

## Dated, evidence-gated application checkpoints

| Calendar study day / date | Checkpoint | Evidence boundary |
|---|---|---|
| **175 · 18 Apr 2027** | Backend / Full-Stack SDE II | Learner-owned backend/API/data implementation, tests, debugging and safe local operation. This is a review target, not automatic qualification. |
| **459 · 27 Jan 2028** | Senior Backend | Unfamiliar distributed diagnosis, repeated integrated recovery evidence and truthful handoff. |
| **550 · 27 Apr 2028** | Applied AI | Python/ML grounding, independent evaluation, uncertainty, security and measured limits. |
| **648 · 3 Aug 2028** | Senior AI | Evidence across AI, security, deployment readiness, observability and sustained operations; local work alone does not establish production ownership. |
| **718 · 12 Oct 2028** | Senior FDE | Real discovery, enterprise integration, stakeholder delivery and adoption/outcome evidence. |
| **774 · 7 Dec 2028** | Staff/FDE/Architect portfolio checkpoint | Portfolio review only; employer-specific titles and no automatic readiness claim. Architect is not universally above Staff. |
| **26 Oct 2029** | Professional Staff-responsibility review | Requires externally verified sustained ownership, technical influence, adoption and cross-team impact. |

Applications may begin earlier based on existing experience and evidence; the calendar is not a prerequisite to applying. These checkpoints do not guarantee expertise, title, hiring, or readiness.

## Progressive application checkpoints

- **SDE II knowledge:** learner-owned Java/Spring and SQL implementation, tests, debugging and safe local operation. Portfolio may support role comparison; it does not establish workplace delivery history.
- **Senior/SDE III capability:** repeated integrated slices, independent diagnosis, permission/reliability behavior, measured operation and truthful handover. Local rehearsal does not establish production on-call or ownership.
- **Staff/Principal or FDE direction:** unfamiliar interview questions test independent reasoning only in the separate question bank. Capstone architecture remains assistant-selected and explained. Professional-level technical direction, discovery, cross-team influence, mentoring, adoption and sustained outcomes need real work evidence and reviewers. Do not turn capstone tasks into design proposals or post-hoc justifications.
- **AWS targets:** AWS Certified Solutions Architect – Associate and AWS Certified Developer – Associate remain subordinate, current-version-checked study targets. No booking, new mandatory certification, pass, or readiness guarantee follows from the schedule.

The role postings inspected 24 October 2026 illustrate (not define) the difference: [Anthropic Staff Forward Deployed Engineer](https://job-boards.greenhouse.io/anthropic/jobs/5449229008) describes independent discovery, technical strategy and production experience; [OpenAI Partner AI Deployment Engineer – AWS, India](https://jobs.ashbyhq.com/openai/87989505-81e5-4513-bba0-5f00bb064216) is not Staff-titled and lists 8+ years technical consulting or equivalent. Recheck current postings before applications. No expertise, title or hiring outcome is guaranteed.

## Safety and evidence boundaries

No cloud deployment, provider/API call, credential grant, customer/workplace data access, paid service, exam booking or external spend is authorized here. Local/deterministic synthetic exercises are offline rehearsal and cannot satisfy live-operation, customer outcome or professional-ownership gates. Future authorized evidence must distinguish the 40-hex source commit from the 64-hex artifact SHA-256 and record actual environment, reviewer, scope, timestamps, operational interval, rollback, incidents and limitations. Do not fabricate records. Dynamic gates must permit future genuine evidence; current missing records are not permanent failure.

## Detailed assignment and Story exports

[Calendar study days 1–305](../calendar/story-export-1-305.json) export the 284 existing detailed units and the 21 executable inserted units in chronological order. The stable crosswalk is authoritative for dates and sprint membership; existing EAI issue IDs are mapped only where documented, new IDs remain unassigned, and board writes are disabled. [Engineering units 271–284](../continuation-271-284/README.md) retain their IDs and acceptance scope at calendar study days 292–305. The later 21 inserted units remain outcome-based and are not pre-ticketed. The [Days 197–270 inventory](../continuation-197-270-inventory.json) preserves learner history and evidence.
