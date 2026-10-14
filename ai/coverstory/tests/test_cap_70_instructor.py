from __future__ import annotations

import base64
import hashlib
import json
import unittest
from pathlib import Path

from ai.coverstory.cap_70 import ProtectedEnvelope, StageError, StagedEnvelope

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-70/synthetic-v1.json"
FIELDS = {"envelope_id", "owner_id", "object_id", "object_revision", "ciphertext_with_tag", "nonce",
          "wrapped_dek", "kek_reference", "encryption_version"}


class FixtureProtectedSink:
    """Test-only owner/AEAD/storage adapter; no plaintext and no production persistence."""

    def __init__(self, fixture: dict, *, owner_id: str | None = None, key_available: bool = True):
        self.owner_id = owner_id or fixture["valid_envelopes"][0]["owner_id"]
        self.expected = {row["envelope_id"]: row for row in fixture["valid_envelopes"]}
        self.key_available = key_available
        self.calls: list[str] = []
        self.rows: dict[tuple[str, int], StagedEnvelope] = {}

    def owns_reference(self, owner_id: str, object_id: str, revision: int) -> bool:
        self.calls.append("authorize")
        return owner_id == self.owner_id and any(
            row["object_id"] == object_id and row["object_revision"] == revision for row in self.expected.values()
        )

    def validate_authenticated_envelope(self, envelope: ProtectedEnvelope) -> None:
        self.calls.append("authenticate")
        if not self.key_available:
            raise StageError(0, "KEY_UNAVAILABLE")
        known = self.expected.get(envelope.envelope_id)
        if known is None or known["object_id"] != envelope.object_id or known["object_revision"] != envelope.object_revision:
            raise StageError(0, "AUTHENTICATION_FAILED")
        if base64.b64decode(known["ciphertext_with_tag"]) != envelope.ciphertext_with_tag:
            raise StageError(0, "AUTHENTICATION_FAILED")
        if base64.b64decode(known["nonce"]) != envelope.nonce or base64.b64decode(known["wrapped_dek"]) != envelope.wrapped_dek:
            raise StageError(0, "AUTHENTICATION_FAILED")
        if known["kek_reference"] != envelope.kek_reference:
            raise StageError(0, "KEY_UNAVAILABLE")

    def store_immutable_batch(self, batch_id: str, rows: tuple[StagedEnvelope, ...]) -> None:
        self.calls.append("store")
        staged = dict(self.rows)
        for row in rows:
            key = (row.envelope.object_id, row.envelope.object_revision)
            if key in staged and staged[key] != row:
                raise ValueError("IMMUTABLE_CONFLICT")
            staged[key] = row
        self.rows = staged


def reference_stage(batch_id: str, lines: list[str], sink: FixtureProtectedSink, source_name: str = "offline.ndjson"):
    """Independent instructor pseudocode. It never returns or logs plaintext."""
    pending: list[StagedEnvelope] = []
    evidence = []
    skipped = 0
    for line_number, raw in enumerate(lines, start=1):
        if not raw.strip():
            skipped += 1
            continue
        try:
            row = json.loads(raw)
        except (json.JSONDecodeError, TypeError):
            raise StageError(line_number, "MALFORMED_JSON") from None
        if not isinstance(row, dict):
            raise StageError(line_number, "INVALID_RECORD")
        if set(row) != FIELDS:
            raise StageError(line_number, "UNKNOWN_OR_MISSING_FIELD")
        try:
            envelope = ProtectedEnvelope(
                envelope_id=str(row["envelope_id"]), owner_id=str(row["owner_id"]), object_id=str(row["object_id"]),
                object_revision=int(row["object_revision"]),
                ciphertext_with_tag=base64.b64decode(row["ciphertext_with_tag"], validate=True),
                nonce=base64.b64decode(row["nonce"], validate=True), wrapped_dek=base64.b64decode(row["wrapped_dek"], validate=True),
                kek_reference=str(row["kek_reference"]), encryption_version=int(row["encryption_version"]),
            )
        except (ValueError, TypeError):
            raise StageError(line_number, "INVALID_ENVELOPE_ENCODING") from None
        if envelope.object_revision < 1 or envelope.encryption_version != 1 or len(envelope.nonce) != 12 or len(envelope.ciphertext_with_tag) < 16:
            raise StageError(line_number, "UNSUPPORTED_OR_MALFORMED_ENVELOPE")
        if not sink.owns_reference(envelope.owner_id, envelope.object_id, envelope.object_revision):
            raise StageError(line_number, "OWNER_FORBIDDEN")
        try:
            sink.validate_authenticated_envelope(envelope)
        except StageError as error:
            raise StageError(line_number, error.code) from None
        digest = hashlib.sha256(envelope.ciphertext_with_tag).hexdigest()
        item = StagedEnvelope(envelope, source_name, line_number, digest)
        pending.append(item)
        evidence.append((envelope.envelope_id, envelope.object_id, envelope.object_revision, source_name, line_number, digest))
    sink.store_immutable_batch(batch_id, tuple(pending))
    return tuple(evidence), skipped


