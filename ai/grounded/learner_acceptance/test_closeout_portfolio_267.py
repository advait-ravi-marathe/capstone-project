"""Learner acceptance for the real closeout files; instructor tests inject a witness."""
from __future__ import annotations

import subprocess
import unittest
import json
import tempfile
from types import SimpleNamespace
from unittest.mock import patch
from pathlib import Path
from ai.grounded import closeout_portfolio_267 as portfolio

ROOT = Path(__file__).resolve().parents[3]
PATHS = ("ai/grounded/enterprise_contract_253.py",
         "ai/grounded/learner_acceptance/test_enterprise_contract_253.py")


class PortfolioReceiptAcceptance(unittest.TestCase):
    def test_day268_committed_source_and_actual_file_bytes(self):
        source = subprocess.run(["git", "-C", str(ROOT), "rev-parse", "HEAD"],
                                check=True, capture_output=True, text=True).stdout.strip()
        receipt = portfolio.build_receipt(ROOT, source, PATHS)
        self.assertEqual(source, receipt["source_sha"])
        self.assertEqual([{"path": p, "sha256": __import__("hashlib").sha256((ROOT/p).read_bytes()).hexdigest()}
                          for p in PATHS], receipt["artifacts"])
        canonical = json.dumps(receipt["artifacts"], sort_keys=True, separators=(",", ":"), ensure_ascii=False).encode()
        self.assertEqual(__import__("hashlib").sha256(canonical).hexdigest(), receipt["artifact_manifest_sha256"])
        self.assertNotEqual(receipt["source_sha"], receipt["artifact_manifest_sha256"])
        self.assertFalse(receipt["learner_completion_awarded"])
        self.assertFalse(receipt["professional_ownership_claimed"])

    def test_day268_rejects_stale_duplicate_missing_and_unsafe_paths(self):
        source = subprocess.run(["git", "-C", str(ROOT), "rev-parse", "HEAD"],
                                check=True, capture_output=True, text=True).stdout.strip()
        for sha, paths in (("0"*40, PATHS), (source, (PATHS[0], PATHS[0])),
                           (source, ("ai/grounded/not-present.py",)),
                           (source, ("../outside",))):
            with self.subTest(sha=sha, paths=paths), self.assertRaises((ValueError, FileNotFoundError)):
                portfolio.build_receipt(ROOT, sha, paths)

    def test_day268_rejects_symlink_files_and_symlinked_parent_directories(self):
        source = subprocess.run(["git", "-C", str(ROOT), "rev-parse", "HEAD"],
                                check=True, capture_output=True, text=True).stdout.strip()
        with tempfile.TemporaryDirectory(prefix="receipt-symlink-", dir=ROOT) as tmp:
            scratch = Path(tmp)
            in_root_target = ROOT / PATHS[0]
            leaf_link = scratch / "leaf-link.py"
            leaf_link.symlink_to(in_root_target)
            linked_parent = scratch / "linked-parent"
            linked_parent.symlink_to(in_root_target.parent, target_is_directory=True)
            external_target = Path(tempfile.gettempdir()) / (scratch.name + "-outside.txt")
            try:
                external_target.write_text("outside evidence root", encoding="utf-8")
                external_link = scratch / "external-link.txt"
                external_link.symlink_to(external_target)
                for raw_path in (str(leaf_link.relative_to(ROOT)),
                                 str((linked_parent / in_root_target.name).relative_to(ROOT)),
                                 str(external_link.relative_to(ROOT))):
                    with self.subTest(path=raw_path), self.assertRaisesRegex(ValueError, "symlink"):
                        portfolio.build_receipt(ROOT, source, (raw_path,))
            finally:
                external_target.unlink(missing_ok=True)

    def test_day268_rejects_bytes_that_do_not_match_the_named_commit(self):
        source = subprocess.run(["git", "-C", str(ROOT), "rev-parse", "HEAD"],
                                check=True, capture_output=True, text=True).stdout.strip()
        with patch.object(portfolio.subprocess, "run", side_effect=(
                SimpleNamespace(stdout=source+"\n"), SimpleNamespace(stdout=b"different committed bytes"))):
            with self.assertRaisesRegex(ValueError, "differ from the selected source commit"):
                portfolio.build_receipt(ROOT, source, PATHS[:1])


if __name__ == "__main__":
    unittest.main()
