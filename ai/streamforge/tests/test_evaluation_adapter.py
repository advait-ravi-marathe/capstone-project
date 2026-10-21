"""Green instructor tests for the typed boundary to earlier preparation."""
from __future__ import annotations

import hashlib
import unittest
from unittest.mock import patch

from ai.streamforge.evaluation_adapter import prepare_evaluation_input
from ai.streamforge.tests.golden import (COVERAGE, HOLDOUT, OUTSIDE, REFERENCE_CATALOG,
                                         SUMMARY, TRAIN, expected_train_content_hash)


def valid_bundle():
    source_hashes = {
        name: hashlib.sha256(payload).hexdigest()
        for name, payload in {
            "manifest.json": b"fixture manifest sentinel",
            "catalog.csv": b"catalog sentinel",
            "events.csv": b"events sentinel",
        }.items()
    }
    return {
        "schema_version": 1,
        "reference_catalog": REFERENCE_CATALOG,
        "train": TRAIN,
        "holdout": HOLDOUT,
        "outside_window": OUTSIDE,
        "training": {**SUMMARY, "content_sha256": expected_train_content_hash()},
        "holdout_diagnostics": COVERAGE,
        "source_sha256": source_hashes,
        "duplicate_events_collapsed": 0,
    }, source_hashes


class EvaluationAdapterTest(unittest.TestCase):
    def setUp(self):
        self.bundle, self.hashes = valid_bundle()
        # The adapter compares prepared hashes to the exact registered fixture
        # bytes, so patch only the preparation result, not the input reader.
        from ai.streamforge.evaluation_adapter import read_fixture_manifest
        self.manifest, source_bytes = read_fixture_manifest("basic")
        self.bundle["source_sha256"] = {
            name: hashlib.sha256(payload).hexdigest() for name, payload in source_bytes.items()
        }

    def test_calls_prior_preparation_once_and_preserves_typed_inputs_and_lineage(self):
        with patch("ai.streamforge.evaluation_adapter.prepare_dataset", return_value=self.bundle) as prepare:
            result = prepare_evaluation_input("basic")
        prepare.assert_called_once_with("basic")
        self.assertEqual("basic", result.fixture_id)
        self.assertEqual("2027-01-20T00:00:00Z", result.cutoff_utc)
        self.assertEqual(3, len(result.catalog))
        self.assertEqual(2, len(result.train))
        self.assertEqual(1, len(result.holdout))
        self.assertEqual(1, len(result.outside_window))
        self.assertEqual(frozenset({"synthetic-viewer-1", "synthetic-viewer-2"}),
                         result.all_training_viewer_ids)
        self.assertEqual("available", result.lineage["preparation_status"]["training"])
        self.assertEqual(self.bundle["source_sha256"], result.lineage["source_sha256"])
        self.assertEqual(0, result.lineage["duplicate_events_collapsed"])

    def test_missing_or_wrong_partition_is_rejected_after_one_prior_call(self):
        for mutation in (
            lambda bundle: bundle.pop("holdout"),
            lambda bundle: bundle.update(train=[*bundle["train"], bundle["holdout"][0]]),
            lambda bundle: bundle.update(outside_window=[bundle["holdout"][0]]),
        ):
            candidate = {**self.bundle, "train": list(self.bundle["train"]),
                         "holdout": list(self.bundle["holdout"]),
                         "outside_window": list(self.bundle["outside_window"])}
            mutation(candidate)
            with self.subTest(candidate=candidate):
                with patch("ai.streamforge.evaluation_adapter.prepare_dataset", return_value=candidate) as prepare:
                    with self.assertRaises(ValueError):
                        prepare_evaluation_input("basic")
                prepare.assert_called_once_with("basic")

    def test_missing_catalog_and_unsupported_schema_are_rejected(self):
        for candidate in (
            {**self.bundle, "reference_catalog": []},
            {**self.bundle, "schema_version": 2},
        ):
            with self.subTest(candidate=candidate):
                with patch("ai.streamforge.evaluation_adapter.prepare_dataset", return_value=candidate) as prepare:
                    with self.assertRaises(ValueError):
                        prepare_evaluation_input("basic")
                prepare.assert_called_once_with("basic")

    def test_false_summary_hash_or_holdout_diagnostics_are_rejected(self):
        bad_hash = {**self.bundle, "training": {**self.bundle["training"], "content_sha256": "0" * 64}}
        bad_count = {**self.bundle, "training": {**self.bundle["training"],
            "media_event_counts": {"m1": True, "m2": 1}}}
        bad_diagnostics = {**self.bundle,
            "holdout_diagnostics": {**self.bundle["holdout_diagnostics"], "viewer_item_unseen": 1}}
        for candidate in (bad_hash, bad_count, bad_diagnostics):
            with self.subTest(candidate=candidate):
                with patch("ai.streamforge.evaluation_adapter.prepare_dataset", return_value=candidate) as prepare:
                    with self.assertRaises(ValueError):
                        prepare_evaluation_input("basic")
                prepare.assert_called_once_with("basic")

    def test_named_prior_todo_propagates_without_fallback_or_retry(self):
        with patch("ai.streamforge.evaluation_adapter.prepare_dataset",
                   side_effect=NotImplementedError("Day 166 learner TODO: implement prepare_dataset")) as prepare:
            with self.assertRaisesRegex(NotImplementedError, "Day 166 learner TODO"):
                prepare_evaluation_input("basic")
        prepare.assert_called_once_with("basic")


if __name__ == "__main__":
    unittest.main()
