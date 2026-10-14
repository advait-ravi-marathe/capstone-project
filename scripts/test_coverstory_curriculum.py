from __future__ import annotations

import unittest

from check_coverstory_curriculum import check


class CoverStoryCurriculumMaterializationTest(unittest.TestCase):
    def test_verified_source_and_all_daily_guides_are_preserved(self) -> None:
        self.assertEqual(
            {
                "learning_cards": 600,
                "daily_primaries": 594,
                "linked_components": 6,
                "phase1_primaries": 228,
                "phase2_primaries": 366,
                "primary_guides_with_explicit_planned_paths": 152,
                "primary_guides_with_source_path_references_only": 76,
                "primary_guides_with_no_explicit_repository_path": 366,
            },
            check(),
        )


if __name__ == "__main__":
    unittest.main()
