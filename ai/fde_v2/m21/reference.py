"""Instructor-only M2.1 reference mechanics.

These functions support deterministic worked examples and mutation tests. They
do not produce real model results, prove human labels, or clear the P1 gate.
"""
from __future__ import annotations

import hashlib
import json
import math
from collections import Counter, defaultdict
from pathlib import Path
from statistics import median
from typing import Any


def _nonempty(value: Any) -> bool:
    return isinstance(value, str) and bool(value.strip())


def validate_case(record):
    if not isinstance(record, dict):
        raise ValueError("case must be an object")
    required = {"case_id", "scenario_family", "source_document_id", "source_version",
                "source_sha256", "question", "answerability", "expected_atomic_facts",
                "abstention_reason", "evidence_spans", "tags", "origin", "author",
                "reviewer", "annotation_status", "split"}
    if required - set(record):
        raise ValueError("case is missing required provenance/label fields")
    for key in ("case_id", "scenario_family", "source_document_id", "question", "author", "reviewer"):
        if not _nonempty(record[key]):
            raise ValueError(f"{key} must be nonempty")
    if type(record["source_version"]) is not int or record["source_version"] < 1:
        raise ValueError("source_version must be a positive integer")
    digest = record["source_sha256"]
    if not isinstance(digest, str) or len(digest) != 64 or any(c not in "0123456789abcdef" for c in digest.lower()):
        raise ValueError("source_sha256 must be a SHA-256 hex digest")
    if record["answerability"] not in {"answerable", "unanswerable"}:
        raise ValueError("answerability must be answerable or unanswerable")
    if not isinstance(record["expected_atomic_facts"], list) or not isinstance(record["evidence_spans"], list):
        raise ValueError("expected facts and evidence spans must be arrays")
    if record["answerability"] == "answerable":
        if not record["expected_atomic_facts"] or not record["evidence_spans"] or not all(
                _nonempty(v) for v in record["expected_atomic_facts"]):
            raise ValueError("answerable cases need facts and source evidence spans")
        if record["abstention_reason"] is not None:
            raise ValueError("answerable cases cannot carry an abstention reason")
    elif not _nonempty(record["abstention_reason"]):
        raise ValueError("unanswerable cases need a reason")
    if (not isinstance(record["tags"], list) or not record["tags"]
            or any(not _nonempty(tag) for tag in record["tags"])):
        raise ValueError("tags must be nonempty strings")
    if record["origin"] not in {"public", "synthetic"}:
        raise ValueError("only public or synthetic sources are permitted")
    if record["annotation_status"] != "source_verified":
        raise ValueError("draft/unverified labels are not release-eligible")
    if record["split"] not in {"development", "heldout"}:
        raise ValueError("split must be development or heldout")
    return dict(record)


def assign_group_splits(records, split_manifest):
    if not isinstance(split_manifest, dict) or set(split_manifest) != {"development", "heldout"}:
        raise ValueError("frozen split manifest needs development and heldout family lists")
    development, heldout = split_manifest["development"], split_manifest["heldout"]
    if (not isinstance(development, list) or not isinstance(heldout, list)
            or len(development) != len(set(development)) or len(heldout) != len(set(heldout))
            or set(development) & set(heldout)):
        raise ValueError("family lists must be unique and disjoint; do not resample")
    expected = {family: "development" for family in development}
    expected.update({family: "heldout" for family in heldout})
    families = {row.get("scenario_family") for row in records if isinstance(row, dict)}
    if families != set(expected):
        raise ValueError("split manifest must account for every record family exactly")
    assigned = {}
    for row in records:
        case_id = row.get("case_id")
        family = row.get("scenario_family")
        if not _nonempty(case_id) or case_id in assigned:
            raise ValueError("case IDs must be unique and nonempty")
        assigned[case_id] = expected[family]
    return assigned


