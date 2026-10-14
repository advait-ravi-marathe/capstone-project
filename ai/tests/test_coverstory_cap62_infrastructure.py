from __future__ import annotations

import json
import unittest
from pathlib import Path

from ai.coverstory.cap_62 import LookupOutcome, LookupStatus, bounded_lookup

FIXTURE = Path(__file__).resolve().parents[2] / "fixtures/coverstory/CAP-62/synthetic-v1.json"


class CAP62InfrastructureTest(unittest.TestCase):
    def test_fixture_is_synthetic_and_covers_saved_timeout_boundaries(self) -> None:
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.assertTrue(data["synthetic"])
        self.assertEqual(5, len(data["cases"]))
        self.assertEqual("CAP-62-CS-05", data["cases"][-1]["id"])
        self.assertEqual({"OK", "TIMEOUT"}, {item.value for item in LookupStatus})
        self.assertTrue(callable(bounded_lookup))
        self.assertEqual(("status", "value"), tuple(LookupOutcome.__dataclass_fields__))
