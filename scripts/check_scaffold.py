#!/usr/bin/env python3
"""Fast static checks for instructor-owned module and data boundaries."""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
MODULES = {"catalog", "shortlist", "application", "payments", "policy", "support", "audit", "analytics"}


class ModuleBoundaryChecks(unittest.TestCase):
    def test_modules_do_not_import_another_modules_persistence(self):
        source_root = ROOT / "src/main/java/coverstory/modules"
        violations = []
        for source in source_root.rglob("*.java"):
            owner = source.relative_to(source_root).parts[0]
            imports = re.findall(r"^import\s+(coverstory\.modules\.([a-z]+)\.[^;]+);", source.read_text(), re.M)
            for imported, target in imports:
                if target != owner and ".persistence." in imported:
                    violations.append(f"{source.relative_to(ROOT)} imports {imported}")
        self.assertEqual([], violations)

    def test_each_bounded_context_has_an_explicit_package(self):
        source_root = ROOT / "src/main/java/coverstory/modules"
        found = {path.parent.name for path in source_root.rglob("*.java")}
        self.assertTrue(MODULES.issubset(found), f"Missing module packages: {MODULES - found}")


class DataBoundaryChecks(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.ddl = (ROOT / "src/main/resources/db/schema/constraints.sql").read_text().lower()

    def test_payments_use_minor_units_and_durable_unique_idempotency(self):
        self.assertIn("amount_minor bigint", self.ddl)
        self.assertIn("unique (owner_id, idempotency_key_hash)", self.ddl)
        self.assertIn("payment_journal_append_only", self.ddl)

    def test_analytics_are_allowlisted_and_have_no_arbitrary_properties(self):
        analytics = self.ddl.split("create table if not exists analytics.behavior_event", 1)[1]
        self.assertIn("check (event_name in", analytics)
        self.assertNotIn("properties", analytics)
        self.assertNotIn("raw_url", analytics)
        self.assertNotIn("health", analytics)
        self.assertNotIn("token", analytics)

    def test_business_audit_is_separate_from_click_analytics(self):
        self.assertIn("business_audit.event", self.ddl)
        self.assertIn("analytics.behavior_event", self.ddl)
        self.assertIn("aggregate_type", self.ddl)


if __name__ == "__main__":
    unittest.main()
