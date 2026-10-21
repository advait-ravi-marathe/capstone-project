# DSA guided-anchor and transfer policy

This is the prospective policy for the existing Days 1–154 plan. It records
study guidance, not learner attempts or completion. Use LeetCode Premium's
problem statements and editorials; no separate DSA course is introduced.
The policy takes effect 2026-10-22 in Asia/Kolkata; this date does not create
an attempt or change any existing attempt date.

## First encounters and evidence

The initial guided sequence is LC121 on study Day 1, LC1 on Day 2, and LC383
on Day 3. LC1's historical canonical anchor day remains Day 3, and LC383's
remains Day 5; moving a planned encounter never changes an existing history
identity. Day 1's assisted-practice narrative and zero independent-solve
credit are preserved. Editorial-assisted learning is guided study, not an
independent pass.

Attempt a transfer before opening its editorial or targeted hint. A passed
transfer requires a genuine unaided attempt and an explanation of the relevant
invariant; platform acceptance alone does not establish transfer. An
independent transfer does not become a new canonical anchor and does not start
another D+3/D+7/D+21 clock. The earliest actual first attempt for the canonical
anchor remains the only clock origin. Publication, planned dates, guided
reading, and policy changes do not create or reset attempts.

Legacy assistance remains unknown unless the record proves otherwise. Retain
historical passed-review evidence as historical evidence without relabeling it
as an independent transfer. Never infer attempts from an empty or partial log.

## Actual-attempt review and scheduling

Review checkpoints remain anchored to actual local calendar dates at D+3,
D+7, and D+21. A later failure remains visible even if an earlier interval
passed. Future and early events do not satisfy a later checkpoint. New event
records are append-only; corrections reference the original event rather than
rewriting it. A transfer target is checked against prior assignment, attempt,
and editorial exposure before it can be called fresh.

Count substantive scheduled exercises, not brief recall or multiple log events
for one exercise. Allocate DSA time by the selected workload:

| Substantive exercises | DSA time | Engineering time | Planned total |
|---:|---:|---:|---:|
| 1–2 | 60 min | 120 min | 180 min |
| 3 | 75 min | 120 min | 195 min |
| 4 | 90 min | 120 min | 210 min |

Engineering remains 25 minutes study, 85 minutes implementation/testing, and
10 minutes evidence. Do not add a problem to fill a tier. Do not schedule more
than four substantive exercises. Failed-retrieval repair and overdue reviews
take priority and displace new guidance. If capacity is exceeded, carry the
unserved item visibly with its original due date; never erase D+21 or reset a
clock. Brief original-anchor recall is useful evidence of recall but does not
close a missing independent-transfer checkpoint.

Recommended bounded sessions are 60 minutes for one guided exercise, or one
guided plus one transfer; 75 minutes for one guided plus two transfers; and
90 minutes for one guided plus three transfers. Retrieval-only sessions use
the same tiers. Incomplete attempts remain incomplete and are repaired later.

## Guidance cutoff and planning limits

New guided pattern introductions end after roadmap Day 150. Days 151–154 are
revision, transfer of already learned patterns, and repair only. Unintroduced
anchors remain incomplete; a fresh question must not smuggle in an unlearned
algorithm as a transfer.

The delivered repository currently contains 31 canonical anchor records. This
is an inventory, not a desired final curriculum limit. A hypothetical goal of
150 guided anchors would imply up to 600 first-cycle encounters (150 initial
encounters plus 450 review slots), not 600 unique problems, completed attempts,
or mastered patterns. If the 150th actual first attempt occurred on roadmap
Day 150, its D+21 date would be Day 171 only under consecutive on-time dates.
These are conditional counts, not learner history or a completion promise.

The verified transfer seed catalog contains limited coverage and explicit
gaps. If no verified, fresh, prerequisite-ready candidate exists, report
`validated_transfer_needed` with its reason and keep that evidence gap visible.
Do not invent mappings to fill a matrix. Candidate mappings are pedagogical
inferences, not official LeetCode classifications. Avoid equivalent families
and reserve each selected target once per session.

The queue can display suggestions, but it cannot inspect the learner's
LeetCode history, Premium access, or prerequisite readiness. After checking
those personally, a due transfer may be supplied as
`--confirmed-transfer lc:121+3=2016`. This is recorded as learner-asserted
freshness/readiness, not independently verified fact. The tool rejects a
candidate already present in the local event history, an equivalent exposed
target, a duplicate target, a non-catalog/non-full-transfer candidate, or a
non-due slot. An unconfirmed slot or catalog gap stays due with its original
date while other eligible due anchors may use capacity; only one retrieval per
anchor and at most four substantive exercises are selected.

For a failed retrieval, use a repair only on the same canonical anchor, review
slot and exact failed target. When the queue reports that target, an explicit
selection looks like `--confirmed-repair lc:121+3=2016`. Record it as a v2
`retrieval_attempt` with `activity: "repair"`, a unique attempt/evidence
reference, and `repairs_event_id` naming the earlier failed event. An unrelated
target, empty repair or repair without evidence is rejected. Only an evidenced
unaided passing repair clears the failed-repair warning; repair is not fresh
transfer credit, so the independent-transfer checkpoint remains due.

When the catalog marks a fresh candidate as a prerequisite extension or partial
component, it can be scheduled explicitly with
`--confirmed-extension lc:121+7=1014` after checking history, access and
prerequisites. The queue labels this as a qualified extension/component
exercise. It is not a full same-invariant transfer, never earns independent
transfer credit, and leaves the original checkpoint due. A relation already
classified as a full transfer belongs on `--confirmed-transfer` instead.

After Day 171, any future engineering/interview-question time policy is outside
this Days 1–154 correction. It does not remove pending DSA reviews. No Days
155+ material is created here.
