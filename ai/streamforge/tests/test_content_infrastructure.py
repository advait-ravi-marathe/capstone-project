"""Default-green supplied content-profile boundary checks."""
import unittest
from fractions import Fraction
from ai.streamforge.content_io import ensure_work_bound, rendered_protocol, validate_ratio, validate_profile_evaluation, validate_predictor_state, LIMITS
from ai.streamforge.content_models import GENRES, MAX_SCORED_PAIRS, PROFILE_PROTOCOL_ID, ProfileEvaluation, PredictorState

class ContentInfrastructureTest(unittest.TestCase):
    def test_fixed_policy_and_canonical_ratio_boundary(self):
        protocol=rendered_protocol("2027-01-01T00:00:00Z","2027-01-20T00:00:00Z","2027-02-01T00:00:00Z")
        self.assertEqual(PROFILE_PROTOCOL_ID,protocol["id"])
        self.assertEqual(3,protocol["k"])
        self.assertEqual(list(GENRES),protocol["genres"])
        self.assertEqual(8,len(LIMITS))
        for ratio in ("0/1","2/3","11/24"): validate_ratio(ratio)
        for bad in ("2/6","01/2","1/0","-1/2","1.0"):
            with self.subTest(bad=bad),self.assertRaises(ValueError):validate_ratio(bad)
    def test_exact_work_bound_rejects_without_truncation(self):
        self.assertEqual(MAX_SCORED_PAIRS,ensure_work_bound(50,50))
        with self.assertRaisesRegex(ValueError,"2550"):ensure_work_bound(51,50)
        with self.assertRaisesRegex(ValueError,"2501"):ensure_work_bound(2501,1)
        self.assertEqual(0,ensure_work_bound(2501,0))

    def test_available_evaluation_cannot_hide_null_metrics(self):
        counts=(("all_holdout_viewers",1),("evaluated_viewers",1),("zero_positive_viewers",0),("positive_viewer_media_pairs",1),("candidate_unreachable_positive_pairs",0),("training_viewer_count",1),("cold_start_evaluated_viewers",0),("known_zero_profile_evaluated_viewers",0),("positive_profile_evaluated_viewers",1),("candidate_count",1),("returned_ids_total",1))
        modes=(("genre_profile",1),("global_cold_start",0),("global_no_positive_profile",0),("no_candidates",0))
        invalid=ProfileEvaluation(("a",),(),(),None,None,counts,modes,"available")
        with self.assertRaisesRegex(ValueError,"both metrics"):
            validate_profile_evaluation(invalid)

    def test_frozen_predictor_rejects_nested_mutable_protocol_values(self):
        state=PredictorState((("genres",["animation"]),),(),(),(),(),(),(),(),"id_order_no_positive_training_signal","0"*64)
        with self.assertRaisesRegex(ValueError,"deeply immutable"):
            validate_predictor_state(state)
