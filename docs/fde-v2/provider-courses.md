# Official provider courses as reading substitutions

This is a small overlay on the existing recommended 382-day route. It does not add sessions or alter the daily allocation: **60 minutes DSA + 30 minutes interview/AWS + 90 minutes AI**. The OpenAI Academy course time below replaces only overlapping AI concept-reading time in the named existing sessions. Keep each session's worked example, learner task, evidence log, and required implementation documentation. If a course takes longer than its estimate, stop at the allocated time and continue the existing offline exercise; do not borrow from another block or claim course completion.

The route still begins with the existing 270-day AI/FDE core and 112-day production-ML extension. This is our custom learning route, not an official OpenAI roadmap. OpenAI Academy courses support selected concepts; they do not cover all classical ML, statistics, PyTorch training, evaluation, or production-ML practice in this route. The linked primary technical documentation and hands-on learner tasks remain necessary. Course study is not a prerequisite substitute, practical-capability pass, or evidence of learner completion. Follow the stated milestone prerequisites before using a course slot.

## Course map

| Existing AI sessions | Course substitution | Planned time | Prerequisite and retained work |
|---|---|---:|---|
| Days 167–169, M4.1 | [OpenAI Academy: Scope AI Solutions](https://academy.openai.com/pages/courses) | 30 min catalog estimate + 20 min assessment allowance = 50 min | First complete the question-first discovery and baseline work. Keep the M4.1 data/access and pilot-scope exercises. |
| Days 230–235, M5.2 | [OpenAI Academy: Evaluate AI Applications](https://academy.openai.com/pages/courses) | 70 min catalog estimate + 20 min assessment allowance = 90 min | Complete the dataset/metric contract and split-preparation work first. Finish before development comparison or held-out scoring; keep the real offline evaluation tasks. |
| Days 43–48, M2.1 | [OpenAI Academy: Build with Retrieval-Augmented Generation](https://academy.openai.com/pages/courses) | 100 min catalog estimate + 20 min assessment allowance = 120 min | Start only after the actual P1 RAG/ownership gate is passed. Keep M2.1 versioning, evaluation, and local exercises. |
| Days 146–154, M3.3 | [OpenAI Academy: Design and Build Agentic Systems](https://academy.openai.com/pages/courses) | 110 min catalog estimate + 20 min assessment allowance = 130 min | M3.1 bounded workflow and M3.2 MCP/approval learner prerequisites remain. Finish before held-out evaluation; keep local safety, replay, denial, and release work. |
| Days 244, 245, 247, M5.2 | [OpenAI Academy: Optimize AI Application Performance](https://academy.openai.com/pages/courses) | 30 min catalog estimate + 20 min assessment allowance = 50 min | Use the local prediction/monitoring artifacts; keep actual measurements and limits. No production-performance claim follows from the course. |

The five catalog estimates total **340 minutes**. This overlay separately reserves a planning allowance of **20 minutes per course for its assessment**, for **440 minutes total**, spread across existing AI study slots. The assessment allowance is our schedule estimate, not an OpenAI-published duration. If a platform assessment or course takes longer, defer it; do not extend the day or drop the session's learner task. OpenAI says course badges require course completion and an assessment score of at least 80%. A certificate for the five-course API learning path requires all five courses and assessments; this map includes all five courses but does **not** claim its certificate or completion. OpenAI says Academy badges and course-completion certificates are **not professional certifications**. See [OpenAI Academy access and credentials](https://help.openai.com/en/articles/20001270-openai-academy-courses).

## Status and evidence

The starting status in [`provider-courses.json`](provider-courses.json) is `not_started` for every course and `not_verified` for every badge. Keep course completion and badge verification separate:

- `not_started`: no course completion evidence recorded;
- `completed`: the learner has completion evidence, but no badge has been checked;
- `badge_verified`: the actual platform badge and its requirements have been checked and recorded.

Do not infer progress from scheduled days or assistant-authored mappings. Do not mark a course or badge complete without actual learner evidence. These course states never change the separate practical milestone gates.

AWS SAA remains the selected AWS exam under its existing readiness and separate authorization gates. OpenAI's formal Certified app access is currently described as invite-only for Enterprise/Edu workspaces; access is unverified. No such access or credential is promised here. There is no course enrollment, API-key setup, provider call, paid lab, or exam purchase in this amendment. The OpenAI Academy help page says its courses are free and globally available to people with a ChatGPT account; this document does not enroll the learner or change account settings. The 382-day curriculum remains our custom roadmap using selected official OpenAI course material and AWS cloud study, not an official OpenAI curriculum. The remaining statistical and ML training work is hands-on practice with its primary references, not claimed Academy course content.
