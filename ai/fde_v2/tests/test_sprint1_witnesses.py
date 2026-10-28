"""Prove independent acceptance selectors reject simple faulty implementations."""
from __future__ import annotations

import json
import sqlite3
import unittest
from unittest.mock import patch

from ai.fde_v2.coverstory.contracts import Document
from ai.fde_v2.coverstory import reference
from ai.fde_v2.coverstory.corpus import SCHEMA
from ai.fde_v2.learner_acceptance import test_units


class Sprint1MutationControls(unittest.TestCase):
    def reject_mutant(self, selector: str, name: str, mutant) -> None:
        candidate = test_units.candidate
        original = getattr(candidate, name)
        setattr(candidate, name, mutant)
        try:
            with self.assertRaises(AssertionError):
                getattr(test_units.First30LearnerAcceptance(), selector)()
        finally:
            setattr(candidate, name, original)

    def test_u003_acceptance_rejects_constant_document(self):
        def wrong(_raw):
            return Document("wrong", 1, "wrong", "synthetic://wrong", "x", "0" * 64)
        self.reject_mutant("test_u003_parse_document_record", "parse_document_record", wrong)

    def test_u008_acceptance_rejects_adapter_bypass(self):
        self.reject_mutant("test_u008_generate_text_bounds_and_forwards_to_real_adapter",
                           "generate_text", lambda *_args, **_kwargs: ("constant", 0.0))

    def test_u010_acceptance_rejects_constant_valid_json(self):
        good = {"answer": "x", "citations": [], "abstained": False}
        self.reject_mutant("test_u010_answer_json_rejects_wrong_shapes_and_types",
                           "parse_answer_json", lambda _text: dict(good))

    def test_u011_acceptance_rejects_noop_metadata_writer(self):
        self.reject_mutant("test_u011_first_insert_and_identical_reingest",
                           "upsert_document_metadata", lambda *_args, **_kwargs: "inserted")

    def test_reference_witnesses_pass_the_actual_sprint1_acceptance_paths(self):
        previous = test_units.candidate
        test_units.candidate = reference
        try:
            suite = unittest.defaultTestLoader.loadTestsFromTestCase(test_units.First30LearnerAcceptance)
            result = unittest.TestResult()
            suite.run(result)
            self.assertEqual(result.errors, [])
            self.assertEqual(result.failures, [])
            self.assertEqual(result.testsRun, 4)
        finally:
            test_units.candidate = previous


if __name__ == "__main__":
    unittest.main()
