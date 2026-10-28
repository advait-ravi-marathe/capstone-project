"""Independent structural checks for real P1 evidence artifacts.

These validators check artifact structure and require the declared real-run
evidence fields. A caller-supplied label or synthetic fixture cannot prove that
inference actually occurred; tests use synthetic records only to exercise the
validators. They do not decide semantic answer correctness, which requires a
separate documented human review of the cited synthetic source text.
"""
from __future__ import annotations

import hashlib
import json
import math
from collections.abc import Mapping, Sequence
from pathlib import Path
from urllib.parse import urlsplit

from .models import MODEL_ID, MODEL_REVISION


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for block in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def require_loopback_url(url: str) -> str:
    """Reject external targets; the closeout clients may call loopback only."""
    parsed = urlsplit(url)
    if (parsed.scheme not in {"http", "https"} or parsed.hostname not in
            {"127.0.0.1", "localhost", "::1"} or parsed.username or parsed.password
            or parsed.query or parsed.fragment):
        raise ValueError("only credential-free loopback API URLs are allowed")
    if parsed.path not in {"", "/"}:
        raise ValueError("provide the API origin only; route paths are selected by the tool")
    return url.rstrip("/")


def validate_benchmark_record(record: Mapping[str, object], expected_count: int = 12) -> None:
    if not isinstance(record, Mapping) or record.get("schema_version") != 1:
        raise ValueError("benchmark record schema is invalid")
    if record.get("evidence_kind") != "actual_pinned_local_inference":
        raise ValueError("synthetic or reference output cannot satisfy the real benchmark")
    if record.get("model_id") != MODEL_ID or record.get("model_revision") != MODEL_REVISION:
        raise ValueError("benchmark must identify the pinned local generation model")
    if record.get("concurrency") != 1 or record.get("sample_count") != expected_count:
        raise ValueError("benchmark sample count/concurrency differs from the bounded contract")
    samples = record.get("samples")
    if not isinstance(samples, list) or len(samples) != expected_count:
        raise ValueError("one result is required for every actual benchmark request")
    ids: set[int] = set()
    for sample in samples:
        if not isinstance(sample, Mapping) or set(sample) != {
            "sequence", "status_code", "client_elapsed_ms", "retrieval_ms", "generation_ms",
            "answer_sha256", "abstained",
        }:
            raise ValueError("benchmark sample fields differ from the contract")
        sequence = sample["sequence"]
        if type(sequence) is not int or not 1 <= sequence <= expected_count or sequence in ids:
            raise ValueError("benchmark sequence numbers must be unique and complete")
        ids.add(sequence)
        if type(sample["status_code"]) is not int or sample["status_code"] != 200 \
                or type(sample["abstained"]) is not bool:
            raise ValueError("only successful real API outcomes belong in a measured sample set")
        for key in ("client_elapsed_ms", "retrieval_ms", "generation_ms"):
            value = sample[key]
            if type(value) not in (int, float) or not math.isfinite(value) or value < 0:
                raise ValueError("latency values must be observed finite nonnegative numbers")
        answer_digest = sample["answer_sha256"]
        if (not isinstance(answer_digest, str) or len(answer_digest) != 64
                or any(char not in "0123456789abcdef" for char in answer_digest.lower())):
            raise ValueError("store a digest of each actual answer, not its text")
    if ids != set(range(1, expected_count + 1)):
        raise ValueError("benchmark sample sequence is incomplete")
    if not isinstance(record.get("machine"), Mapping) or not isinstance(record.get("resource_limits"), str):
        raise ValueError("machine limits and explicit resource-measurement limits are required")
    if record.get("cost") != "unavailable: local inference has no provider billing meter":
        raise ValueError("cost must be reported as unavailable for this local runtime")


def load_heldout_cases(path: Path) -> list[dict[str, object]]:
    rows: list[dict[str, object]] = []
    for line in path.read_text(encoding="utf-8").splitlines():
        if line.strip():
            row = json.loads(line)
            if not isinstance(row, dict):
                raise ValueError("held-out cases must be JSON objects")
            rows.append(row)
    expected = {"answerable": 6, "unanswerable": 2, "adversarial": 2}
    counts = {category: sum(row.get("category") == category for row in rows) for category in expected}
    if len(rows) != 10 or counts != expected:
        raise ValueError("held-out source must contain exactly 6/2/2 answerable/unanswerable/adversarial cases")
    ids = [row.get("case_id") for row in rows]
    if any(not isinstance(value, str) or not value for value in ids) or len(set(ids)) != 10:
        raise ValueError("held-out case IDs must be unique nonempty strings")
    return rows


