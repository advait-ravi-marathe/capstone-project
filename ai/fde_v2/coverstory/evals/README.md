# Synthetic retrieval evaluation fixtures

The two JSONL files are a starter fixture, not an independently reviewed benchmark. They use fictional CC0 training documents. Development has 20 cases (12 answerable, 4 unanswerable, 4 adversarial); held-out has 10 (6/2/2). IDs do not overlap, and the held-out cases must not guide chunk, prompt, model or threshold tuning.

Expected labels are source-side annotations, not values returned by a retriever. The evaluator scores only the answerable cases using the declared evidence IDs; adversarial and unanswerable cases are retained for separate safety/abstention review. A high retrieval score does not establish that generated claims are supported.

Run integrity check: `python3 -m unittest scripts.test_fde_v2_sprint2.Sprint2ManifestTests.test_eval_fixture_counts_and_isolation`.
