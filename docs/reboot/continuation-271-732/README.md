# Outcome roadmap extension: Days 271–732

**Approved planning extension; not learner completion.** This adds 462 calendar days (33 complete 14-day sprints) after the published Day 270 closeout. The original 270 assignments, IDs, source records, learner statuses and evidence are preserved; their computed study-day dates shift two days later. The authoritative calendar starts 26 October 2026: Day 271 is 23 July 2027 and Day 732 is 26 October 2028. The existing October 2029 North Star remains a separate professional-ownership review horizon, not another year of fixed daily assignments.

## Fixed capacity

| Window | Days | Engineering | One shared block | Total |
|---|---:|---:|---:|---:|
| Added extension | 462 | 76,230 min / **1,270.5 h** | 20,790 min / **346.5 h** | **1,617 h** |
| Per full sprint | 14 | 2,310 min / 38.5 h | 630 min / 10.5 h | 49 h |
| Reserve inside engineering | 20% | 15,246 min / **254.1 h** | — | included above |
| Planned deep work after reserve | — | 60,984 min / **1,016.4 h** | — | included above |

Every day remains 165 engineering minutes plus exactly one shared 45-minute block. The reserve is inside those minutes, not overtime. At each sprint review, first use it for the next named unmet prerequisite, bounded recovery, integration, sustained observation, or reassessment. If none applies, perform that sprint's preassigned hardening/transfer task. Current absent evidence is not a reason to convert every day into remediation. The separate DSA track and its time are unchanged.

## Outcome phases and realistic envelopes

CoverStory remains the primary flagship; StreamForge remains gated by actual prior learner evidence. Phases are overlapping emphases, not isolated prerequisites or one-technology checklists. Envelopes include the same 20% reserve and shared block.

| Days / planned dates | Envelope | Emphasis and measurable exit evidence |
|---|---:|---|
| **271–354 · 23 Jul–14 Oct 2027** | 84 d · 231 engineering h · 63 shared h; 46.2 engineering h reserved | Java/Spring runtime behavior, JVM/OS/concurrency, profiling/JFR, PostgreSQL internals and measured query behavior. Build repeatable workload/plan/latency baselines with exact result correctness; explain estimate-versus-observation differences; diagnose unfamiliar contention or query regressions. Start deployment readiness early. Begin Python, probability/statistics, linear algebra and optimization before model work. Exit: independently reproduce a diagnosis on an unfamiliar workload; no preferred plan shape or timing threshold. **Only Days 271–284 are ticket-ready.** |
| **355–438 · 15 Oct 2027–6 Jan 2028** | 84 d · 231 engineering h · 63 shared h; 46.2 engineering h reserved | Distributed consistency, transaction boundaries, idempotency, event delivery, failure/recovery, capacity and observability. Compare Redis, Kafka and RabbitMQ against explicit workload and operational-cost cases; local experiments may reject unjustified infrastructure. Continue deployment/security/readiness and Python/math. Exit: deterministic failure-injection evidence, repeated recovery/restore cycles, documented consistency/capacity limits, and reviewed use/no-use decisions. |
| **439–522 · 7 Jan–30 Mar 2028** | 84 d · 231 engineering h · 63 shared h; 46.2 engineering h reserved | Python/ML grounding; one justified live-provider RAG or bounded-agent path only after offline baselines, authorization and safety controls. Add retrieval, abstention, independent labels, uncertainty slices, prompt-injection cases, timeout/fallback, latency and cost accounting. Establish measured retrieval/evaluation baselines first. No model makes consequential underwriting/claims decisions. Exit: independent evaluation, counterfactual/negative controls and calibrated limitations; synthetic results remain simulations. |
| **523–606 · 31 Mar–22 Jun 2028** | 84 d · 231 engineering h · 63 shared h; 46.2 engineering h reserved | AWS deployment/security/operations: least-privilege identity, secrets, network boundaries, CI/CD, telemetry, SLOs, incident response, rollback and cost controls. Cloud/model use, credential grants and external data require separate authorization. Rehearse offline first; authorized real deployment and sustained operations are separate gates. Exit: multiple release/restore cycles, observed operational intervals, unfamiliar incident diagnosis, independently checked security/rollback evidence. A public endpoint alone is not ownership. |
| **607–676 · 23 Jun–31 Aug 2028** | 70 d · 192.5 engineering h · 52.5 shared h; 38.5 engineering h reserved | Second materially distinct enterprise-integration opportunity, preferably an authorized real work opportunity. Keep CoverStory context and gated StreamForge; activate no StreamForge work without actual evidence gates. Without an authorized real opportunity, use a clearly labeled synthetic contract to test transfer, not customer delivery. Exit: changed identity/data contracts, compatibility/handoff tests, external review/use only if actually available, separately recorded adoption/outcome evidence. |
| **677–732 · 1 Sep–26 Oct 2028** | 56 d · 154 engineering h · 42 shared h; 30.8 engineering h reserved | Reusable platform capabilities across CoverStory and eligible StreamForge: extract repeated needs only; validate two-context reuse; measure deployment-time/reliability/cost/regression improvement; obtain independent review/use; retain support/upgrade boundaries. Close with evidence-based application checkpoints and recovery queue. Exit: measured reuse across both projects where gates permit. Portfolio reuse is not professional Staff/FDE ownership. |