def validate_heldout_run(run: Mapping[str, object], cases: Sequence[Mapping[str, object]],
                         expected_fixture_sha256: str) -> None:
    if run.get("schema_version") != 1 or run.get("evidence_kind") != "actual_pinned_local_rag_run":
        raise ValueError("held-out artifact is not identified as an actual local RAG run")
    if run.get("model_id") != MODEL_ID or run.get("model_revision") != MODEL_REVISION:
        raise ValueError("held-out artifact must identify the pinned real generation model")
    if (not isinstance(expected_fixture_sha256, str) or len(expected_fixture_sha256) != 64
            or run.get("fixture_sha256") != expected_fixture_sha256):
        raise ValueError("held-out fixture digest is required")
    config_digest = run.get("configuration_sha256")
    if (not isinstance(config_digest, str) or len(config_digest) != 64
            or any(char not in "0123456789abcdef" for char in config_digest.lower())):
        raise ValueError("a frozen configuration digest is required")
    results = run.get("results")
    if not isinstance(results, list) or len(results) != 10:
        raise ValueError("held-out run must record exactly ten outcomes")
    for source, result in zip(cases, results, strict=True):
        if not isinstance(result, Mapping) or result.get("case_id") != source.get("case_id"):
            raise ValueError("held-out results must preserve canonical source order and IDs")
        if result.get("status_code") != 200:
            raise ValueError("a blocked/failed model request is not an evaluation pass")
        if (not isinstance(result.get("answer"), str) or not result["answer"].strip()
                or not isinstance(result.get("citations"), list)
                or type(result.get("abstained")) is not bool):
            raise ValueError("raw actual answer and citations are required for review")
        if result.get("model_id") != MODEL_ID or result.get("model_revision") != MODEL_REVISION:
            raise ValueError("every outcome must be from the pinned model")
        citations = result["citations"]
        sources = result.get("sources")
        if (any(not isinstance(value, str) or not value for value in citations)
                or not isinstance(sources, list) or any(not isinstance(item, Mapping) for item in sources)):
            raise ValueError("citation/source evidence fields are malformed")
        source_ids = [item.get("chunk_id") for item in sources if isinstance(item, Mapping)]
        if set(citations) - set(source_ids):
            raise ValueError("every emitted citation must resolve to returned source evidence")
    if run.get("contains_manual_correctness_judgment") is not False:
        raise ValueError("raw runner must not grade its own answer correctness")


def validate_heldout_review(review: Mapping[str, object], run: Mapping[str, object],
                            cases: Sequence[Mapping[str, object]]) -> dict[str, int]:
    """Check review completeness and counts; semantic judgments remain human-owned."""
    if review.get("schema_version") != 1 or review.get("review_type") != "independent_source_grounding_review":
        raise ValueError("a separately labeled grounding review is required")
    if (not isinstance(review.get("reviewer"), str) or not review["reviewer"].strip()
            or not isinstance(review.get("reviewed_at_utc"), str) or not review["reviewed_at_utc"].strip()):
        raise ValueError("reviewer identity and review timestamp are required")
    if review.get("run_sha256") != hashlib.sha256(
            json.dumps(run, sort_keys=True, separators=(",", ":")).encode()).hexdigest():
        raise ValueError("review must bind to the exact raw run bytes/structure")
    judgments = review.get("judgments")
    if not isinstance(judgments, list) or len(judgments) != 10:
        raise ValueError("review must cover all ten frozen cases exactly once")
    counts = {"answerable_correct_supported": 0, "unanswerable_not_fabricated": 0,
              "adversarial_boundary_clear": 0}
    for source, judgment in zip(cases, judgments, strict=True):
        if not isinstance(judgment, Mapping) or judgment.get("case_id") != source.get("case_id"):
            raise ValueError("review order/IDs must match the frozen held-out source")
        category = source["category"]
        if category == "answerable":
            if type(judgment.get("correct_and_supported")) is not bool:
                raise ValueError("answerable outcomes require a human correctness/support judgment")
            counts["answerable_correct_supported"] += judgment["correct_and_supported"]
        elif category == "unanswerable":
            if type(judgment.get("fabricated")) is not bool:
                raise ValueError("unanswerable outcomes require a human fabrication judgment")
            counts["unanswerable_not_fabricated"] += not judgment["fabricated"]
        else:
            if type(judgment.get("boundary_violation")) is not bool:
                raise ValueError("adversarial outcomes require an explicit boundary judgment")
            counts["adversarial_boundary_clear"] += not judgment["boundary_violation"]
    return counts
