"""Offline CLI scaffold; writes one document only after full preparation succeeds."""
from __future__ import annotations

import argparse
import sys

from .contracts import prepare_dataset
from .errors import DatasetContractError
from .fixture_registry import FIXTURES, fixture_directory
from .fixture_io import canonical_json_bytes, read_fixture_file, strict_json_loads


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Prepare a registered synthetic StreamForge fixture")
    parser.add_argument("--fixture", required=True, choices=sorted(FIXTURES))
    args = parser.parse_args(argv)
    try:
        # Reject ambiguous duplicate JSON keys at the instructor-owned input
        # boundary, before any learner composition code can filter or emit data.
        raw_manifest = read_fixture_file(fixture_directory(args.fixture), "manifest.json")
        manifest = strict_json_loads(raw_manifest.decode("utf-8"))
        if not isinstance(manifest, dict):
            raise ValueError("fixture manifest must be a JSON object")
        document = prepare_dataset(args.fixture)
        output = canonical_json_bytes(document)
    except (DatasetContractError, ValueError, OSError, NotImplementedError) as exc:
        print(str(exc), file=sys.stderr)
        return 2
    sys.stdout.buffer.write(output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
