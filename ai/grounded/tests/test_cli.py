import json
import subprocess
import sys
import unittest
from ai.grounded.cli import report_bytes

class CliTests(unittest.TestCase):
    def test_fresh_process_report_is_canonical_and_repeatable(self):
        outputs=[]
        for _ in range(2):
            proc=subprocess.run([sys.executable,"-m","ai.grounded.cli"],capture_output=True,timeout=10,check=False)
            self.assertEqual(0,proc.returncode,proc.stderr.decode())
            outputs.append(proc.stdout)
        self.assertEqual(outputs[0],outputs[1])
        self.assertEqual(report_bytes(),outputs[0])
        parsed=json.loads(outputs[0])
        self.assertEqual({"total":3,"cited":2,"abstained":1,"exact_match":3,"citation_mismatch":0},parsed)
        self.assertTrue(outputs[0].endswith(b"\n"))
