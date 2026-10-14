"""Bounded async lookup seam; outcome policy remains learner-owned."""
from __future__ import annotations

import asyncio
from dataclasses import dataclass
from enum import Enum
from typing import Awaitable, Callable, Generic, TypeVar

T = TypeVar("T")


class LookupStatus(str, Enum):
    OK = "OK"
    TIMEOUT = "TIMEOUT"


@dataclass(frozen=True)
class LookupOutcome(Generic[T]):
    status: LookupStatus
    value: T | None = None


def _consume_late_result(task: asyncio.Task[object]) -> None:
    """Consume detached task errors after timeout/caller cancellation."""
    if not task.cancelled():
        task.exception()


async def bounded_lookup(operation: Callable[[], Awaitable[T]], timeout_seconds: float) -> LookupOutcome[T]:
    # LEARNER TODO: reject non-positive bounds before invoking operation,
    # preserve caller cancellation, and make the deadline terminal even when
    # a cancelled operation completes late.
    raise NotImplementedError("CAP-62 learner TODO: implement bounded_lookup")