def audit_dataset(records, split_manifest):
    if not isinstance(records, (list, tuple)) or len(records) != 100:
        raise ValueError("release draft targets exactly 100 cases")
    validated = [validate_case(row) for row in records]
    ids = [row["case_id"] for row in validated]
    if len(ids) != len(set(ids)):
        raise ValueError("case IDs must be unique")
    families = defaultdict(list)
    for row in validated:
        families[row["scenario_family"]].append(row)
    if len(families) != 20 or any(len(rows) != 5 for rows in families.values()):
        raise ValueError("the frozen draft is 20 scenario families x 5 cases")
    assignment = assign_group_splits(validated, split_manifest)
    if any(len({assignment[row["case_id"]] for row in rows}) != 1 for rows in families.values()):
        raise ValueError("scenario-family leakage across development/heldout")
    if any(row["split"] != assignment[row["case_id"]] for row in validated):
        raise ValueError("each case split label must match the frozen family manifest")
    counts = Counter(assignment.values())
    if counts != Counter({"development": 60, "heldout": 40}):
        raise ValueError("split must contain 60 development and 40 heldout cases")
    heldout = [row for row in validated if assignment[row["case_id"]] == "heldout"]
    answer_counts = Counter(row["answerability"] for row in heldout)
    if answer_counts != Counter({"answerable": 24, "unanswerable": 16}):
        raise ValueError("heldout draft must contain 24 answerable and 16 unanswerable cases")
    adversarial = sum("adversarial" in row["tags"] for row in validated)
    if adversarial < 10:
        raise ValueError("at least ten cases must carry the adversarial tag")
    return {"case_count": 100, "family_count": 20, "cases_per_family": 5,
            "development_count": 60, "heldout_count": 40,
            "heldout_answerable": 24, "heldout_unanswerable": 16,
            "adversarial_tag_count": adversarial,
            "release_eligible": True,
            "note": "structural audit only; source truth requires independent human verification"}


def plan_version_update(old_chunks, new_chunks):
    old = {row.chunk_id: row for row in old_chunks}
    new = {row.chunk_id: row for row in new_chunks}
    if len(old) != len(old_chunks) or len(new) != len(new_chunks):
        raise ValueError("chunk IDs must be unique within each version")
    if new and len({(row.document_id, row.version) for row in new.values()}) != 1:
        raise ValueError("new chunks must belong to one document/version")
    return {"document_id": next(iter(new.values())).document_id if new else None,
            "new_version": next(iter(new.values())).version if new else None,
            "add_chunk_ids": sorted(set(new) - set(old)),
            "replace_chunk_ids": sorted(key for key in set(old) & set(new) if old[key] != new[key]),
            "remove_chunk_ids": sorted(set(old) - set(new)),
            "preserve_active_until_commit": True}


def stage_version_update(ledger, document, chunks, vectors, vector_store):
    """Stage, verify, switch the SQL head, then retire old vectors."""
    if len(chunks) != len(vectors) or not chunks:
        raise ValueError("one vector per nonempty staged chunk set is required")
    old_version = ledger.active_version(document.document_id)
    ledger.stage(document, chunks)
    vector_store.upsert(chunks, vectors)
    prior = ledger.activate(document.document_id, document.version, vector_store)
    if prior is not None and prior != document.version:
        try:
            vector_store.delete_document_version(document.document_id, prior)
            cleanup_pending = False
        except Exception:
            # New head is already authoritative. Stale points remain invisible
            # because every retrieval result is filtered against the ledger.
            cleanup_pending = True
    else:
        cleanup_pending = False
    return {"status": "activated", "old_version": old_version,
            "active_version": document.version, "cleanup_pending": cleanup_pending}


def delete_document(document_id, ledger, vector_store):
    previous = ledger.delete(document_id)
    try:
        vector_store.delete_document(document_id)
        return {"status": "deleted", "previous_version": previous, "physical_cleanup_pending": False}
    except Exception:
        return {"status": "tombstoned", "previous_version": previous, "physical_cleanup_pending": True}


def filter_current_candidates(hits, ledger):
    return [hit for hit in hits if ledger.is_current(hit.chunk.document_id, hit.chunk.version)]


def reconcile_versions(ledger, vector_store):
    expected = set()
    active_keys = set()
    for row in ledger.active_rows():
        key = (row["document_id"], row["version"])
        active_keys.add(key)
        expected.update(chunk.chunk_id for chunk in ledger.version_chunks(*key))
    points = vector_store.list_document_versions()
    actual = {row["chunk_id"] for row in points}
    by_document_version = {(row["document_id"], row["version"]) for row in points}
    return {"missing_active_chunk_ids": sorted(expected - actual),
            "stale_vector_chunk_ids": sorted(actual - expected),
            "active_versions_missing_vectors": sorted(
                ["{}:v{}".format(doc_id, version) for doc_id, version in active_keys
                 if (doc_id, version) not in by_document_version]),
            "active_versions_present": len(active_keys),
            "vector_point_count": len(points)}


