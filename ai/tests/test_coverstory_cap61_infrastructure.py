from __future__ import annotations

import json
import unittest
from pathlib import Path

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-61/synthetic-v1.json"


class CAP61InfrastructureTest(unittest.TestCase):
    def test_synthetic_oracle_has_threshold_boundary_access_and_invalid_vectors(self) -> None:
        fixture = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.assertEqual("CAP-61-synthetic-v1", fixture["fixture_id"])
        self.assertEqual(4, len(fixture["cases"]))
        self.assertEqual([0.7, 0.699], fixture["cases"][1]["scores"])
        self.assertFalse(fixture["cases"][2]["hits"][0]["authorized"])
        self.assertIn("NaN", fixture["cases"][3]["invalid_scores"])


if __name__ == "__main__": unittest.main()
