"""Fixed local fixture registry; never accepts arbitrary paths or URLs."""
from pathlib import Path

PACKAGE_DIR = Path(__file__).resolve().parent
FIXTURE_ROOT = PACKAGE_DIR / "fixtures"
FIXTURES = {"basic": FIXTURE_ROOT / "basic"}


def fixture_directory(name: str) -> Path:
    """Return only a named, checked-in fixture directory."""
    if name not in FIXTURES:
        raise ValueError(f"unknown fixture: {name}")
    return FIXTURES[name]
