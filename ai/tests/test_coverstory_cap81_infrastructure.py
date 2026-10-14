"""Independent CAP-81 fixture/oracle checks; never calls the learner report."""
from __future__ import annotations

import copy
import json
import unittest
from pathlib import Path

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-81/synthetic-v1.json"
RECORD_FAMILIES = ("domain", "audit", "projection", "step")


def fixture_data():
    return json.loads(FIXTURE.read_text(encoding="utf-8"))


def snapshot_for(case_id: str) -> tuple[dict, dict]:
    root = fixture_data()
    case = next(item for item in root["cases"] if item["id"] == case_id)
    snapshot = copy.deepcopy(root["base_snapshot"])
    mutation = case["mutations"]
    remove_audits = set(mutation.get("remove_audit_transition_ids", ()))
    snapshot["audit_records"] = [r for r in snapshot["audit_records"] if r["transition_id"] not in remove_audits]
    snapshot["audit_records"].extend(mutation.get("foreign_audits", ()))
    remove_steps = set(mutation.get("remove_step_transition_ids", ()))
    snapshot["journey_step_events"] = [r for r in snapshot["journey_step_events"] if r["transition_id"] not in remove_steps]
    snapshot["analytics_transition_ids"] = mutation.get("analytics_transition_ids", snapshot["analytics_transition_ids"])
    snapshot["protected_payload_references"].extend(mutation.get("extra_payload_references", ()))
    snapshot["history_sequences"] = mutation.get("history_sequences", snapshot["history_sequences"])
    snapshot["run_metadata"]["commit_sha"] = mutation.get("commit_sha", snapshot["run_metadata"]["commit_sha"])
    snapshot["run_metadata"]["command"] = mutation.get("command", snapshot["run_metadata"]["command"])
    return case, snapshot


