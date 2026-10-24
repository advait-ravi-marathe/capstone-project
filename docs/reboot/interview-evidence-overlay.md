# Interview evidence overlay (checked 20 October 2026)

This is a curriculum overlay, not evidence that the learner passed a contract or is interview-ready. It supports immediate interview readiness through repeated learner-first proposals, small runnable experiments, and timed practice. It does not guarantee questions or establish SDE II readiness. Sessions use existing engineering time; they do not add hours or replace contract acceptance.

## Official expectations

- [Amazon SDE II preparation](https://www.amazon.jobs/content/en/how-we-hire/sde-ii-interview-prep), accessed 20 Oct 2026: lifecycle ownership covers design, testing, operation, security and documentation; coding emphasizes executable, robust, tested code; system design evaluates practical trade-offs, reliability and scale; behavioral discussion asks for actual decisions and evidence. Team/role dependent.
- [Microsoft technical interviewing](https://careers.microsoft.com/v2/global/en/hiring-tips/technical-interviewing.html), general guidance accessed 20 Oct 2026, not a level-specific promise: clarify ambiguity and plan; demonstrate design, real-language coding and testing.
- [Google-published engineer interview advice](https://blog.google/company-news/inside-google/life-at-google/google-engineer-shares-her-technical-interview-tips/), 17 Mar 2022, is advice, not a current format guarantee: explain reasoning aloud, prepare algorithms and show thought process; hints are acceptable.

## Public first-person reports (anecdotal, not independently verified employer records)

Article bodies were reviewed. Dates below are report dates or interview dates as stated by authors. Anonymous authorship, selection bias, recollection error and self-assessed feedback limit confidence. Reported outcomes/ratings are not treated as facts; the mapping only identifies useful practice themes. This purposive eight-report sample is not a statistical survey.

Repeated themes mean only that related topics occur in multiple reports here. They do not establish prevalence. Security/concurrency prompts also reflect this synthetic application’s risks; they are curriculum-design inferences, not claims every company asks those exact questions. No interview solution or resume claim is adopted as evidence of hiring causation.

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
