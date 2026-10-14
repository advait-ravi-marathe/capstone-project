from __future__ import annotations

import asyncio
import json
import unittest
from pathlib import Path

from ai.coverstory.cap_62 import LookupStatus, bounded_lookup

FIXTURE = Path(__file__).resolve().parents[3] / "fixtures/coverstory/CAP-62/synthetic-v1.json"


class CAP62AcceptanceTest(unittest.IsolatedAsyncioTestCase):
    async def test_completes_inside_deadline_with_exact_result(self) -> None:
        outcome = await bounded_lookup(lambda: asyncio.sleep(0.020, result=["hit-a"]), 0.100)
        self.assertEqual((LookupStatus.OK, ["hit-a"]), (outcome.status, outcome.value))

    async def test_timeout_cancels_operation_and_returns_empty_timeout(self) -> None:
        started, cleaned = asyncio.Event(), asyncio.Event()
        async def never_finishes():
            started.set()
            try:
                await asyncio.Future()
            finally:
                cleaned.set()
        outcome = await bounded_lookup(never_finishes, 0.050)
        await asyncio.wait_for(cleaned.wait(), timeout=0.2)
        self.assertEqual(LookupStatus.TIMEOUT, outcome.status)
        self.assertIsNone(outcome.value)
        self.assertTrue(started.is_set())

    async def test_caller_cancellation_propagates_and_cleans_worker(self) -> None:
        started, cleaned = asyncio.Event(), asyncio.Event()
        async def never_finishes():
            started.set()
            try:
                await asyncio.Future()
            finally:
                cleaned.set()
        caller = asyncio.create_task(bounded_lookup(never_finishes, 5))
        # If the learner seam is still untouched, surface its explicit TODO
        # immediately instead of misreporting the resulting missing event as
        # a broken cancellation fixture.
        await asyncio.sleep(0)
        if caller.done():
            await caller
        await asyncio.wait_for(started.wait(), timeout=0.2)
        caller.cancel()
        with self.assertRaises(asyncio.CancelledError):
            await caller
        await asyncio.wait_for(cleaned.wait(), timeout=0.2)

    async def test_late_completion_does_not_replace_timeout(self) -> None:
        late_done = asyncio.Event()
        async def ignores_first_cancel():
            try:
                await asyncio.Future()
            except asyncio.CancelledError:
                await asyncio.sleep(0.010)
                late_done.set()
                return ["late-hit"]
        outcome = await bounded_lookup(ignores_first_cancel, 0.005)
        await asyncio.wait_for(late_done.wait(), timeout=0.2)
        self.assertEqual((LookupStatus.TIMEOUT, None), (outcome.status, outcome.value))

    async def test_zero_timeout_rejected_without_invoking_operation(self) -> None:
        calls = 0
        def operation():
            nonlocal calls
            calls += 1
            return asyncio.sleep(0)
        with self.assertRaises(ValueError):
            await bounded_lookup(operation, 0)
        self.assertEqual(0, calls)

    async def test_fixture_is_synthetic_and_has_five_saved_case_ids(self) -> None:
        data = json.loads(FIXTURE.read_text(encoding="utf-8"))
        self.assertTrue(data["synthetic"])
        self.assertEqual([f"CAP-62-CS-{i:02d}" for i in range(1, 6)], [row["id"] for row in data["cases"]])


if __name__ == "__main__":
    unittest.main()
