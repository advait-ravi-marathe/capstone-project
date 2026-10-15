# CAP-122 bounded reranking delta

**Status:** PROPOSED / NOT_RUN as a learner architecture artifact. Maximum six named nodes; links and learner changes remain blank.

```text
[AuthorizedCandidates] -- blank --> [RerankerPort] -- blank --> [KnownIdValidator]
          |                                              |
          +-- blank --> [FallbackRanking] <-- blank -- [FakeClock]
                                      |
                                    blank
                                      v
                               [MetricReport]
```

Facts to annotate, at most three:

1. Reranking is bounded by the fixture’s exact 100ms deadline.
2. Candidate IDs returned must be a subset of the evidence already authorized.
3. A timeout retains the original authorized order and does not issue another request.

Alternative to compare: retry until reranking succeeds or fetch unknown returned IDs. Failure evidence is CAP-122-CS-2 and CS-3. Interview prompt: a result finishes at 101ms and includes `z9`; why can neither an apparent metric gain nor late completion make it safe to use?
