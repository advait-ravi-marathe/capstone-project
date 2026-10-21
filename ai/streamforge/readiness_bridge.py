"""Five-minute Python syntax bridge used before the Day 155 data contract."""
from __future__ import annotations

import unittest


def only_synthetic(rows: list[dict[str, str]]) -> bool:
    for row in rows:
        try:
            if row["source"] != "synthetic":
                raise ValueError("source must be synthetic")
        except (KeyError, ValueError):
            return False
    return True


def main() -> None:
    checks = [{"source": "synthetic"}, {"source": "synthetic"}]
    check = unittest.TestCase()
    check.assertTrue(only_synthetic(checks))
    check.assertFalse(only_synthetic(checks + [{"source": "external"}]))
    print("Python readiness bridge passed")


if __name__ == "__main__":
    main()
