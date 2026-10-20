# CoverStory continuation: Days 31–44

This is an isolated 14-day continuation proposal based on main SHA `65dc57f88fc844291cab3b80484a01c206e42916`. It does not modify the first-30-day canonical manifest or the board.

- [Canonical daily manifest](manifest.json): authoritative planned scope and independent DSA/engineering status.
- [Scrum owner export](scrum-owner-manifest.json): one consolidated story per day, for owner review; it has not been written to YouTrack or any project board.
- [DSA Top 150 map](dsa/top-150-map.md), [actual-attempt log](dsa/actual-attempt-log.json), and [revision log template](dsa/revision-log-template.md).
- Cross-batch queue command: `python3 scripts/dsa-review-queue.py --on-date YYYY-MM-DD`; behavior checks: `python3 -m unittest scripts/test_dsa_review_queue.py`.
- [Day contracts](contracts/).

Days 31–42 fill Sprint 3's remaining 12 dates after existing Days 29–30. Days 43–44 are the first 2 of 14 Sprint 4 dates; the other 12 dates remain unpopulated. Sprint membership follows the existing calendar, so this 14-day batch spans two sprints.

The feature is a synthetic HomeSecure policy servicing lifecycle. It is not a real insurance offer or production binding flow. No payments, PII, legal coverage promise, Kafka/outbox, StreamForge, or AWS setup is in scope. New scaffolds and acceptance tests are deliberately learner-owned; TODOs remain unresolved.

Each day is 180 minutes: 60 Java DSA + 120 engineering (85 implementation/testing, 25 concept/course, 10 evidence). The AWS Developer Associate mapping is for concept comparison only. D+3/D+7/D+21 are always anchored to recorded actual first attempts, including carryover from Days 1–30. Failed reviews replace new work inside the same 60-minute DSA allocation.
