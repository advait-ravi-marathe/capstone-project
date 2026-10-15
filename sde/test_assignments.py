"""Acceptance contracts. Failures at NotImplementedError are intentional until learner work."""
import json, unittest
from pathlib import Path
from sde import assignments

FIXTURES = json.loads((Path(__file__).parent.parent / "fixtures/cases.json").read_text())

def make_test(fn, case):
    def test(self): self.assertEqual(fn(case["input"]), case["expected"])
    return test

for day in range(1, 31):
    fn = getattr(assignments, f"day_{day:02d}")
    for index, case in enumerate(FIXTURES[f"sde_{day:02d}"]["cases"], 1):
        setattr(globals().setdefault(f"TestDay{day:02d}", type(f"TestDay{day:02d}", (unittest.TestCase,), {})), f"test_case_{index:02d}", make_test(fn, case))

if __name__ == "__main__": unittest.main()