def score_retrieval(relevant_ids, ranked_ids, k):
    if type(k) is not int or k < 1:
        raise ValueError("k must be a positive integer")
    relevant = set(relevant_ids)
    if any(not _nonempty(item) for item in relevant):
        raise ValueError("relevant IDs must be nonempty strings")
    if not relevant:
        return {"included_in_answerable_denominator": False, "recall_at_k": None, "reciprocal_rank": None}
    if any(not _nonempty(item) for item in ranked_ids):
        raise ValueError("ranked IDs must be nonempty strings")
    unique_ranked = []
    seen = set()
    for item in ranked_ids:
        if item not in seen:
            unique_ranked.append(item)
            seen.add(item)
    top = unique_ranked[:k]
    first_rank = next((index for index, item in enumerate(unique_ranked, 1) if item in relevant), None)
    return {"included_in_answerable_denominator": True,
            "recall_at_k": len(set(top) & relevant) / len(relevant),
            "reciprocal_rank": 0.0 if first_rank is None else 1.0 / first_rank,
            "first_relevant_rank": first_rank}


def summarize_latency(samples, timeout_seconds):
    if not isinstance(samples, (list, tuple)) or not samples:
        raise ValueError("all-case samples are required")
    if type(timeout_seconds) not in (int, float) or not math.isfinite(timeout_seconds) or timeout_seconds <= 0:
        raise ValueError("timeout_seconds must be finite and positive")
    durations = []
    failures = 0
    for row in samples:
        if not isinstance(row, dict) or type(row.get("ok")) is not bool:
            raise ValueError("each sample needs an explicit boolean outcome")
        if not row["ok"]:
            failures += 1
            durations.append(float(timeout_seconds))
            continue
        value = row.get("elapsed_seconds")
        if type(value) not in (int, float) or not math.isfinite(value) or value < 0:
            raise ValueError("successful sample must have a finite nonnegative time")
        durations.append(float(value))
    ordered = sorted(durations)
    rank = math.ceil(0.95 * len(ordered))
    return {"sample_count": len(samples), "failure_count": failures,
            "median_seconds": median(ordered), "p95_nearest_rank_seconds": ordered[rank - 1],
            "p95_method": "nearest-rank; failures receive the full declared timeout penalty"}


def score_workflow(case, result, human_judgment):
    if case.get("case_id") != result.get("case_id") or case.get("case_id") != human_judgment.get("case_id"):
        raise ValueError("case, result and judgment IDs must match exactly")
    if case.get("answerability") not in {"answerable", "unanswerable"}:
        raise ValueError("case answerability label is invalid")
    if human_judgment.get("reviewer") in (None, "") or human_judgment.get("source_reviewed") is not True:
        raise ValueError("semantic support must come from a recorded human source review")
    if result.get("status") != "completed":
        return {"case_id": case["case_id"], "success": False, "failure": result.get("failure", "incomplete")}
    citations = result.get("citation_chunk_ids")
    if not isinstance(citations, list) or len(citations) != len(set(citations)):
        raise ValueError("citation IDs must be unique and explicit")
    if human_judgment.get("severe_error") is True:
        return {"case_id": case["case_id"], "success": False, "severe_error": True}
    if case["answerability"] == "answerable":
        success = human_judgment.get("all_required_facts_supported") is True and bool(citations)
    else:
        success = (result.get("abstained") is True
                   and human_judgment.get("fabricated_material_claim") is False)
    return {"case_id": case["case_id"], "success": bool(success), "severe_error": False}


