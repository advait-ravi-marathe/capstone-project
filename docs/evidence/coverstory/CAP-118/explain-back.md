# CAP-118 explain-back prompt

**Expected learner response, not assessed:** For the four fixed synthetic queries, the precomputed candidate has Recall@3 `0.75` and MRR about `0.4583`, compared with lexical `0.5` and `0.4375`. Keep the lexical baseline because a four-query offline example is too small to support adoption. The unsafe candidate can have its ranking metrics reported, but it cannot be promoted: `br1` is outside the authorized source set. Authorization is a veto independent of relevance quality. No model was called and no index was built.

Learner response, exact commit/environment, and correction: **NOT RECORDED**. Instructor tests do not establish independent learner mastery.
