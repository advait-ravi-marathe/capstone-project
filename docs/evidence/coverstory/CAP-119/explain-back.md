# CAP-119 explain-back

Status: instructor prompt prepared; learner response NOT_RUN.

If the candidate store ignores its owner filter and returns BETA_CANARY, the result-scope check must reject the unsafe result before any text reaches model context or response serialization. The check uses authenticated owner/case authority and verifies every row. The caller-provided owner field cannot broaden scope.

The accompanying fixed oracle is an in-memory instructor reference. It does not prove that a production Support endpoint, persistence adapter, or model connection enforces this boundary.
