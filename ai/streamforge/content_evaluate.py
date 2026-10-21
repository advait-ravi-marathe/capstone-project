"""Instructor-framed content-profile composition CLI; Day 194 is a learner seam."""
from __future__ import annotations

import argparse
import sys
from typing import Any

from .errors import DatasetContractError
from .evaluation_adapter import prepare_evaluation_input, read_fixture_manifest
from .fixture_io import canonical_json_bytes
from .fixture_registry import FIXTURES


def evaluate_content_fixture(fixture_name: str) -> dict[str, Any]:
    raise NotImplementedError("Day 194 learner TODO: implement evaluate_content_fixture")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Run the registered offline genre-profile experiment")
    parser.add_argument("--fixture", required=True, choices=sorted(FIXTURES))
    args = parser.parse_args(argv)
    try:
        # Strict preflight rejects duplicate manifest keys before any learner seam.
        read_fixture_manifest(args.fixture)
        report = evaluate_content_fixture(args.fixture)
        output = canonical_json_bytes(report)
    except (DatasetContractError, ValueError, OSError, NotImplementedError) as exc:
        print(str(exc), file=sys.stderr)
        return 2
    sys.stdout.buffer.write(output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