def reference_report(snapshot: dict) -> dict:
    """Small independent oracle. It returns violation codes/IDs, never payloads."""
    owner = snapshot["owner_id"]
    violations: set[tuple[str, str]] = set()
    transitions = snapshot["transitions"]
    audit_rows = snapshot["audit_records"]
    steps = snapshot["journey_step_events"]
    projection = snapshot["journey"]

    meta = snapshot["run_metadata"]
    verified = bool(meta.get("commit_sha") and meta.get("command"))
    if not verified:
        violations.add(("UNVERIFIED_SNAPSHOT", "run-metadata"))

    if projection is None or projection.get("owner_id") != owner:
        violations.add(("OWNER_MISMATCH", "journey-projection"))

    for transition in transitions:
        tid = transition["transition_id"]
        candidates = [row for row in audit_rows if row.get("transition_id") == tid]
        owned = [row for row in candidates if row.get("owner_id") == owner]
        if candidates and not owned:
            violations.add(("OWNER_MISMATCH", tid))
        exact = [row for row in owned if all(row.get(field) == transition.get(field) for field in
                 ("entity_type", "entity_id", "entity_version", "journey_version", "linked_effect_id"))]
        if not exact:
            violations.add(("MISSING_AUDIT", tid))

        step_candidates = [row for row in steps if row.get("transition_id") == tid]
        owned_steps = [row for row in step_candidates if row.get("owner_id") == owner]
        valid_step = [row for row in owned_steps if row.get("journey_id") == transition.get("journey_id")
                      and row.get("journey_version") == transition.get("journey_version")]
        if not valid_step:
            violations.add(("MISSING_STEP_EVENT", tid))

    if projection is not None:
        current_revision = any(t["owner_id"] == owner and t["entity_type"] == "APPLICATION_REVISION"
                               and t["entity_id"] == projection.get("application_id")
                               and t["entity_version"] == projection.get("application_revision")
                               for t in transitions)
        if not current_revision:
            violations.add(("REVISION_MISMATCH", projection.get("journey_id", "journey")))
        envelope_refs = [p for p in snapshot["protected_payload_references"]
                         if p.get("owner_id") == owner
                         and p.get("application_id") == projection.get("application_id")
                         and p.get("application_revision") == projection.get("application_revision")
                         and p.get("envelope_id") == projection.get("protected_envelope_id")]
        if not envelope_refs:
            violations.add(("MISSING_PROTECTED_REFERENCE", projection.get("journey_id", "journey")))

    fingerprints: dict[str, list[dict]] = {}
    for payload_ref in snapshot["protected_payload_references"]:
        if payload_ref.get("owner_id") != owner:
            continue
        fingerprints.setdefault(payload_ref["fingerprint_sha256"], []).append(payload_ref)
    for refs in fingerprints.values():
        if len(refs) > 1 and any(r.get("plaintext_present") or r.get("storage_location") == "journey_projection" for r in refs):
            violations.add(("DUPLICATE_SENSITIVE_STATE", refs[-1]["reference_id"]))

    sequences = sorted(snapshot["history_sequences"])
    if len(sequences) != len(set(sequences)) or (sequences and sequences != list(range(sequences[0], sequences[-1] + 1))):
        violations.add(("NONCONTIGUOUS_HISTORY", projection.get("journey_id", "journey") if projection else "journey"))

    for probe in snapshot["rollback_probes"]:
        if set(probe["before"]) != set(RECORD_FAMILIES) or set(probe["after"]) != set(RECORD_FAMILIES):
            violations.add(("ROLLBACK_MISMATCH", probe["boundary"]))
        elif probe["before"] != probe["after"]:
            violations.add(("ROLLBACK_MISMATCH", probe["boundary"]))

    for operation in {row["operation_id"] for row in snapshot["payment_observations"] if row["owner_id"] == owner}:
        rows = sorted((r for r in snapshot["payment_observations"] if r["owner_id"] == owner
                       and r["operation_id"] == operation), key=lambda r: r["sequence"])
        states = [row["state"] for row in rows]
        expected_prefixes = [["FAILED"], ["FAILED", "UNKNOWN"], ["FAILED", "UNKNOWN", "SUCCEEDED"]]
        if states not in expected_prefixes or [r["sequence"] for r in rows] != list(range(1, len(rows) + 1)):
            violations.add(("PAYMENT_HISTORY_GAP", operation))
        if rows and rows[-1]["state"] == "SUCCEEDED" and not rows[-1].get("reconciled"):
            violations.add(("UNRECONCILED_SUCCESS", operation))
        if rows and rows[-1]["state"] == "SUCCEEDED" and not rows[-1].get("linked_effect_id"):
            violations.add(("MISSING_EFFECT_LINK", operation))

    effects = [row for row in snapshot["payment_effects"] if row["owner_id"] == owner and row["posted"]]
    linked_success_ids = {row.get("linked_effect_id") for row in snapshot["payment_observations"]
                          if row["owner_id"] == owner and row["state"] == "SUCCEEDED" and row.get("reconciled")}
    linked_effects = [row for row in effects if row["effect_id"] in linked_success_ids]
    by_operation: dict[str, set[str]] = {}
    for row in effects:
        by_operation.setdefault(row["operation_id"], set()).add(row["effect_id"])
    for operation, ids in by_operation.items():
        if len(ids) > 1:
            violations.add(("MULTIPLE_EFFECTS_PER_OPERATION", operation))
    effect_ids = {row["effect_id"] for row in linked_effects}
    posted_total = sum(row["amount_minor"] for row in linked_effects if row["effect_id"] in effect_ids)

    # Analytics is intentionally never read as proof of audit or step completeness.
    status = "UNVERIFIED" if any(code == "UNVERIFIED_SNAPSHOT" for code, _ in violations) else (
        "FAIL" if violations else "PASS")
    return {
        "status": status,
        "violations": tuple(sorted(violations)),
        "counts": {
            "transitions": len(transitions),
            "audits_matched": sum(1 for t in transitions if any(
                a.get("transition_id") == t["transition_id"] and a.get("owner_id") == owner for a in audit_rows)),
            "step_events_matched": sum(1 for t in transitions if any(
                s.get("transition_id") == t["transition_id"] and s.get("owner_id") == owner for s in steps)),
            "rollback_probes_checked": len(snapshot["rollback_probes"]),
            "money_effect_count": len(effect_ids),
            "posted_total_minor": posted_total,
        },
    }


