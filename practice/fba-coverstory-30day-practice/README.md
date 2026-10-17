# FBA + CoverStory 30-day practice

Isolated learning branch `fba-coverstory-30day-practice`. Daily sessions run **17 October–15 November 2026**. The FBA role is Front-End Engineer II — FBA (job 10575321). The second assignment each day is a 90-minute CoverStory practice slot (`CS90-01`–`CS90-30`; these are local labels, not live board ticket IDs), for 180 planned minutes total: FBA 90 minutes plus CoverStory 90 minutes. CoverStory allocates 55 minutes to repository implementation/tests, 30 to the already selected official AWS pathway, and 5 to evidence/design. Each pairing is 180 minutes/day. Days 1–28 map the first 14 original S01 primary assignments plus six linked component cards; Days 29–30 are CAP-630/CAP-631 S01 integration/failure-recovery and acceptance-review implementation sessions. The original saved roadmap resumes on 16 November at source unit 15 / CAP-58.

All FBA exercises, fixtures, mapping rows, and the React test workspace were copied from the published source commit recorded in [`fba/SPRINT-MANIFEST.json`](fba/SPRINT-MANIFEST.json). The saved source is the FBA role only; the Seller Springboard role is excluded. Each paired CoverStory task links to the unchanged saved source card and its existing code/test harness. No learner TODO is marked done.

The appended 14-day mastery sprint maps to native board issues CAP-632–CAP-645 for 18 June–1 July 2028; issue links are listed in [`appended-mastery-native-id-map.json`](appended-mastery-native-id-map.json). Contract-specific starter seams, synthetic fixtures, acceptance tests and guides are in [`mastery-days/`](mastery-days/) and [`IMPLEMENTATION-PATH-MAP.json`](IMPLEMENTATION-PATH-MAP.json). Assessed business behavior remains TODO; these scaffolds are not solutions or learner completion. See [`COVERAGE.md`](COVERAGE.md) for verification and limits.

## Start

Requires Node/npm for FBA and the project JDK/Python prerequisites for CoverStory. From repository root:

```sh
python3 practice/fba-coverstory-30day-practice/fba/scripts/validate_course.py
(cd practice/fba-coverstory-30day-practice/fba/frontend && npm ci)
(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)
(cd practice/fba-coverstory-30day-practice/fba/frontend && npm run build)
```

Day 1 commands, from repository root:

```sh
(cd practice/fba-coverstory-30day-practice/fba/frontend && DAY=1 npm run test:contracts)
(cd practice/fba-coverstory-30day-practice/fba/frontend && npm test)
./mvnw -Dtest=CAP40ScaffoldInfrastructureTest test
./mvnw -Plearner-tests -Dtest=CAP40AcceptanceTest test
python3 -m unittest ai.tests.test_coverstory_cap41_infrastructure
python3 -m unittest ai.tests.test_coverstory_cap41_acceptance
```

The instructor/setup checks and learner acceptance commands are separate. Learner commands are expected to expose the preserved TODO before implementation; keep their actual outcomes distinct. Each daily FBA 90-minute block ends with a task-specific component test plus adversarial fixture/check. The exact FBA task commands and verified CoverStory learner seams are in [`days/`](days/), while the FBA source task text is in [`fba/assignments/frontend.md`](fba/assignments/frontend.md).

## Safety and evidence

Use only checked-in synthetic fixtures. No AWS account, credential, resource, payment, insurer action, real consumer/health information, or paid service is created. If AWS pathway access is unavailable, document that blocker without claiming course progress. Label AI-assisted work. Instructor setup passing is not proof of learner mastery. See the [coverage report](COVERAGE.md) for tested scaffolds and limits. The [Nov 16 transition plan](transition-2026-11-16.md) stages a source-backed continuation and S01 coverage gate without editing the main board.
