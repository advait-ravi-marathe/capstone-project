"""Independent CAP-72 instructor oracle over a transactional in-memory store.

This exercises the contract without implementing the learner module or contacting
any payment provider. The oracle intentionally lives outside learner acceptance.
"""
from __future__ import annotations

import copy
import hashlib
import json
import sys
import unittest
from contextlib import contextmanager
from dataclasses import asdict
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[2]))
from ai.coverstory.cap_72 import (  # noqa: E402
    LoadReport, Manifest, ProviderObservation, TransactionAuditEvent,
    ValidatedProviderRow,
)

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-72/synthetic-v1.json"


def stable_hash(value: object) -> str:
    body = json.dumps(value, sort_keys=True, separators=(",", ":"), ensure_ascii=False)
    return hashlib.sha256(body.encode("utf-8")).hexdigest()


def parse_row(value: dict) -> ValidatedProviderRow:
    return ValidatedProviderRow(**value)


class FakePaymentsUnitOfWork:
    def __init__(self):
        self._manifests: dict[str, Manifest] = {}
        self._observations: list[ProviderObservation] = []
        self._audits: list[TransactionAuditEvent] = []
        self.effects: dict[str, tuple[int, str]] = {}
        self.application_transitions: list[str] = []
        self.fail_after: int | None = None
        self.write_count = 0
        self.manifests = self
        self.provider_events = self
        self.transaction_audit = self
        self.payment_effects = self

    @contextmanager
    def transaction(self):
        before = (copy.deepcopy(self._manifests), copy.deepcopy(self._observations),
                  copy.deepcopy(self._audits), copy.deepcopy(self.effects),
                  copy.deepcopy(self.application_transitions))
        old_count = self.write_count
        try:
            yield self
        except Exception:
            (self._manifests, self._observations, self._audits, self.effects,
             self.application_transitions) = before
            self.write_count = old_count
            raise

    def _written(self):
        self.write_count += 1
        if self.fail_after == self.write_count:
            raise RuntimeError(f"injected failure after write {self.write_count}")

    def find_manifest(self, batch_id):
        return self._manifests.get(batch_id)

    def save_manifest(self, manifest):
        self._manifests[manifest.batch_id] = manifest
        self._written()

    def find_event(self, provider_id, event_id):
        return next((o for o in self._observations
                     if o.row.provider_id == provider_id and o.row.event_id == event_id), None)

    def append_observation(self, observation):
        self._observations.append(observation)
        self._written()

    def next_sequence(self, attempt_id):
        return 1 + max((a.sequence for a in self._audits if a.attempt_id == attempt_id), default=0)

    def append_audit(self, event):
        self._audits.append(event)
        self._written()

    def posted_effect_total(self, operation_id):
        return self.effects.get(operation_id, (0, "INR"))

    def snapshot(self):
        return {
            "manifests": dict(self._manifests),
            "observations": list(self._observations),
            "audits": list(self._audits),
            "effects": dict(self.effects),
            "application_transitions": list(self.application_transitions),
        }


def oracle_load(batch_id: str, rows: list[ValidatedProviderRow], uow: FakePaymentsUnitOfWork) -> LoadReport:
    """Test-only reference for contract witnesses; never imported by learner tests."""
    batch_hash = stable_hash([asdict(row) for row in rows])
    with uow.transaction():
        prior = uow.find_manifest(batch_id)
        if prior:
            if prior.content_hash != batch_hash:
                return LoadReport("BATCH_CONFLICT", prior.receipt_id, 0)
            return LoadReport("ALREADY_LOADED", prior.receipt_id, len(rows))

        for row in rows:
            prior_event = uow.find_event(row.provider_id, row.event_id)
            if prior_event and stable_hash(asdict(prior_event.row)) != stable_hash(asdict(row)):
                return LoadReport("EVENT_HASH_CONFLICT", "", 0)

        new_rows = [row for row in rows if uow.find_event(row.provider_id, row.event_id) is None]
        if not new_rows and rows:
            first = uow.find_event(rows[0].provider_id, rows[0].event_id)
            return LoadReport("ALREADY_LOADED", uow.find_manifest(first.batch_id).receipt_id, len(rows))

        for ordinal, row in enumerate(rows, start=1):
            existing = uow.find_event(row.provider_id, row.event_id)
            if existing:
                continue
            uow.append_observation(ProviderObservation(batch_id, row, ordinal))
            uow.append_audit(TransactionAuditEvent(
                row.attempt_id, row.operation_id, uow.next_sequence(row.attempt_id),
                row.event_id, row.state, row.amount_minor, row.currency,
                row.request_id, row.provider_reference,
            ))
        receipt_id = "receipt-" + batch_id
        uow.save_manifest(Manifest(batch_id, batch_hash, receipt_id, "COMPLETED"))
        return LoadReport("LOADED", receipt_id, len(rows))


