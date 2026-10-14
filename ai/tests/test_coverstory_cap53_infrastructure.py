from __future__ import annotations

import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


class CAP53InfrastructureTest(unittest.TestCase):
    def test_allowlist_and_only_direct_child_synthetic_files_are_present(self) -> None:
        root = ROOT / "fixtures/coverstory/CAP-53/corpus"
        manifest = json.loads((root / "manifest.json").read_text(encoding="utf-8"))
        self.assertEqual(["b.json", "a.json"], manifest["files"])
        for name in manifest["files"]:
            path = root / name
            row = json.loads(path.read_text(encoding="utf-8"))
            self.assertEqual(root.resolve(), path.resolve().parent)
            self.assertEqual("SYNTHETIC", row["evidenceStatus"])
            self.assertEqual("PUBLIC", row["classification"])


if __name__ == "__main__": unittest.main()
