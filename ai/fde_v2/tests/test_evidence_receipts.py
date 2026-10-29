"""Receipt manifests verify bytes and coverage, never completion truth."""
import hashlib, json, tempfile, unittest
from pathlib import Path
from ai.fde_v2.evidence_receipts import SCHEMA, audit_artifact_receipt


class EvidenceReceiptTests(unittest.TestCase):
    def test_missing_receipt_path_returns_invalid_instead_of_raising(self):
        with tempfile.TemporaryDirectory() as td:
            got=audit_artifact_receipt(Path(td)/'missing.json','M',['required'])
            self.assertEqual(got['status'],'INVALID')
            self.assertEqual(got['missing_keys'],['required'])

    def test_hash_and_key_coverage_require_review_not_pass(self):
        with tempfile.TemporaryDirectory() as td:
            root=Path(td); artifact=root/'trace.json'; artifact.write_text('{"status":"local"}')
            receipt=root/'receipt.json'
            receipt.write_text(json.dumps({"schema":SCHEMA,"milestone":"M2.2","artifacts":[
                {"key":"route_trace","path":"trace.json","sha256":hashlib.sha256(artifact.read_bytes()).hexdigest()}]}))
            got=audit_artifact_receipt(receipt,'M2.2',['route_trace'])
            self.assertEqual(got['status'],'ARTIFACTS_PRESENT_REQUIRES_INDEPENDENT_REVIEW')
            self.assertFalse(got['milestone_pass'])
            self.assertEqual(audit_artifact_receipt(receipt,'M2.2',['route_trace','identity'])['status'],'BLOCKED')
            artifact.write_text('changed')
            self.assertEqual(audit_artifact_receipt(receipt,'M2.2')['status'],'INVALID')

    def test_traversal_and_symlink_receipts_are_rejected(self):
        with tempfile.TemporaryDirectory() as td, tempfile.TemporaryDirectory() as outside:
            root=Path(td); secret=Path(outside)/'secret'; secret.write_text('x')
            receipt=root/'receipt.json'
            (root/'link').symlink_to(secret)
            for relative in ('../secret','link'):
                data={"schema":SCHEMA,"milestone":"M","artifacts":[{"key":"x","path":relative,"sha256":hashlib.sha256(b'x').hexdigest()}]}
                receipt.write_text(json.dumps(data))
                self.assertEqual(audit_artifact_receipt(receipt,'M')['status'],'INVALID')


if __name__=='__main__': unittest.main()