def compare_runs(baseline, candidate):
    def index(rows):
        if not isinstance(rows, (list, tuple)):
            raise ValueError("run rows must be a sequence")
        result = {}
        for row in rows:
            if (not isinstance(row, dict) or not _nonempty(row.get("case_id"))
                    or type(row.get("success")) is not bool or row["case_id"] in result):
                raise ValueError("each run must have unique IDs and boolean outcomes")
            result[row["case_id"]] = row
        return result
    left, right = index(baseline), index(candidate)
    if not left or left.keys() != right.keys():
        raise ValueError("baseline and candidate must cover the exact same cases")
    gains = sorted(key for key in left if not left[key]["success"] and right[key]["success"])
    regressions = sorted(key for key in left if left[key]["success"] and not right[key]["success"])
    return {"case_count": len(left), "baseline_successes": sum(x["success"] for x in left.values()),
            "candidate_successes": sum(x["success"] for x in right.values()),
            "gains": gains, "regressions": regressions,
            "net_change": len(gains) - len(regressions)}


def freeze_release(bundle, output: Path):
    required = {"baseline_run_sha256", "candidate_config_sha256", "corpus_manifest_sha256",
                "case_manifest_sha256", "rubric_sha256", "criterion"}
    if not isinstance(bundle, dict) or required - set(bundle):
        raise ValueError("release freeze is missing a baseline/config/data/rubric/criterion digest")
    for key in required - {"criterion"}:
        value = bundle[key]
        if not isinstance(value, str) or len(value) != 64 or any(c not in "0123456789abcdef" for c in value.lower()):
            raise ValueError(f"{key} must be SHA-256 hex")
    encoded = json.dumps(bundle, sort_keys=True, separators=(",", ":"), ensure_ascii=False).encode()
    digest = hashlib.sha256(encoded).hexdigest()
    record = {"schema_version": 1, "evidence_kind": "proposed_frozen_evaluation_config",
              "bundle": bundle, "bundle_sha256": digest,
              "gate_status": "proposal_only_requires_real_p1_and_independent_review"}
    output.parent.mkdir(parents=True, exist_ok=True)
    with output.open("x", encoding="utf-8") as handle:
        json.dump(record, handle, indent=2, sort_keys=True)
        handle.write("\n")
    return digest


def _heldout_metrics(baseline_judgments, candidate_judgments, criterion, latency):
    """Pure threshold calculation; this does not validate evidence provenance."""
    baseline = compare_runs(baseline_judgments, candidate_judgments)
    expected = set(criterion["expected_case_ids"])
    answerable = set(criterion["answerable_case_ids"])
    unsupported = set(criterion["unanswerable_case_ids"])
    if len(expected) != 40 or len(answerable) != 24 or len(unsupported) != 16:
        raise ValueError("frozen case roster must be 40 = 24 answerable + 16 unanswerable")
    if answerable & unsupported or answerable | unsupported != expected:
        raise ValueError("answerability lists must partition the frozen case roster")
    if set(row["case_id"] for row in candidate_judgments) != expected:
        raise ValueError("judgments must cover the frozen roster exactly")
    by_id = {row["case_id"]: row for row in candidate_judgments}
    answerable_success = sum(by_id[case_id]["success"] for case_id in answerable)
    unsupported_success = sum(by_id[case_id]["success"] for case_id in unsupported)
    severe = sum(bool(row["severe_error"]) for rows in (baseline_judgments, candidate_judgments) for row in rows)
    passed = (
        answerable_success >= criterion["min_answerable_successes"]
        and unsupported_success >= criterion["min_unanswerable_successes"]
        and severe <= criterion["max_severe_errors"]
        and baseline["net_change"] >= criterion["min_net_gain"]
        and latency["p95_nearest_rank_seconds"] <= criterion["max_p95_seconds"]
    )
    return {"criterion_satisfied": passed, "answerable_successes": answerable_success,
            "unanswerable_successes": unsupported_success, "severe_error_count": severe,
            "net_change": baseline["net_change"], "latency_p95_seconds": latency["p95_nearest_rank_seconds"],
            "baseline_comparison": baseline}


