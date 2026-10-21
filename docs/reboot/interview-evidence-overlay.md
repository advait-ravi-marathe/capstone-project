# Interview evidence overlay (checked 20 October 2026)

This is a curriculum overlay, not evidence that the learner passed a contract or is interview-ready. It adds explainable decisions and adversarial tests, not new technologies or hours. The sample helps practice working code, project depth, database reasoning, design trade-offs and explanations; it does not establish question frequency or guarantee questions. Thirty days builds foundations, not SDE II readiness.

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

## Time and DSA rules

Use the workload-tiered DSA policy: 1–2 substantive exercises = 60 minutes, 3 = 75, 4 = 90; keep engineering at 120 minutes (85 implementation/testing, 25 concept/course, 10 evidence). Use LeetCode Premium/editorials for guided first encounters; assisted work is not independent credit. D+3/D+7/D+21 follow the earliest actual first attempt, not planned dates. Failed/overdue retrieval displaces new guidance; overflow preserves the original due date and does not drop D+21. New guided introductions stop after Day150; Days151–154 are revision/transfer/repair only. See [shared DSA policy](dsa/guided-transfer-policy.md).

On ordinary days, the existing 10-minute evidence block covers a concise explanation, test command/result, and gap/trade-off. Choose one or two prompts, not a questionnaire. On Days 14, 28 and 30, a 20-minute mock/teach-back replaces implementation time within the 85-minute block: 65 build/test + 20 mock + 25 concept + 10 evidence = 120. If integration is blocked, explain the failing boundary and next diagnostic step; do not call it a passing demo. Day 1’s concept remains AWS DVA-C02 Domain 1 Task 1 Skill 1.1.6 API work.

## Rubric

For each dimension record 0, 1 or 2: **0** cannot demonstrate or incorrect; **1** demonstrates with hints or misses a boundary; **2** demonstrates independently with a counterexample and accurate explanation. Record assistance. This is a local learning rubric, not a company hiring score. Test success and understanding are independent. Record `contract passed` only with a real result and `explanation independent` only after unassisted demonstration. Do not average away authorization or atomicity defects. A rubric gap becomes bounded repair work, not automatic scope expansion.

1. **Contract:** restates inputs, outputs, ownership and invariant; asks about ambiguity before coding.
2. **Reasoning:** explains why the approach maintains the invariant, its complexity/cost and one rejected alternative.
3. **Implementation:** navigates and changes relevant Java/SQL/TypeScript without a generated answer; compiled code agrees with explanation.
4. **Verification:** predicts a negative result, runs the targeted test, interprets failure and checks affected behavior.
5. **Transfer:** handles one changed assumption, identifies first design pressure and avoids unsupported claims.

## Later work (not Days 1–30 scope)

Keep exactly two flagships: CoverStory first; StreamForge later. A future StreamForge recommendation product could compare a baseline, evaluate ranking, address cold start, and measure latency/cost. Later CoverStory agentic features could exercise bounded tools, authorization, human approval for consequential actions, evaluation data, recovery and audit. This interview sample does not validate an AI hiring plan. No AI implementation, third flagship, or additional infrastructure belongs in this slice.
