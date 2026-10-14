from __future__ import annotations

import hashlib
import json
import copy
import unittest
from contextlib import contextmanager
from pathlib import Path

from ai.coverstory.cap_72 import ValidatedProviderRow, load_provider_batch

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-72/synthetic-v1.json"


def row(value: dict) -> ValidatedProviderRow:
    return ValidatedProviderRow(**value)


def changed_amount(value: dict) -> dict:
    changed = {**value, "amount_minor": value["amount_minor"] + 1}
    basis = {key: item for key, item in changed.items() if key != "payload_hash"}
    canonical = json.dumps(basis, sort_keys=True, separators=(",", ":"), ensure_ascii=False)
    changed["payload_hash"] = hashlib.sha256(canonical.encode("utf-8")).hexdigest()
    return changed


class MemoryPaymentsUnitOfWork:
    """Persistence adapter only; it deliberately contains no CAP-72 decisions."""
    def __init__(self):
        self.manifests = self
        self.provider_events = self
        self.transaction_audit = self
        self.payment_effects = self
        self._manifests = {}
        self._observations = []
        self._audits = []
        self.effects = {}
        self.application_transitions = []
        self.fail_after = None
        self.write_count = 0

    @contextmanager
    def transaction(self):
        before = copy.deepcopy((self._manifests, self._observations, self._audits,
                                self.effects, self.application_transitions))
        old_count = self.write_count
        try:
            yield self
        except Exception:
            (self._manifests, self._observations, self._audits,
             self.effects, self.application_transitions) = before
            self.write_count = old_count
            raise

    def _write(self):
        self.write_count += 1
        if self.fail_after == self.write_count:
            raise RuntimeError("injected persistence failure")

    def find_manifest(self, batch_id):
        return self._manifests.get(batch_id)

    def save_manifest(self, manifest):
        self._manifests[manifest.batch_id] = manifest
        self._write()

    def find_event(self, provider_id, event_id):
        return next((x for x in self._observations
                     if x.row.provider_id == provider_id and x.row.event_id == event_id), None)

    def append_observation(self, observation):
        self._observations.append(observation)
        self._write()

    def next_sequence(self, attempt_id):
        return 1 + max((x.sequence for x in self._audits if x.attempt_id == attempt_id), default=0)

    def append_audit(self, event):
        self._audits.append(event)
        self._write()

    def posted_effect_total(self, operation_id):
        return self.effects.get(operation_id, (0, "INR"))


class CAP72AcceptanceTest(unittest.TestCase):
    """Saved learner contract. Failures at the explicit TODO are expected today."""

    def setUp(self):
        self.data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.uow = MemoryPaymentsUnitOfWork()

    def test_failed_and_unknown_observations_commit_in_order_without_effect(self):
        report = load_provider_batch(self.data["batch_id"], [row(x) for x in self.data["initial_rows"]], self.uow)
        self.assertEqual("LOADED", report.code)
        self.assertEqual(self.data["receipt_id"], report.receipt_id)
        self.assertEqual(2, report.observation_count)
        self.assertEqual(["FAILED", "UNKNOWN"], [x.state for x in self.uow._audits])
        self.assertEqual(["event-synth-failed-001", "event-synth-unknown-001"],
                         [x.row.event_id for x in self.uow._observations])
        self.assertEqual({}, self.uow.effects)
        self.assertEqual([], self.uow.application_transitions)

    def test_exact_batch_replay_returns_original_receipt(self):
        rows = [row(x) for x in self.data["initial_rows"]]
        first = load_provider_batch(self.data["batch_id"], rows, self.uow)
        replay = load_provider_batch(self.data["batch_id"], rows, self.uow)
        self.assertEqual("ALREADY_LOADED", replay.code)
        self.assertEqual(first.receipt_id, replay.receipt_id)

    def test_changed_batch_or_event_content_conflicts(self):
        rows = [row(x) for x in self.data["initial_rows"]]
        load_provider_batch(self.data["batch_id"], rows, self.uow)
        before = (list(self.uow._observations), list(self.uow._audits), dict(self.uow._manifests))
        changed_batch = load_provider_batch(self.data["batch_id"], rows[:1], self.uow)
        self.assertEqual("BATCH_CONFLICT", changed_batch.code)
        changed_event = changed_amount(self.data["initial_rows"][0])
        event_conflict = load_provider_batch("batch-synth-other", [row(changed_event)], self.uow)
        self.assertEqual("EVENT_HASH_CONFLICT", event_conflict.code)
        self.assertEqual(before, (self.uow._observations, self.uow._audits, self.uow._manifests))

    def test_write_failure_rolls_back_provider_audit_and_manifest(self):
        rows = [row(x) for x in self.data["initial_rows"]]
        for fail_after in range(1, 6):
            with self.subTest(fail_after=fail_after):
                self.uow = MemoryPaymentsUnitOfWork()
                self.uow.fail_after = fail_after
                try:
                    load_provider_batch(self.data["batch_id"], rows, self.uow)
                except NotImplementedError as error:
                    if "CAP-72 learner TODO" in str(error):
                        raise
                    raise
                except RuntimeError as error:
                    self.assertEqual("injected persistence failure", str(error))
                else:
                    self.fail("injected write failure must escape the unit of work")
                self.assertEqual({}, self.uow._manifests)
                self.assertEqual([], self.uow._observations)
                self.assertEqual([], self.uow._audits)
                self.assertEqual([], self.uow.application_transitions)

    def test_unknown_reconciliation_appends_history_and_keeps_effect_unique(self):
        load_provider_batch(self.data["batch_id"], [row(x) for x in self.data["initial_rows"]], self.uow)
        self.uow.effects["operation-synth-unknown"] = (25000, "INR")
        report = load_provider_batch("batch-synth-reconcile", [row(self.data["reconciliation_row"])], self.uow)
        self.assertEqual("LOADED", report.code)
        self.assertEqual(["UNKNOWN", "SUCCEEDED"], [x.state for x in self.uow._audits
                                                       if x.attempt_id == "attempt-synth-unknown"])
        self.assertEqual((25000, "INR"), self.uow.posted_effect_total("operation-synth-unknown"))


if __name__ == "__main__":
    unittest.main()
