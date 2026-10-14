from __future__ import annotations

import base64
import json
import unittest
from pathlib import Path

from ai.coverstory.cap_70 import StageError, stage_envelopes

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-70/synthetic-v1.json"


class FixtureSink:
    def __init__(self, fixture: dict, *, owner: str | None = None, valid: bool = True):
        self.owner = owner or fixture["valid_envelopes"][0]["owner_id"]
        self.valid = valid
        self.rows = []

    def owns_reference(self, owner_id: str, object_id: str, revision: int) -> bool:
        return owner_id == self.owner

    def validate_authenticated_envelope(self, envelope) -> None:
        if not self.valid:
            raise StageError(0, "AUTHENTICATION_FAILED")

    def store_immutable_batch(self, batch_id: str, rows: tuple) -> None:
        self.rows.extend(rows)


class CAP70AcceptanceTest(unittest.TestCase):
    def setUp(self) -> None:
        self.fixture = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.lines = [json.dumps(row, separators=(",", ":")) for row in self.fixture["valid_envelopes"]]

    def test_two_synthetic_envelopes_preserve_exact_bytes_and_revision_bindings(self) -> None:
        sink = FixtureSink(self.fixture)
        report = stage_envelopes("learner-batch", self.lines, sink)
        self.assertEqual(2, len(report.records))
        self.assertEqual(["70000000-0000-4000-8000-000000000010", "70000000-0000-4000-8000-000000000011"],
                         [row.object_id for row in report.records])
        self.assertEqual([base64.b64decode(row["ciphertext_with_tag"]) for row in self.fixture["valid_envelopes"]],
                         [row.envelope.ciphertext_with_tag for row in sink.rows])

    def test_blank_physical_line_is_skipped_without_renumbering_provenance(self) -> None:
        report = stage_envelopes("learner-batch", ["", self.lines[0]], FixtureSink(self.fixture))
        self.assertEqual(1, report.skipped_blank_lines)
        self.assertEqual([2], [row.source_line for row in report.records])

    def test_plaintext_or_malformed_line_is_rejected_with_safe_line_code(self) -> None:
        row = json.loads(self.lines[0]); row["email"] = "synthetic@example.invalid"
        raw = json.dumps(row)
        with self.assertRaises(StageError) as caught:
            stage_envelopes("learner-batch", [raw], FixtureSink(self.fixture))
        self.assertEqual(1, caught.exception.line_number)
        self.assertNotIn("synthetic@example.invalid", str(caught.exception))

    def test_owner_authentication_and_aead_fail_closed(self) -> None:
        denied = FixtureSink(self.fixture, owner="other-owner")
        with self.assertRaises(StageError):
            stage_envelopes("learner-batch", self.lines[:1], denied)
        invalid_tag = FixtureSink(self.fixture, valid=False)
        with self.assertRaises(StageError):
            stage_envelopes("learner-batch", self.lines[:1], invalid_tag)

    def test_fixed_vector_is_named_and_not_used_as_a_generated_nonce(self) -> None:
        self.assertEqual(2, len(self.fixture["fixed_vectors"]))
        nonces = [vector["nonce"] for vector in self.fixture["fixed_vectors"]]
        self.assertEqual(len(nonces), len(set(nonces)))
        self.assertTrue(all(vector["object_id"] and vector["aad"] for vector in self.fixture["fixed_vectors"]))


if __name__ == "__main__":
    unittest.main()