class CAP70InstructorOracleTests(unittest.TestCase):
    def setUp(self) -> None:
        self.fixture = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.rows = self.fixture["valid_envelopes"]
        self.lines = [json.dumps(row, separators=(",", ":")) for row in self.rows]

    def test_fixture_contains_only_synthetic_fixed_vectors_and_integrity_hashes_match(self) -> None:
        self.assertFalse(self.fixture["actual_customer_data"])
        self.assertFalse(self.fixture["plaintext_persisted"])
        self.assertEqual(2, len(self.fixture["fixed_vectors"]))
        actual = [hashlib.sha256(base64.b64decode(row["ciphertext_with_tag"], validate=True)).hexdigest() for row in self.rows]
        self.assertEqual(self.fixture["expected_ciphertext_sha256"], actual)

    def test_reference_preserves_ciphertext_key_material_and_physical_line_provenance(self) -> None:
        sink = FixtureProtectedSink(self.fixture)
        evidence, skipped = reference_stage("batch-70-a", [self.lines[0], "", self.lines[1]], sink)
        self.assertEqual(1, skipped)
        self.assertEqual([1, 3], [row.source_line for row in sink.rows.values()])
        self.assertEqual([1, 3], [row[4] for row in evidence])
        self.assertEqual([base64.b64decode(row["ciphertext_with_tag"]) for row in self.rows],
                         [row.envelope.ciphertext_with_tag for row in sink.rows.values()])
        self.assertEqual([base64.b64decode(row["nonce"]) for row in self.rows],
                         [row.envelope.nonce for row in sink.rows.values()])
        self.assertEqual([base64.b64decode(row["wrapped_dek"]) for row in self.rows],
                         [row.envelope.wrapped_dek for row in sink.rows.values()])
        self.assertTrue(all(not hasattr(row, "plaintext") for row in sink.rows.values()))

    def test_owner_is_authorized_before_authentication_and_plaintext_field_is_redacted(self) -> None:
        sink = FixtureProtectedSink(self.fixture, owner_id="70000000-0000-4000-8000-000000000099")
        with self.assertRaises(StageError) as denied:
            reference_stage("batch-70-denied", [self.lines[0]], sink)
        self.assertEqual((1, "OWNER_FORBIDDEN"), (denied.exception.line_number, denied.exception.code))
        self.assertEqual(["authorize"], sink.calls)
        self.assertEqual({}, sink.rows)

        plaintext = json.loads(self.lines[0]); plaintext["healthAnswer"] = "SYNTHETIC-HEALTH-MARKER"
        with self.assertRaises(StageError) as rejected:
            reference_stage("batch-70-plaintext", [json.dumps(plaintext)], FixtureProtectedSink(self.fixture))
        self.assertEqual("UNKNOWN_OR_MISSING_FIELD", rejected.exception.code)
        self.assertNotIn("SYNTHETIC-HEALTH-MARKER", str(rejected.exception))

    def test_malformed_or_unavailable_envelopes_fail_closed_without_partial_batch(self) -> None:
        good = json.loads(self.lines[0]); bad = json.loads(self.lines[1]); bad["nonce"] = "AA=="
        sink = FixtureProtectedSink(self.fixture)
        with self.assertRaises(StageError) as malformed:
            reference_stage("batch-70-partial", [json.dumps(good), json.dumps(bad)], sink)
        self.assertEqual((2, "UNSUPPORTED_OR_MALFORMED_ENVELOPE"), (malformed.exception.line_number, malformed.exception.code))
        self.assertEqual({}, sink.rows, "invalid batch must not leave partial staged rows")

        unavailable = FixtureProtectedSink(self.fixture, key_available=False)
        with self.assertRaises(StageError) as missing_key:
            reference_stage("batch-70-key", [self.lines[0]], unavailable)
        self.assertEqual("KEY_UNAVAILABLE", missing_key.exception.code)
        self.assertEqual({}, unavailable.rows)

    def test_tampered_tag_or_unsupported_version_rejects_without_echoing_input(self) -> None:
        bad = json.loads(self.lines[0]); raw = bytearray(base64.b64decode(bad["ciphertext_with_tag"])); raw[-1] ^= 1
        bad["ciphertext_with_tag"] = base64.b64encode(raw).decode()
        sink = FixtureProtectedSink(self.fixture)
        with self.assertRaises(StageError) as tampered:
            reference_stage("batch-70-tamper", [json.dumps(bad)], sink)
        self.assertEqual("AUTHENTICATION_FAILED", tampered.exception.code)
        self.assertNotIn(bad["ciphertext_with_tag"], str(tampered.exception))
        self.assertEqual({}, sink.rows)

        unsupported = json.loads(self.lines[0]); unsupported["encryption_version"] = 99
        with self.assertRaises(StageError) as version:
            reference_stage("batch-70-version", [json.dumps(unsupported)], FixtureProtectedSink(self.fixture))
        self.assertEqual("UNSUPPORTED_OR_MALFORMED_ENVELOPE", version.exception.code)


if __name__ == "__main__":
    unittest.main()