def require_contract(store: FakePaymentsUnitOfWork, expected_event_ids: list[str]) -> None:
    actual = [observation.row.event_id for observation in store._observations]
    if actual != expected_event_ids:
        raise AssertionError(f"ordered provider observations differ: {actual!r}")
    audit_ids = [event.event_id for event in store._audits]
    if audit_ids != expected_event_ids:
        raise AssertionError(f"audit evidence differs from accepted observations: {audit_ids!r}")
    observation_states = [observation.row.state for observation in store._observations]
    audit_states = [event.state for event in store._audits]
    if audit_states != observation_states:
        raise AssertionError(f"audit states differ from immutable observation history: {audit_states!r}")
    if store.effects:
        raise AssertionError("batch loader changed owning payment effects")
    if store.application_transitions:
        raise AssertionError("batch loader changed application state")


class CAP72InfrastructureTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        cls.initial = [parse_row(x) for x in cls.data["initial_rows"]]
        cls.reconciliation = parse_row(cls.data["reconciliation_row"])

    def test_failed_unknown_batch_commits_ordered_observations_and_audit(self):
        store = FakePaymentsUnitOfWork()
        report = oracle_load(self.data["batch_id"], self.initial, store)
        self.assertEqual("LOADED", report.code)
        self.assertEqual(self.data["receipt_id"], report.receipt_id)
        self.assertEqual(["event-synth-failed-001", "event-synth-unknown-001"],
                         [o.row.event_id for o in store._observations])
        self.assertEqual([1, 1], [a.sequence for a in store._audits])
        self.assertEqual(["FAILED", "UNKNOWN"], [a.state for a in store._audits])
        self.assertIsNotNone(store.find_manifest(self.data["batch_id"]))
        self.assertEqual({}, store.effects)
        self.assertEqual([], store.application_transitions)
        require_contract(store, ["event-synth-failed-001", "event-synth-unknown-001"])

    def test_synthetic_event_payload_hashes_match_canonical_rows(self):
        for raw in self.data["initial_rows"] + [self.data["reconciliation_row"]]:
            basis = {key: value for key, value in raw.items() if key != "payload_hash"}
            self.assertEqual(stable_hash(basis), raw["payload_hash"], raw["event_id"])

    def test_reference_oracle_rejects_order_state_and_money_count_defects(self):
        store = FakePaymentsUnitOfWork()
        oracle_load(self.data["batch_id"], self.initial, store)
        wrong_order = copy.deepcopy(store)
        wrong_order._observations.reverse()
        with self.assertRaisesRegex(AssertionError, "ordered provider observations"):
            require_contract(wrong_order, ["event-synth-failed-001", "event-synth-unknown-001"])

        overwritten_unknown = copy.deepcopy(store)
        overwritten_unknown._audits[1] = TransactionAuditEvent(
            **{**asdict(overwritten_unknown._audits[1]), "state": "FAILED"})
        with self.assertRaisesRegex(AssertionError, "audit"):
            require_contract(overwritten_unknown,
                             ["event-synth-failed-001", "event-synth-unknown-001"])

        audit_counted_as_money = copy.deepcopy(store)
        audit_counted_as_money.effects["operation-synth-unknown"] = (50000, "INR")
        with self.assertRaisesRegex(AssertionError, "owning payment effects"):
            require_contract(audit_counted_as_money,
                             ["event-synth-failed-001", "event-synth-unknown-001"])

    def test_exact_batch_replay_preserves_receipt_without_duplicate_writes(self):
        store = FakePaymentsUnitOfWork()
        first = oracle_load(self.data["batch_id"], self.initial, store)
        before = store.snapshot()
        replay = oracle_load(self.data["batch_id"], self.initial, store)
        self.assertEqual("ALREADY_LOADED", replay.code)
        self.assertEqual(first.receipt_id, replay.receipt_id)
        self.assertEqual(before, store.snapshot())

    def test_batch_and_event_conflicts_leave_original_history_unchanged(self):
        store = FakePaymentsUnitOfWork()
        oracle_load(self.data["batch_id"], self.initial, store)
        before = store.snapshot()
        changed_batch = oracle_load(self.data["batch_id"], self.initial[:1], store)
        self.assertEqual("BATCH_CONFLICT", changed_batch.code)
        changed_fields = {**asdict(self.initial[0]), "amount_minor": self.initial[0].amount_minor + 1}
        changed_fields["payload_hash"] = stable_hash({k: v for k, v in changed_fields.items()
                                                       if k != "payload_hash"})
        changed = ValidatedProviderRow(**changed_fields)
        changed_event = oracle_load("batch-synth-changed-event", [changed], store)
        self.assertEqual("EVENT_HASH_CONFLICT", changed_event.code)
        self.assertEqual(before, store.snapshot())

    def test_failure_after_each_observation_audit_or_manifest_write_rolls_back(self):
        # 2 rows × (provider observation + audit), then one manifest write.
        for fail_after in range(1, 6):
            with self.subTest(fail_after=fail_after):
                store = FakePaymentsUnitOfWork()
                store.fail_after = fail_after
                with self.assertRaisesRegex(RuntimeError, "injected failure"):
                    oracle_load(self.data["batch_id"], self.initial, store)
                self.assertEqual({"manifests": {}, "observations": [], "audits": [], "effects": {},
                                  "application_transitions": []},
                                 store.snapshot())

    def test_unknown_reconciliation_appends_history_and_effect_total_is_owner_fact(self):
        store = FakePaymentsUnitOfWork()
        oracle_load(self.data["batch_id"], self.initial, store)
        store.effects["operation-synth-unknown"] = (25000, "INR")  # owning module posts once
        before_reconcile_total = store.posted_effect_total("operation-synth-unknown")
        report = oracle_load("batch-synth-reconcile", [self.reconciliation], store)
        self.assertEqual("LOADED", report.code)
        self.assertEqual(["UNKNOWN", "SUCCEEDED"], [a.state for a in store._audits
                                                       if a.attempt_id == "attempt-synth-unknown"])
        self.assertEqual([1, 2], [a.sequence for a in store._audits
                                  if a.attempt_id == "attempt-synth-unknown"])
        self.assertEqual(before_reconcile_total, store.posted_effect_total("operation-synth-unknown"))
        self.assertEqual((25000, "INR"), store.posted_effect_total("operation-synth-unknown"))
        self.assertEqual(3, len(store._audits))

    def test_provider_event_replay_in_new_batch_has_no_duplicate_audit(self):
        store = FakePaymentsUnitOfWork()
        first = oracle_load(self.data["batch_id"], self.initial, store)
        before = store.snapshot()
        replay = oracle_load("batch-synth-replay", self.initial, store)
        self.assertEqual("ALREADY_LOADED", replay.code)
        self.assertEqual(first.receipt_id, replay.receipt_id)
        self.assertEqual(before, store.snapshot())


if __name__ == "__main__":
    unittest.main()
