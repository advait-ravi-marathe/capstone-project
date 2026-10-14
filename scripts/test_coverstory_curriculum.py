from __future__ import annotations

import unittest

import json

from check_coverstory_curriculum import check
from materialize_coverstory_curriculum import CURRICULUM, planned_paths, active_task


class CoverStoryCurriculumMaterializationTest(unittest.TestCase):
    def test_verified_source_and_all_daily_guides_are_preserved(self) -> None:
        self.assertEqual(
            {
                "learning_cards": 600,
                "daily_primaries": 594,
                "linked_components": 6,
                "phase1_primaries": 228,
                "phase2_primaries": 366,
                "primary_guides_with_explicit_planned_paths": 228,
                "primary_guides_with_source_path_references_only": 0,
                "primary_guides_with_no_explicit_repository_path": 366,
            },
            check(),
        )

    def test_outcome_heading_precedes_inherited_architecture_preamble(self) -> None:
        cards = json.loads(CURRICULUM.read_text(encoding="utf-8"))["cards"]
        cap100 = next(card for card in cards if card["id"] == "CAP-100")
        cap13 = next(card for card in cards if card["id"] == "CAP-13")
        cap307 = next(card for card in cards if card["id"] == "CAP-307")
        cap507 = next(card for card in cards if card["id"] == "CAP-507")
        self.assertIn("Classify synthetic onboarding-extraction", active_task(cap100["description"]))
        self.assertIn("Extend comparison with sourced term-life fields", active_task(cap13["description"]))
        self.assertIn("authenticated Support scope", active_task(cap307["description"]))
        self.assertIn("Independently diagnose one seeded discrepancy", active_task(cap507["description"]))

    def test_labeled_planned_path_is_not_downgraded_to_a_reference(self) -> None:
        cards = json.loads(CURRICULUM.read_text(encoding="utf-8"))["cards"]
        cap235 = next(card for card in cards if card["id"] == "CAP-235")
        self.assertIn(
            "src/main/java/capstone/coverstory/application/FinalReadinessGate.java",
            planned_paths(cap235["description"]),
        )


if __name__ == "__main__":
    unittest.main()
