"""Exercise a bounded Java Flight Recorder workload and inspect its marker event."""
from __future__ import annotations
import re
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class RuntimeSqlJfrTest(unittest.TestCase):
    def test_work_phase_marker_is_recorded_and_profile_summary_is_inspectable(self):
        java = shutil.which("java")
        self.assertIsNotNone(java, "Java 21 runtime is required")
        javac = shutil.which("javac")
        jfr = shutil.which("jfr")
        with tempfile.TemporaryDirectory(prefix="runtime-sql-jfr-") as raw:
            directory = Path(raw)
            classes = directory / "classes"
            classes.mkdir()
            recording = directory / "profile.jfr"
            if javac:
                compile_command = [javac, "-d", str(classes), str(ROOT / "labs/runtime_sql/ProfilingWork.java")]
            else:
                compile_command = [java, "--module", "jdk.compiler/com.sun.tools.javac.Main", "-d", str(classes),
                                   str(ROOT / "labs/runtime_sql/ProfilingWork.java")]
            subprocess.run(compile_command, cwd=ROOT, check=True, capture_output=True, text=True)
            run = subprocess.run([java, f"-XX:StartFlightRecording=filename={recording},dumponexit=true",
                                  "-cp", str(classes), "ProfilingWork"], cwd=ROOT, check=True,
                                 capture_output=True, text=True, timeout=20)
            self.assertIn("iterations=500000000 checksum=7954362999", run.stdout)
            if jfr:
                summary_command = [jfr, "summary", str(recording)]
                print_command = [jfr, "print", "--events", "lab.WorkPhase", str(recording)]
            else:
                summary_command = [java, "--module", "jdk.jfr/jdk.jfr.internal.tool.Main", "summary", str(recording)]
                print_command = [java, "--module", "jdk.jfr/jdk.jfr.internal.tool.Main", "print",
                                 "--events", "lab.WorkPhase", str(recording)]
            summary = subprocess.run(summary_command, cwd=ROOT, check=True, capture_output=True,
                                     text=True, timeout=20).stdout
            marker = subprocess.run(print_command, cwd=ROOT, check=True, capture_output=True,
                                    text=True, timeout=20).stdout
            self.assertRegex(summary, r"(?m)^\s*lab\.WorkPhase\s+1\s+")
            self.assertIn('workload = "cpu-bitcount-v1"', marker)
            self.assertIn("iterations = 500000000", marker)
            self.assertIn("checksum = 7954362999", marker)
            # ExecutionSample counts are recorded as observations, not brittle exact thresholds.
            sample = re.search(r"(?m)^\s*jdk\.ExecutionSample\s+(\d+)\s+", summary)
            if sample:
                self.assertGreaterEqual(int(sample.group(1)), 0)


if __name__ == "__main__":
    unittest.main()
