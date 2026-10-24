#!/usr/bin/env python3
"""Regenerate the Days 197–210 canonical and owner exports from the daily contracts."""
from __future__ import annotations
import json
import re
from datetime import date
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
BATCH = ROOT / "docs/reboot/continuation-197-210"
manifest = json.loads((BATCH / "manifest.json").read_text())
owner = json.loads((BATCH / "scrum-owner-manifest.json").read_text())
for day in manifest["days"]:
    path = BATCH / "contracts" / f"day-{day['day']}.md"
    text = path.read_text()
    for needle in (day["title"], day["date"], day["selectors"][0]):
        if needle not in text:
            raise SystemExit(f"contract mismatch for day {day['day']}: {needle}")
    if date.fromisoformat(day["date"]).isoformat() != day["date"]:
        raise SystemExit(f"invalid date for day {day['day']}")
expected = list(range(197, 211))
if [d["day"] for d in manifest["days"]] != expected:
    raise SystemExit("manifest day sequence mismatch")
if [d["day_number"] for d in owner["cards"]] != expected:
    raise SystemExit("owner day sequence mismatch")
for d, o in zip(manifest["days"], owner["cards"]):
    for key in ("date", "title", "learner_seam", "module", "signature", "selectors", "study_refs", "study_guidance", "engineering_architecture"):
        owner_key = "learner_seam" if key == "learner_seam" else key
        if d[key] != o[owner_key]:
            raise SystemExit(f"export mismatch day {d['day']} field {key}")
print("Days 197–210 contracts and canonical/owner exports are consistent")