class CAP81InfrastructureTest(unittest.TestCase):
    def test_fixture_is_synthetic_and_only_identifiers_hashes_and_minor_units_are_present(self):
        root = fixture_data()
        self.assertEqual("SYNTHETIC_READ_MODEL_ONLY", root["classification"])
        self.assertFalse(root["actual_database_or_crypto_access"])
        self.assertIn("never echo payload values", root["content_policy"])
        self.assertEqual(6, len(root["cases"]))
        self.assertNotIn("draft_text", json.dumps(root))

    def test_reference_accepts_pass_and_all_expected_failure_classes(self):
        for case_id in (f"CAP-81-CS-{i:02d}" for i in range(1, 7)):
            with self.subTest(case_id=case_id):
                case, snapshot = snapshot_for(case_id)
                result = reference_report(snapshot)
                codes = {code for code, _ in result["violations"]}
                self.assertEqual(case["expected_status"], result["status"])
                self.assertEqual(set(case["expected_codes"]), codes)
                if "expected_effect_count" in case:
                    self.assertEqual(case["expected_effect_count"], result["counts"]["money_effect_count"])
                    self.assertEqual(case["expected_posted_total_minor"], result["counts"]["posted_total_minor"])
                if "expected_rollback_probe_count" in case:
                    self.assertEqual(case["expected_rollback_probe_count"], result["counts"]["rollback_probes_checked"])

    def test_independent_defect_witnesses_catch_version_gap_rollback_overwrite_and_duplicate_effect(self):
        _, snapshot = snapshot_for("CAP-81-CS-01")
        wrong_version = copy.deepcopy(snapshot)
        wrong_version["audit_records"][0]["entity_version"] = 2
        self.assertIn("MISSING_AUDIT", {code for code, _ in reference_report(wrong_version)["violations"]})

        rollback_gap = copy.deepcopy(snapshot)
        rollback_gap["rollback_probes"][2]["after"]["projection"] = ["journey-A-v6"]
        self.assertIn("ROLLBACK_MISMATCH", {code for code, _ in reference_report(rollback_gap)["violations"]})

        overwritten = copy.deepcopy(snapshot)
        overwritten["payment_observations"] = overwritten["payment_observations"][1:]
        self.assertIn("PAYMENT_HISTORY_GAP", {code for code, _ in reference_report(overwritten)["violations"]})

        double_effect = copy.deepcopy(snapshot)
        double_effect["payment_effects"].append({**double_effect["payment_effects"][0], "effect_id": "effect-payment-A-duplicate"})
        double_effect["payment_observations"][-1]["linked_effect_id"] = "effect-payment-A-duplicate"
        self.assertIn("MULTIPLE_EFFECTS_PER_OPERATION", {code for code, _ in reference_report(double_effect)["violations"]})

    def test_foreign_rows_never_satisfy_owner_a_and_report_does_not_echo_fingerprint(self):
        case, snapshot = snapshot_for("CAP-81-CS-05")
        result = reference_report(snapshot)
        codes = {code for code, _ in result["violations"]}
        self.assertIn("OWNER_MISMATCH", codes)
        self.assertIn("MISSING_AUDIT", codes)
        self.assertIn("DUPLICATE_SENSITIVE_STATE", codes)
        rendered = json.dumps(result)
        self.assertNotIn("sha256:1111111111111111111111111111111111111111111111111111111111111111", rendered)


if __name__ == "__main__":
    unittest.main()
