"""Acceptance contract for M2.2 learner seams; default candidate intentionally fails."""
from __future__ import annotations

import importlib
import os
import unittest


CANDIDATE = importlib.import_module(os.environ.get("M22_CANDIDATE", "ai.fde_v2.m22.learner"))


class M22LearnerAcceptance(unittest.TestCase):
    def setUp(self):
        self.context = {"subject_id": "alice", "tenant_id": "tenant-a"}
        self.docs = {
            "own": {"tenant_id": "tenant-a", "owner_id": "alice", "reader_ids": [],
                    "writer_ids": [], "deleted": False, "active_version": 2,
                    "active_chunk_ids": ["own-current"]},
            "team-read": {"tenant_id": "tenant-a", "owner_id": "bob", "reader_ids": ["alice"],
                          "writer_ids": [], "deleted": False, "active_version": 1,
                          "active_chunk_ids": ["team-read-current"]},
            "foreign": {"tenant_id": "tenant-b", "owner_id": "alice", "reader_ids": ["alice"],
                        "writer_ids": ["alice"], "deleted": False, "active_version": 1,
                        "active_chunk_ids": ["foreign-current"]},
            "deleted": {"tenant_id": "tenant-a", "owner_id": "alice", "reader_ids": [],
                        "writer_ids": [], "deleted": True, "active_version": 1,
                        "active_chunk_ids": ["deleted-current"]},
        }

    def test_verified_identity_adapter_checks_issuer_audience_expiry_and_subject(self):
        claims = {"sub": "alice", "iss": "issuer", "aud": "app", "exp": 100}
        lookup = lambda _subject: {"enabled": True, "tenant_id": "tenant-a"}
        self.assertEqual(CANDIDATE.verify_identity("opaque", lambda _: claims, lookup,
                                                   expected_issuer="issuer", expected_audience="app", now=10),
                         self.context)
        for broken in ({**claims, "iss": "attacker"}, {**claims, "aud": "other"},
                       {**claims, "exp": 10}, {**claims, "sub": ""}):
            with self.subTest(broken=broken), self.assertRaises(ValueError):
                CANDIDATE.verify_identity("opaque", lambda _token, c=broken: c, lookup,
                                          expected_issuer="issuer", expected_audience="app", now=10)
        with self.assertRaises(ValueError):
            CANDIDATE.verify_identity("opaque", lambda _: claims, lambda _: None,
                                      expected_issuer="issuer", expected_audience="app", now=10)

    def test_server_authorization_positive_and_negative(self):
        self.assertTrue(CANDIDATE.authorize_document(self.context, self.docs["own"], "read"))
        self.assertTrue(CANDIDATE.authorize_document(self.context, self.docs["team-read"], "read"))
        self.assertFalse(CANDIDATE.authorize_document(self.context, self.docs["team-read"], "write"))
        self.assertFalse(CANDIDATE.authorize_document(self.context, self.docs["foreign"], "read"))
        self.assertFalse(CANDIDATE.authorize_document(self.context, self.docs["deleted"], "read"))
        self.assertFalse(CANDIDATE.authorize_document({"subject_id": "alice"}, self.docs["own"], "read"))
        self.assertFalse(CANDIDATE.authorize_document(self.context, self.docs["own"], "unknown"))

    def test_vector_hits_are_revalidated_against_authoritative_access_state(self):
        hits = [
            {"document_id": "own", "chunk_id": "own-old", "version": 1},
            {"document_id": "own", "chunk_id": "own-current", "version": 2},
            {"document_id": "foreign", "chunk_id": "foreign-current", "version": 1},
            {"document_id": "deleted", "chunk_id": "deleted-current", "version": 1},
            {"document_id": "unknown", "chunk_id": "unknown-current", "version": 1},
            {"document_id": "own", "chunk_id": "own-current", "version": 2},
        ]
        got = CANDIDATE.filter_retrieved_hits(self.context, hits, self.docs)
        self.assertEqual(got, [{"document_id": "own", "chunk_id": "own-current", "version": 2}])

    def test_audit_record_drops_arbitrary_content_and_secret_fields(self):
        event = {"subject_pseudonym": "h:" + "a" * 32, "action": "ask", "outcome": "denied",
                 "trace_id": "b" * 32, "timestamp": "2026-10-29T00:00:00Z", "prompt": "private", "secret": "canary"}
        result = CANDIDATE.safe_audit_event(event)
        self.assertEqual(set(result), {"subject_pseudonym", "action", "outcome", "trace_id", "timestamp"})
        self.assertNotIn("canary", repr(result))
        for key in ("subject_pseudonym", "action", "outcome", "trace_id", "timestamp"):
            with self.subTest(key=key), self.assertRaises(ValueError):
                CANDIDATE.safe_audit_event({**event, key: "SECRET-PROMPT-CANARY"})

    def test_retry_uses_one_deadline_and_rejects_permanent_or_unbounded_retries(self):
        self.assertEqual(CANDIDATE.remaining_budget(12, 10), 2)
        self.assertEqual(CANDIDATE.remaining_budget(10, 12), 0)
        self.assertTrue(CANDIDATE.retry_decision("transient", 1, 0.5))
        self.assertFalse(CANDIDATE.retry_decision("auth", 1, 0.5))
        self.assertFalse(CANDIDATE.retry_decision("transient", 2, 0.5))
        self.assertFalse(CANDIDATE.retry_decision("transient", 1, 0))

    def test_evidence_gate_never_claims_a_milestone_pass(self):
        incomplete = CANDIDATE.evaluate_evidence({})
        self.assertEqual(incomplete["status"], "BLOCKED")
        self.assertFalse(incomplete["m22_pass"])


if __name__ == "__main__":
    unittest.main()
