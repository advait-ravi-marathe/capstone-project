from __future__ import annotations

import json
import unittest
from pathlib import Path

from materialize_assignment_harnesses import OUT, PARTITION_SIZE, partition_rows


class AssignmentHarnessMatrixTest(unittest.TestCase):
    def test_partitioning_preserves_order_and_bounds_each_file(self) -> None:
        rows = [{"day_number": day} for day in range(1, 104)]
        parts = partition_rows(rows, 50)
        self.assertEqual([(1, 50), (51, 100), (101, 103)],
                         [(first, last) for first, last, _ in parts])
        self.assertEqual(rows, [row for _, _, chunk in parts for row in chunk])
        self.assertTrue(all(len(chunk) <= 50 for _, _, chunk in parts))
        with self.assertRaises(ValueError):
            partition_rows(rows, 0)

    def test_generated_index_and_partitions_preserve_all_mapped_days(self) -> None:
        root = OUT.parent
        index = json.loads(OUT.read_text(encoding="utf-8"))
        self.assertEqual("coverstory-harness-matrix-index-v1", index["format"])
        self.assertEqual(PARTITION_SIZE, index["partition_size_days"])
        rows = []
        for part in index["partitions"]:
            payload = json.loads((root / part["path"]).read_text(encoding="utf-8"))
            self.assertEqual(index["source_curriculum_sha256"], payload["source_curriculum_sha256"])
            self.assertEqual(part["first_day"], payload["first_day"])
            self.assertEqual(part["last_day"], payload["last_day"])
            self.assertLessEqual(len(payload["assignments"]), PARTITION_SIZE)
            rows.extend(payload["assignments"])
        self.assertEqual(594, len(rows))
        self.assertEqual(list(range(1, 595)), [row["day_number"] for row in rows])
        self.assertEqual(594, len({row["card_id"] for row in rows}))
        first_twenty = rows[:20]
        self.assertEqual(list(range(1, 21)), [row["day_number"] for row in first_twenty])
        self.assertTrue(all(row["instructor_scaffold_readiness"] == "READY_TO_START" for row in first_twenty))
        self.assertTrue(all(row["assignability"] == "READY_TO_ATTEMPT" for row in first_twenty))
        self.assertTrue(all(row["instructor_readiness_evidence"] for row in first_twenty))
        self.assertTrue(all(row["timebox_status"] in {"TIMEBOX_NOT_VALIDATED", "NOT_REHEARSED"}
                            for row in first_twenty), "readiness must not invent measured time")


if __name__ == "__main__":
    unittest.main()
