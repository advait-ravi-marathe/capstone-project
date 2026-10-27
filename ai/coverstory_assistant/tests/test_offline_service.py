import unittest

from ai.coverstory_assistant.server import offline_answer


class OfflineServiceTests(unittest.TestCase):
    def test_synthetic_hail_answers_are_tenant_bound_and_marked_offline(self):
        owner_a = offline_answer({"question": "Is hail covered?", "tenant_id": "owner-a"})
        owner_b = offline_answer({"question": "Is hail covered?", "tenant_id": "owner-b"})
        self.assertEqual("Hail damage is excluded.", owner_a["answer"])
        self.assertEqual("SYN-POL-A", owner_a["citations"][0]["documentId"])
        self.assertEqual("Hail damage is covered up to the listed limit.", owner_b["answer"])
        self.assertEqual("SYN-POL-B", owner_b["citations"][0]["documentId"])
        for result in (owner_a, owner_b):
            self.assertEqual("offline_synthetic_fixture", result["executionMode"])
            self.assertFalse(result["providerRequestMade"])
            self.assertFalse(result["liveQualityEvaluated"])

    def test_unsupported_question_abstains_and_foreign_tenant_fails(self):
        result = offline_answer({"question": "Is flood damage covered?", "tenant_id": "owner-a"})
        self.assertEqual({"answer": "I don't know.", "citations": [], "abstained": True}, {k: result[k] for k in ("answer", "citations", "abstained")})
        with self.assertRaises(PermissionError):
            offline_answer({"question": "Is hail covered?", "tenant_id": "owner-c"})
        with self.assertRaises(ValueError):
            offline_answer({"question": " ", "tenant_id": "owner-a"})
