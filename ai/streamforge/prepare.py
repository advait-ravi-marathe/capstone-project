"""Offline CLI scaffold; writes one document only after full preparation succeeds."""
from __future__ import annotations

import argparse
import sys

from .contracts import prepare_dataset
from .errors import DatasetContractError
from .fixture_registry import FIXTURES
from .fixture_io import canonical_json_bytes


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Prepare a registered synthetic StreamForge fixture")
    parser.add_argument("--fixture", required=True, choices=sorted(FIXTURES))
    args = parser.parse_args(argv)
    try:
        document = prepare_dataset(args.fixture)
        output = canonical_json_bytes(document)
    except (DatasetContractError, ValueError, OSError, NotImplementedError) as exc:
        print(str(exc), file=sys.stderr)
        return 2
    sys.stdout.buffer.write(output)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
