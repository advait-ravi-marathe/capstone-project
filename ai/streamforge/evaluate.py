"""Instructor-framed local evaluation CLI; learner composition begins on Day 179."""
from __future__ import annotations

import argparse
import sys
from typing import Any

from .errors import DatasetContractError
from .evaluation_adapter import read_fixture_manifest
from .fixture_io import canonical_json_bytes
from .fixture_registry import FIXTURES


def evaluate_fixture(fixture_name: str) -> dict[str, Any]:
    """Day 179 seam: compose the completed preparation and evaluation path."""
    raise NotImplementedError("Day 179 learner TODO: implement evaluate_fixture")


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Evaluate a registered synthetic StreamForge fixture offline")
    parser.add_argument("--fixture", required=True, choices=sorted(FIXTURES))
    args = parser.parse_args(argv)
    try:
        # Duplicate keys (including nested keys) are rejected before any
        # learner composition or prior preparation can emit success output.
        read_fixture_manifest(args.fixture)
        report = evaluate_fixture(args.fixture)
        output = canonical_json_bytes(report)
    except (DatasetContractError, ValueError, OSError, NotImplementedError) as exc:
        print(str(exc), file=sys.stderr)
        return 2
    sys.stdout.buffer.write(output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