def evaluate_release_gate(baseline, candidate, judgments, frozen_criterion, entry_gate):
    """Evidence gate: deterministic fixtures never yield a learner pass."""
    if entry_gate != "P1_ACTUAL_EVIDENCE_INDEPENDENTLY_CLEARED":
        return {"status": "BLOCKED", "m21_pass": False, "reason": "P1 prerequisite not independently cleared"}
    if not isinstance(frozen_criterion, dict) or frozen_criterion.get("frozen_before_heldout") is not True:
        return {"status": "INVALID", "m21_pass": False, "reason": "predeclared frozen criterion is required"}
    if frozen_criterion.get("heldout_key_separated") is not True:
        return {"status": "INVALID", "m21_pass": False, "reason": "held-out key separation is not evidenced"}
    required = {"expected_case_ids", "answerable_case_ids", "unanswerable_case_ids",
                "min_answerable_successes", "min_unanswerable_successes", "max_severe_errors",
                "min_net_gain", "max_p95_seconds", "timeout_seconds"}
    if required - set(frozen_criterion):
        return {"status": "INVALID", "m21_pass": False, "reason": "frozen criterion is incomplete"}
    try:
        expected_ids = set(frozen_criterion["expected_case_ids"])
        for name, rows, evidence_kind in (("baseline", baseline, "actual_p1_keyword_baseline"),
                                          ("candidate", candidate, "actual_pinned_local_rag_run")):
            if not isinstance(rows, list) or len(rows) != 40:
                return {"status": "INVALID", "m21_pass": False, "reason": f"complete {name} run is required"}
            ids = [row.get("case_id") for row in rows if isinstance(row, dict)]
            if len(ids) != 40 or len(set(ids)) != 40 or set(ids) != expected_ids:
                return {"status": "INVALID", "m21_pass": False, "reason": f"{name} must cover the frozen case roster exactly"}
    except (KeyError, TypeError) as exc:
        return {"status": "INVALID", "m21_pass": False, "reason": f"invalid frozen case roster: {exc}"}
    if (not isinstance(judgments, dict) or set(judgments) != {"baseline", "candidate"}
            or not all(isinstance(judgments[key], list) for key in judgments)):
        return {"status": "INVALID", "m21_pass": False, "reason": "separate baseline and candidate source judgments are required"}
    expected = set(frozen_criterion["expected_case_ids"])
    for name in ("baseline", "candidate"):
        rows = judgments[name]
        if len(rows) != 40 or {row.get("case_id") for row in rows if isinstance(row, dict)} != expected:
            return {"status": "INVALID", "m21_pass": False, "reason": f"complete {name} judgments are required"}
        if any(row.get("source_reviewed") is not True or not _nonempty(row.get("reviewer"))
               or type(row.get("success")) is not bool or type(row.get("severe_error")) is not bool for row in rows):
            return {"status": "INVALID", "m21_pass": False, "reason": f"reviewed {name} outcomes are malformed"}
    if (any(row.get("evidence_kind") != "actual_p1_keyword_baseline" for row in baseline)
            or any(row.get("evidence_kind") != "actual_pinned_local_rag_run" for row in candidate)):
        return {"status": "BLOCKED", "m21_pass": False, "reason": "complete actual baseline and pinned model runs are required"}
    try:
        if any(type(row.get("ok")) is not bool for row in (*baseline, *candidate)):
            raise ValueError("each run row needs an explicit success/failure execution status")
        judgments_by_system = {name: {row["case_id"]: row for row in judgments[name]}
                               for name in ("baseline", "candidate")}
        for name, run_rows in (("baseline", baseline), ("candidate", candidate)):
            if any(not row["ok"] and judgments_by_system[name][row["case_id"]]["success"]
                   for row in run_rows):
                raise ValueError(f"a failed {name} request cannot receive a successful task judgment")
        latency = summarize_latency(candidate, frozen_criterion["timeout_seconds"])
        metrics = _heldout_metrics(judgments["baseline"], judgments["candidate"], frozen_criterion, latency)
    except (ValueError, TypeError, KeyError) as exc:
        return {"status": "INVALID", "m21_pass": False, "reason": str(exc)}
    if not metrics["criterion_satisfied"]:
        return {"status": "FAIL", "m21_pass": False, "metrics": metrics,
                "reason": "the frozen usefulness or quality criterion did not pass"}
    return {"status": "ELIGIBLE_FOR_INDEPENDENT_REVIEW", "m21_pass": False,
            "metrics": metrics, "comparison": metrics["baseline_comparison"],
            "reason": "independent evidence review remains required; this is not a learner pass"}
