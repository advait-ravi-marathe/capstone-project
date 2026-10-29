"""Conditional M2.2 policy mechanics; fixtures do not prove identity or deployment."""
from __future__ import annotations

import json
import unittest

from ai.fde_v2.m22 import learner, reference


class M22ReferenceMechanics(unittest.TestCase):
    def setUp(self):
        self.context = {"subject_id": "alice", "tenant_id": "tenant-a"}
        self.docs = {
            "owned": {"tenant_id": "tenant-a", "owner_id": "alice", "reader_ids": [],
                      "writer_ids": [], "deleted": False},
            "shared": {"tenant_id": "tenant-a", "owner_id": "bob", "reader_ids": ["alice"],
                       "writer_ids": [], "deleted": False},
            "foreign": {"tenant_id": "tenant-b", "owner_id": "alice", "reader_ids": ["alice"],
                        "writer_ids": ["alice"], "deleted": False},
            "revoked": {"tenant_id": "tenant-a", "owner_id": "alice", "reader_ids": [],
                        "writer_ids": [], "deleted": True},
        }

    def test_draft_schedule_preserves_full_accounting_and_stage_minutes(self):
        from pathlib import Path
        schedule = json.loads((Path(__file__).parents[1] / "m22" / "draft_schedule.json").read_text())
        days = schedule["days"]
        self.assertEqual([row["day"] for row in days], list(range(62, 83)))
        self.assertTrue(all(row["dsa_minutes"] == 60 and row["role_bank_minutes"] == 30
                            and row["ai_minutes"] == 90 for row in days))
        self.assertTrue(all(row["m22_substantive_minutes"] + row["m22_buffer_minutes"]
                            + row["visibility_minutes"] + row["m21_boundary_buffer_minutes"]
                            + row["unallocated_minutes"] == 90 for row in days))
        self.assertEqual(sum(row["m22_substantive_minutes"] for row in days), 1500)
        self.assertEqual(sum(row["m22_buffer_minutes"] for row in days), 240)
        self.assertEqual(sum(row["visibility_minutes"] for row in days), 90)
        self.assertEqual(sum(row["m21_boundary_buffer_minutes"] for row in days), 30)
        self.assertEqual(sum(row["unallocated_minutes"] for row in days), 30)
        stages = {f"M2.2-S{i:02}": 0 for i in range(1, 11)}
        for row in days:
            for segment in row["segments"]:
                stages[segment["stage"]] += segment["minutes"]
        self.assertEqual(set(stages.values()), {150})
        self.assertEqual([row["day"] for row in days if row["role_bank_focus"] == "AWS"],
                         [62, 68, 69, 75, 76, 82])

    def test_identity_adapter_checks_claim_contract_after_injected_verified_provider(self):
        calls = []
        claims = {"sub": "alice", "iss": "https://issuer.invalid", "aud": ["coverstory"], "exp": 2000}

        def verifier(token):
            calls.append(token)
            return claims

        lookup = lambda subject: {"enabled": subject == "alice", "tenant_id": "tenant-a"}
        identity = reference.verify_identity("opaque-test-token", verifier, lookup,
                                             expected_issuer="https://issuer.invalid",
                                             expected_audience="coverstory", now=1000)
        self.assertEqual(identity, self.context)
        self.assertEqual(calls, ["opaque-test-token"])
        for broken in ({**claims, "iss": "other"}, {**claims, "aud": ["other"]},
                       {**claims, "exp": 1000}, {**claims, "sub": ""}):
            with self.subTest(broken=broken), self.assertRaises(ValueError):
                reference.verify_identity("token", lambda _token, c=broken: c, lookup,
                                          expected_issuer="https://issuer.invalid",
                                          expected_audience="coverstory", now=1000)
        with self.assertRaises(ValueError):
            reference.verify_identity("token", verifier, lambda _subject: None,
                                      expected_issuer="https://issuer.invalid",
                                      expected_audience="coverstory", now=1000)

    def test_authorization_matrix_defaults_to_deny(self):
        self.assertTrue(reference.authorize_document(self.context, self.docs["owned"], "read"))
        self.assertTrue(reference.authorize_document(self.context, self.docs["shared"], "read"))
        self.assertFalse(reference.authorize_document(self.context, self.docs["shared"], "write"))
        self.assertFalse(reference.authorize_document(self.context, self.docs["foreign"], "read"))
        self.assertFalse(reference.authorize_document(self.context, self.docs["revoked"], "read"))
        self.assertFalse(reference.authorize_document({"subject_id": "alice"}, self.docs["owned"], "read"))
        self.assertFalse(reference.authorize_document(self.context, self.docs["owned"], "export"))

    def test_oracles_reject_permissive_access_and_retrieval_mutants(self):
        def assert_access_contract(candidate):
            self.assertTrue(candidate(self.context, self.docs["owned"], "read"))
            self.assertFalse(candidate(self.context, self.docs["foreign"], "read"))
            self.assertFalse(candidate(self.context, self.docs["revoked"], "read"))

        assert_access_contract(reference.authorize_document)
        always_allow = lambda *_args: True
        with self.assertRaises(AssertionError):
            assert_access_contract(always_allow)

        hits = [{"document_id": key, "chunk_id": key + "-chunk"}
                for key in ("owned", "foreign", "revoked")]
        expected = ["owned"]
        self.assertEqual([row["document_id"] for row in
                          reference.filter_retrieved_hits(self.context, hits, self.docs)], expected)
        with self.assertRaises(AssertionError):
            self.assertEqual([row["document_id"] for row in hits], expected)

    def test_audit_oracle_detects_raw_event_logging_mutant(self):
        event = {"subject_pseudonym": "h:abc", "action": "ask", "outcome": "ok",
                 "trace_id": "t1", "timestamp": "fixture", "prompt": "private"}
        self.assertNotIn("prompt", reference.safe_audit_event(event))
        with self.assertRaises(AssertionError):
            self.assertNotIn("prompt", dict(event))

    def test_retrieval_boundary_revalidates_authoritative_acl_and_deduplicates(self):
        hits = [{"document_id": key, "chunk_id": f"{key}-1"} for key in ("owned", "foreign", "revoked", "missing", "owned")]
        got = reference.filter_retrieved_hits(self.context, hits, self.docs)
        self.assertEqual([row["document_id"] for row in got], ["owned"])
        # A forged tenant field on a vector hit cannot overrule the SQL authority map.
        forged = [{"document_id": "foreign", "tenant_id": "tenant-a", "chunk_id": "f-1"}]
        self.assertEqual(reference.filter_retrieved_hits(self.context, forged, self.docs), [])

    def test_audit_event_allowlist_omits_content_and_secret_fields(self):
        event = {"subject_pseudonym": "h:abc", "action": "ask", "outcome": "denied",
                 "trace_id": "trace-1", "timestamp": "2026-10-29T00:00:00Z",
                 "prompt": "private synthetic text", "authorization": "secret-canary"}
        safe = reference.safe_audit_event(event)
        self.assertEqual(set(safe), {"subject_pseudonym", "action", "outcome", "trace_id", "timestamp"})
        self.assertNotIn("secret-canary", repr(safe))
        with self.assertRaises(ValueError):
            reference.safe_audit_event({**event, "trace_id": " "})

    def test_deadline_and_retry_policy_are_bounded(self):
        self.assertEqual(reference.remaining_budget(12.5, 10), 2.5)
        self.assertEqual(reference.remaining_budget(10, 12), 0)
        self.assertTrue(reference.retry_decision("transient", 1, 1.0))
        self.assertFalse(reference.retry_decision("transient", 2, 1.0))
        self.assertFalse(reference.retry_decision("auth", 1, 1.0))
        self.assertFalse(reference.retry_decision("transient", 1, 0.0))

    def test_evidence_gate_is_blocked_and_never_self_passes(self):
        self.assertFalse(reference.evaluate_evidence({})["m22_pass"])
        receipts = {key: True for key in reference.REQUIRED_EVIDENCE}
        decision = reference.evaluate_evidence(receipts)
        self.assertEqual(decision["status"], "ELIGIBLE_FOR_INDEPENDENT_REVIEW")
        self.assertFalse(decision["m22_pass"])
        self.assertEqual(reference.evaluate_evidence({**receipts, "identity_flow_actual": False})["status"], "BLOCKED")

    def test_learner_seams_remain_explicit_todos(self):
        calls = [lambda: learner.verify_identity("token", lambda _: {}, lambda _: {},
                                                 expected_issuer="issuer", expected_audience="app", now=0),
                 lambda: learner.authorize_document(self.context, self.docs["owned"], "read"),
                 lambda: learner.filter_retrieved_hits(self.context, [], self.docs),
                 lambda: learner.safe_audit_event({}),
                 lambda: learner.remaining_budget(1, 0),
                 lambda: learner.retry_decision("transient", 1, 1),
                 lambda: learner.evaluate_evidence({})]
        for call in calls:
            with self.subTest(call=call), self.assertRaises(NotImplementedError):
                call()


if __name__ == "__main__":
    unittest.main()
