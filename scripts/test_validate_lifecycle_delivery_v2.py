import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

MODULE = Path(__file__).with_name("validate-lifecycle-delivery-v2.py")
spec = importlib.util.spec_from_file_location("delivery_validator", MODULE)
validator = importlib.util.module_from_spec(spec)
spec.loader.exec_module(validator)

class DeliveryValidatorTest(unittest.TestCase):
    def write(self, data):
        temp = tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False)
        json.dump(data, temp); temp.close(); self.addCleanup(Path(temp.name).unlink)
        return Path(temp.name)

    def test_blank_template_is_valid_and_not_complete(self):
        source = Path("docs/reboot/continuation-31-44/templates/lifecycle-delivery-v2.json")
        self.assertEqual([], validator.validate(source))

    def test_pass_requires_actual_and_evidence(self):
        data=json.loads(Path("docs/reboot/continuation-31-44/templates/lifecycle-delivery-v2.json").read_text())
        data["acceptance_cases"]=[{"id":"A","selector":"test#case","result":"passed"}]
        data["observations"]=[{"case_id":"test#case","verdict":"passed","actual":"","evidence_ref":""}]
        self.assertTrue(any("lacks actual" in msg for msg in validator.validate(self.write(data))))

    def test_duplicate_case_ids_rejected(self):
        data=json.loads(Path("docs/reboot/continuation-31-44/templates/lifecycle-delivery-v2.json").read_text())
        data["acceptance_cases"]=[{"id":"A","selector":"test#one"},{"id":"A","selector":"test#two"}]
        self.assertTrue(any("unique" in msg for msg in validator.validate(self.write(data))))

if __name__ == "__main__": unittest.main()
