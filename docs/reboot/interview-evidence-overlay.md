# Interview evidence overlay (checked 20 October 2026)

This is a curriculum overlay, not evidence that the learner passed a contract or is interview-ready. It supports immediate interview readiness through repeated learner-first proposals, small runnable experiments, and timed practice. It does not guarantee questions or establish SDE II readiness. Sessions use existing engineering time; they do not add hours or replace contract acceptance.

## Official expectations

- [Amazon SDE II preparation](https://www.amazon.jobs/content/en/how-we-hire/sde-ii-interview-prep), accessed 20 Oct 2026: lifecycle ownership covers design, testing, operation, security and documentation; coding emphasizes executable, robust, tested code; system design evaluates practical trade-offs, reliability and scale; behavioral discussion asks for actual decisions and evidence. Team/role dependent.
- [Microsoft technical interviewing](https://careers.microsoft.com/v2/global/en/hiring-tips/technical-interviewing.html), general guidance accessed 20 Oct 2026, not a level-specific promise: clarify ambiguity and plan; demonstrate design, real-language coding and testing.
- [Google Software Engineer III, Full Stack](https://www.google.com/about/careers/applications/jobs/results/97812573251871430-software-engineer-iii-full-stack), indexed official posting located 20 Oct 2026: lists backend language plus JavaScript/TypeScript and full-stack/API experience; preferred areas include DSA, debugging, performance and test engineering. Direct opening returned “Job not found,” so this is historical/indexed context, not a verified active vacancy. Titles/qualifications do not automatically transfer across companies or countries.
- [Google-published engineer interview advice](https://blog.google/company-news/inside-google/life-at-google/google-engineer-shares-her-technical-interview-tips/), 17 Mar 2022, is advice, not a current format guarantee: explain reasoning aloud, prepare algorithms and show thought process; hints are acceptable.

## Public first-person reports (anecdotal, not independently verified employer records)

Article bodies were reviewed. Dates below are report dates or interview dates as stated by authors. Anonymous authorship, selection bias, recollection error and self-assessed feedback limit confidence. Reported outcomes/ratings are not treated as facts; the mapping only identifies useful practice themes. This purposive eight-report sample is not a statistical survey.

- <a id="a1"></a>**A1 — Amazon SDE2**, post dated 21 May 2025; author reports 2.9 years’ experience and pending result: [full account](https://leetcode.com/discuss/post/6766714/amazon-sde2-interview-experience-chances-9bwx/). Reports graph/tree and search coding, logger/filter object design, architecture trade-offs, and behavioral ownership.
- <a id="a2"></a>**A2 — Google L4**, interview month April 2025 in title; author reports 4.5 years, Hyderabad/Pune: [full account](https://leetcode.com/discuss/post/6713084/google-l4-experience-for-phone-screen-an-6eyt/). Reports prefix comparison, graph reasoning, alternatives, ambiguity, boundary cases and explaining under time constraints. Not evidence that array-only preparation suffices.
- <a id="a3"></a>**A3 — Microsoft SDE-2 India**, December 2025 in title; post dated 23 Dec 2025: [full account](https://leetcode.com/discuss/post/7433909/). Reports sliding-window coding, expiring-state structure, extensible cache design, resume architecture, database/scaling trade-offs.
- <a id="a4"></a>**A4 — PayPay India SDE-2**, interview May 2025 in title; posted 9 Jun 2025; author reports 3 years: [full account](https://leetcode.com/discuss/post/6827319/). Reports Spring DI/component roles/transactions, HTTP errors, isolation/ACID, database choices and 3Sum.
- <a id="a5"></a>**A5 — Fortinet backend SDE**, title/body say Dec 2025; SDE 2 is a tag, not verified formal level; author reports about 3 years: [full account](https://leetcode.com/discuss/post/7532617/). Reports Spring API completion, SQL/ORM/index/schema deep dives, project failure/security, CSRF and design.
- <a id="a6"></a>**A6 — Swiggy Full Stack SDE-2**, posted 1 Sep 2025, updated 2 Sep; interview date unspecified; author reports 2.5 years: [full account](https://leetcode.com/discuss/post/7143750/). Reports React/backend practical task, frontend fundamentals/debugging and search-system design.
- <a id="a7"></a>**A7 — Syfe SDE 2 Java backend**, Jan 2026 interview in title, posted 19 Feb/updated 23 Feb 2026; author reports 3 years 9 months: [full account](https://leetcode.com/discuss/post/7592843/). Reports project follow-ups, DSA, stateful player object design with concurrent requests and diagnosis of a user-specific transaction failure despite healthy services.
- <a id="a8"></a>**A8 — HiLabs SDE 2 Java backend**, Aug 2025 interview in title; publication date unavailable in opened body; author reports 3 years 2 months: [full account](https://leetcode.com/discuss/post/7662240/hilabs-sde-2-interview-experience-aug-20-wqqx/). Reports Java/SQL theory, interval reasoning, dependency injection and project explanation.

Repeated themes mean only that related topics occur in multiple reports here. They do not establish prevalence. Security/concurrency prompts also reflect this synthetic application’s risks; they are curriculum-design inferences, not claims every company asks those exact questions. No interview solution or resume claim is adopted as evidence of hiring causation.

## Time, DSA, and mock rules

Keep engineering at **120 minutes**: 25 concept/course, 85 practice/test, and
10 evidence. Two 45-minute mock sessions per week use 45 minutes of the
85-minute practice block, leaving 40 minutes for that day's implementation
and tests. A third session is optional only when it replaces practice time and
does not erase an acceptance gate. Unfinished work carries forward with its
true status; do not add hours or call a partial slice complete. The mock is
5/5/15/10/10: clarify, record the learner's first proposal, deep practice,
inspect one failure and trade-off, then feedback/rubric. Save the proposal
before hints, reference answers, or AI. Any later AI comparison is recorded
separately.

For DSA planning, use the separate [DSA preparation repository](https://github.com/advait-ravi-marathe/dsa-prep/tree/859e161f6553e9aca8dc501fda152780cec66ef0). Its assignments, trackers, tests,
and factual attempt history are maintained there. The capstone will not
duplicate DSA code or a review queue. Preserve actual attempt dates and
assistance labels in that source of truth. The 60/75/90-minute DSA tiers
through Day 171 and the post-Day-171 165-minute engineering plus shared
45-minute block are scheduling constraints; the shared block is not an
additional DSA block.

## Practice rubric

Score each dimension 0–2 and record assistance. **0** means incorrect or
unobserved; **1** means partly demonstrated or dependent on a hint; **2** means
independently demonstrated with correct evidence. This is a learning rubric,
not an employer hiring score. Do not average away a security or data-integrity
failure.

1. **Problem framing:** useful clarifications and observable acceptance.
2. **System boundary:** ownership, trust, API, and transaction responsibilities.
3. **Correctness/data:** invariant and model withstand duplicates/concurrency.
4. **Failure/operations:** bounded failure response and a recovery/diagnostic.
5. **Verification/communication:** predicted negative case and interpreted
   runnable result.

Use the [mock format and scoring detail](interview/mock-rubric.md), the
[`36 original prompts`](interview/question-bank.md), and the
[`12-session practice plan`](interview/deep-practice.md). Routine daily
evidence records the real command, output, and blocker; it is not a generic
post-hoc design-psychology exercise. Keep the exact Day 1 API course bridge.

## Later work (not Days 1–30 scope)

Keep exactly two flagships: CoverStory first; StreamForge later. A future StreamForge recommendation product could compare a baseline, evaluate ranking, address cold start, and measure latency/cost. Later CoverStory agentic features could exercise bounded tools, authorization, human approval for consequential actions, evaluation data, recovery and audit. This interview sample does not validate an AI hiring plan. No AI implementation, third flagship, or additional infrastructure belongs in this slice.
