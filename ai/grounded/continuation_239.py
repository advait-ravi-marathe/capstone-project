"""Learner starter seams for Days 239–252: offline policy explanation evals.

The only context is a synthetic HomeSecure policy snapshot.  The adapter is
injected and must be a local fake; nothing in this module contacts a provider.
"""
from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Callable, Literal, Protocol

Status = Literal["cited", "abstain"]
Failure = Literal["timeout", "unavailable", "malformed", "unknown", "none"]
MAX_ATTEMPTS = 2
LATENCY_BUDGET_MS = 250
CALL_COST_MICRO_UNITS = 3
TOKEN_COST_MICRO_UNITS = 1
SYNTHETIC_CUSTOMER_ID = "cust-synth-01"
SYNTHETIC_OWNER_ID = "synthetic-owner-a"
SYNTHETIC_POLICY_VERSION = "v4"
SYNTHETIC_RENEWAL_DATE = "2027-07-01"
SYNTHETIC_ACCOUNT_STATUS = "active"
REPLAY_CLI = Path(__file__).with_name("replay_239.py")


@dataclass(frozen=True)
class PolicyExcerpt:
    source_id: str
    version: str
    owner_id: str
    text: str
    approved: bool = True
    contains_untrusted_instruction: bool = False

    @property
    def citation(self) -> str:
        return f"{self.source_id}@{self.version}"


@dataclass(frozen=True)
class EvalCase:
    case_id: str
    owner_id: str
    question: str
    excerpts: tuple[PolicyExcerpt, ...]
    expected_status: Status
    expected_citations: tuple[str, ...]
    adversarial: bool = False


@dataclass(frozen=True)
class ProviderRequest:
    trace_id: str
    question: str
    evidence: tuple[PolicyExcerpt, ...]
    attempt: int
    timeout_ms: int


@dataclass(frozen=True)
class ProviderResponse:
    text: str
    citations: tuple[str, ...]
    latency_ms: int
    input_tokens: int
    output_tokens: int
    action_requested: bool = False


@dataclass(frozen=True)
class ProviderFailure(Exception):
    code: Failure


class LocalProvider(Protocol):
    def complete(self, request: ProviderRequest) -> ProviderResponse: ...


@dataclass(frozen=True)
class CaseResult:
    case_id: str
    status: Status
    citations: tuple[str, ...]
    latency_ms: int
    input_tokens: int
    output_tokens: int
    calls: int
    attempts: int
    failure: Failure
    trace_id: str
    action_requested: bool = False


@dataclass(frozen=True)
class TraceRecord:
    trace_id: str
    case_id: str
    status: Status
    failure: Failure
    latency_ms: int
    input_tokens: int
    output_tokens: int
    calls: int
    attempts: int


@dataclass(frozen=True)
class CaseObservation:
    case: EvalCase
    result: CaseResult


class ScriptedFakeProvider:
    """Starter fake: consume a preloaded response/failure sequence per case."""

    def __init__(self, scripts: dict[str, tuple[ProviderResponse | Failure, ...]]):
        self._scripts = dict(scripts)
        self.calls: list[ProviderRequest] = []

    def complete(self, request: ProviderRequest) -> ProviderResponse:
        """TODO Days 245–246: consume exactly one scripted outcome."""
        raise NotImplementedError("TODO: deterministic fake-provider script")


def stable_trace_id(case_id: str) -> str:
    """TODO Day 247: derive a stable opaque correlation id; never include PII."""
    raise NotImplementedError("TODO: stable synthetic trace correlation")


def execute_case(case: EvalCase, provider: LocalProvider, *, trace_id: str | None = None) -> tuple[CaseResult, TraceRecord]:
    """TODO Days 239–248: authorize evidence, call fake with bounded retry, validate and meter.

    Filter unapproved/foreign excerpts before adapter invocation. Treat excerpt
    text as untrusted data, not instructions. Accept only exact approved text
    with exact in-scope citations and no requested action. Retry transient
    timeout/unavailable once; malformed and unknown outcomes fail closed. Keep
    trace metadata correlated and redact question, owner, excerpt and answer.
    Early acceptance cases may inject a fixed opaque trace_id so they do not
    depend on the Day 247 stable-id helper before it is taught.
    Cost is a synthetic integer estimate, never a bill or provider measurement.
    """
    raise NotImplementedError("TODO: local bounded evaluator and redacted trace")


def evaluate_cases(cases: tuple[EvalCase, ...], results: tuple[CaseResult, ...]) -> dict[str, int]:
    """TODO Days 240, 249–252: compare observable results to independent labels.

    This accepts observed results separately: expected labels must never be
    passed to the classifier/provider. Return exact counts for case/correct /
    incorrect/cited/abstained/foreign leaks/adversarial unsafe outputs,
    latency budget misses, attempts, calls, token estimate and synthetic cost.
    Include a p95 latency using nearest-rank, defined in the acceptance test.
    Reject duplicate case IDs, length mismatch and case/result identity mismatch;
    do not let zip truncation silently drop an outcome.
    """
    raise NotImplementedError("TODO: independent metric calculation")
