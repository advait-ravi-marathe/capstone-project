# Self-review rubrics

These are practice aids authored for this sprint, not Amazon's hiring rubrics. Score only what you can explain from your own work. Do not infer readiness from a high score or from green tests.

## System and component design (5 points, manual)

Give 0 or 1 for each item, with one sentence of evidence:

1. **Requirements:** states primary user goals, constraints, and an explicit out-of-scope item before choosing components.
2. **Boundaries and data:** identifies owners, API/data contracts, and one important invariant.
3. **Failure and safety:** covers a realistic fault, recovery behavior, and privacy/security boundary.
4. **Tradeoff:** compares at least two viable options and names the cost of the chosen one.
5. **Verification and operations:** proposes a meaningful test plus an observable success/failure signal.

Use the 15-minute design segment to make a compact diagram or interface sketch; reserve the last two minutes to state the highest-risk assumption.

## STAR / Leadership Principle reflection (4 points, manual)

Give 0 or 1 for each item. A truthful example can score well without impressive numbers:

1. **Situation and task:** enough context to understand the problem and your responsibility.
2. **Individual action:** specific decisions and work you personally did, including collaboration where relevant.
3. **Evidence:** a verifiable outcome or an honest qualitative result; never invent metrics.
4. **Learning:** what you would repeat or change and how it connects to the named principle.

Use a real experience. If you lack a close example, say so and discuss a genuine adjacent experience; do not fabricate a story to fill the timebox.

## Coding review (not a score for completion)

After the 35-minute work block, record correctness evidence, edge cases, complexity, and one change you would make with another 15 minutes. Automated tests are acceptance contracts; passing them alone does not demonstrate independent mastery.
