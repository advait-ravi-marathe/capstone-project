import unittest
from scripts.fde_v2_query_plan_lab import run_lab

class QueryPlanLabTest(unittest.TestCase):
    def test_composite_index_changes_observed_plan_not_results(self):
        result=run_lab()
        self.assertEqual(result['fixture'],'synthetic_in_memory_only')
        self.assertTrue(result['uses_named_index'])
        self.assertTrue(result['same_results_before_after_index'])
        self.assertTrue(result['tenant_filter_enforced'])
        self.assertEqual(result['returned_rows'],10)
        self.assertFalse(result['elapsed_performance_claim'])

    def test_seeded_missing_tenant_filter_exposes_cross_tenant_rows(self):
        result=run_lab('missing-tenant-filter')
        self.assertFalse(result['tenant_filter_enforced'])
        self.assertEqual(result['returned_rows'],10)
        self.assertGreater(result['cross_tenant_rows'],0)
        self.assertIsNone(result['expected_tenant_rows'])
        self.assertFalse(result['elapsed_performance_claim'])

if __name__=='__main__': unittest.main()