Operations-readiness and math foundations start early while deep AI follows foundations and baseline evidence. Dates are plans; current evidence and authorization gates remain dynamic. Unmet work moves within the envelope.

## Verified source families for future sprint planning

These are primary references checked on 24 October 2026, not preassigned daily lessons. Future contracts must recheck the live title, current version and exact heading on the assignment date. Keep the existing confirmed AWS and in28Minutes course providers where an exact available lesson is verified; when a suitable lesson cannot be verified, assign the official documentation directly. Do not invent lecture names or durations.

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

These source families support the outcome map; they do not silently add courses, certification requirements, credentials, AWS deployment, a model provider or a broker.

## Progressive application checkpoints

- **SDE II knowledge:** learner-owned Java/Spring and SQL implementation, tests, debugging and safe local operation. Portfolio may support role comparison; it does not establish workplace delivery history.
- **Senior/SDE III capability:** repeated integrated slices, independent diagnosis, permission/reliability behavior, measured operation and truthful handover. Local rehearsal does not establish production on-call or ownership.
- **Staff/Principal or FDE direction:** unfamiliar interview questions test independent reasoning only in the separate question bank. Capstone architecture remains assistant-selected and explained. Professional-level technical direction, discovery, cross-team influence, mentoring, adoption and sustained outcomes need real work evidence and reviewers. Do not turn capstone tasks into design proposals or post-hoc justifications.
- **AWS targets:** AWS Certified Solutions Architect – Associate and AWS Certified Developer – Associate remain subordinate, current-version-checked study targets. No booking, new mandatory certification, pass, or readiness guarantee follows from the schedule.

The role postings inspected 24 October 2026 illustrate (not define) the difference: [Anthropic Staff Forward Deployed Engineer](https://job-boards.greenhouse.io/anthropic/jobs/5449229008) describes independent discovery, technical strategy and production experience; [OpenAI Partner AI Deployment Engineer – AWS, India](https://jobs.ashbyhq.com/openai/87989505-81e5-4513-bba0-5f00bb064216) is not Staff-titled and lists 8+ years technical consulting or equivalent. Recheck current postings before applications. No expertise, title or hiring outcome is guaranteed.

## Safety and evidence boundaries

No cloud deployment, provider/API call, credential grant, customer/workplace data access, paid service, exam booking or external spend is authorized here. Local/deterministic synthetic exercises are offline rehearsal and cannot satisfy live-operation, customer outcome or professional-ownership gates. Future authorized evidence must distinguish the 40-hex source commit from the 64-hex artifact SHA-256 and record actual environment, reviewer, scope, timestamps, operational interval, rollback, incidents and limitations. Do not fabricate records. Dynamic gates must permit future genuine evidence; current missing records are not permanent failure.

## Next sprint

[Days 271–284](../continuation-271-284/README.md) is the only detailed next sprint, with daily source-backed tasks, starter seam, acceptance checks and shared-session subjects. Later daily Stories are intentionally not pre-created. The [Days 197–270 inventory](../continuation-197-270-inventory.json) remains unchanged.
