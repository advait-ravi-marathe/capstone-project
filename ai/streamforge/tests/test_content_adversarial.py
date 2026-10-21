"""Learner adversarial assertions for train sensitivity and bounded work."""
import unittest
from unittest import mock
from fractions import Fraction as F
from ai.streamforge.content_io import ensure_work_bound
from ai.streamforge.content_models import ProfileCounts, ProfileWeights, PredictorState
from ai.streamforge.content_models import GENRES, MAX_SCORED_PAIRS
from ai.streamforge.content_io import rendered_protocol
from ai.streamforge.evaluation_models import HoldoutTargets
from ai.streamforge import content_contracts as seams

class Day195Test(unittest.TestCase):
    def test_same_candidates_training_change_changes_profile_ranking(self):
        # Literal same-candidate contrast; duplicate a replay is one distinct pair.
        features={"a":(0,0,0,1,0),"b":(0,0,0,1,0),"c":(0,1,0,0,0),"d":(0,0,1,0,0),"e":(1,0,0,0,0),"f":(0,0,0,0,1),"x":(0,1,0,0,0)}
        candidates=("a","b","c","d","e","f")
        base_pairs=(("v1","a"),("v1","a"),("v1","x"),("v1","c"))
        changed_pairs=(("v1","a"),("v1","a"),("v1","d"),("v1","d"))
        base_counts=seams.profile_counts(base_pairs,features,("v1",))
        changed_counts=seams.profile_counts(changed_pairs,features,("v1",))
        self.assertEqual(ProfileCounts((("v1",(0,2,0,1,0),3),)),base_counts)
        self.assertEqual(ProfileCounts((("v1",(0,0,1,1,0),2),)),changed_counts)
        base_weights=seams.normalize_profiles(base_counts);changed_weights=seams.normalize_profiles(changed_counts)
        global_order=("d","e","a","b","c","f")
        before=seams.rank_for_viewer("v1",base_weights,base_counts,candidates,features,global_order)
        after=seams.rank_for_viewer("v1",changed_weights,changed_counts,candidates,features,global_order)
        self.assertEqual(("c","a","b","d","e","f"),before.full_order)
        self.assertEqual(("a","b","d","c","e","f"),after.full_order)
        self.assertEqual((F(1,2),F(1,2),F(1,2)),tuple(dict(after.scores_by_media)[x] for x in ("a","b","d")))
        self.assertNotEqual(before.recommendation_ids,after.recommendation_ids)

    def test_literal_precision_and_signed_delta_positive_control(self):
        from ai.streamforge import content_contracts as seams
        from ai.streamforge.content_models import ProfileEvaluation, QueryPrediction, ViewerMetric
        from ai.streamforge.evaluation_models import BaselineEvaluation, RankedList
        from ai.streamforge.content_io import validate_profile_evaluation
        from ai.streamforge.evaluation_io import validate_evaluation
        candidates=("a","b","c","d","e")
        query=QueryPrediction("v",True,1,"genre_profile",tuple((m,F(1) if m in {"a","b","c"} else F(0)) for m in candidates),candidates,("a","b","c"))
        row=ViewerMetric("v",("a","b","c"),3,3,0,F(1),F(1))
        counts=(("all_holdout_viewers",1),("evaluated_viewers",1),("zero_positive_viewers",0),("positive_viewer_media_pairs",3),("candidate_unreachable_positive_pairs",0),("training_viewer_count",1),("cold_start_evaluated_viewers",0),("known_zero_profile_evaluated_viewers",0),("positive_profile_evaluated_viewers",1),("candidate_count",5),("returned_ids_total",3))
        modes=(("genre_profile",1),("global_cold_start",0),("global_no_positive_profile",0),("no_candidates",0))
        profile=ProfileEvaluation(candidates,(query,),(row,),F(1),F(1),counts,modes,"available")
        validate_profile_evaluation(profile)
        ranked=RankedList(("d","e","c","a","b"),("d","e","c"),"popularity_ranked")
        global_eval=BaselineEvaluation(ranked,F(1,3),F(1,3),1,1,0,3,0,1,0,5,3,"available")
        validate_evaluation(global_eval,candidates)
        better=seams.compare_against_global(profile,global_eval)
        self.assertEqual((F(2,3),F(2,3)),(better.precision_delta,better.recall_delta))
        winning_truth=("c","d","e")
        losing_profile_row=ViewerMetric("v",winning_truth,1,3,0,F(1,3),F(1,3))
        losing_profile=ProfileEvaluation(candidates,(query,),(losing_profile_row,),F(1,3),F(1,3),counts,modes,"available")
        validate_profile_evaluation(losing_profile)
        winner=BaselineEvaluation(ranked,F(1),F(1),1,1,0,3,0,1,0,5,3,"available")
        validate_evaluation(winner,candidates)
        worse=seams.compare_against_global(losing_profile,winner)
        self.assertEqual((F(-2,3),F(-2,3)),(worse.precision_delta,worse.recall_delta))

    def test_work_guard_rejects_truncation_boundary(self):
        self.assertEqual(2500,ensure_work_bound(50,50))
        with self.assertRaises(ValueError):ensure_work_bound(51,50)

    def test_real_evaluator_checks_work_bound_before_any_viewer_rank(self):
        from ai.streamforge import content_contracts as seams
        candidates=tuple(f"m{i:02d}" for i in range(50))
        p=rendered_protocol("2027-01-01T00:00:00Z","2027-01-20T00:00:00Z","2027-02-01T00:00:00Z");p["genres"]=GENRES
        features=tuple((mid,(1,0,0,0,0)) for mid in candidates)
        state=PredictorState(tuple(sorted(p.items())),candidates,features,(),(),tuple((mid,0) for mid in candidates),candidates,candidates[:3],"id_order_no_positive_training_signal","0"*64)
        from ai.streamforge.content_io import validate_predictor_state
        validate_predictor_state(state)
        viewers=tuple(f"v{i:02d}" for i in range(50))
        targets=HoldoutTargets({v:frozenset((candidates[0],)) for v in viewers},frozenset(viewers),0,"available")
        original=seams.rank_for_viewer
        with mock.patch.object(seams,"rank_for_viewer",wraps=original) as ranked:
            result=seams.evaluate_profiles(state,targets)
            self.assertEqual(50,len(result.queries));self.assertEqual(50,ranked.call_count)
        more=tuple(f"v{i:02d}" for i in range(51))
        too_many=HoldoutTargets({v:frozenset((candidates[0],)) for v in more},frozenset(more),0,"available")
        with mock.patch.object(seams,"rank_for_viewer",side_effect=AssertionError("ranking ran before guard")) as ranked:
            with self.assertRaisesRegex(ValueError,"2550"):
                seams.evaluate_profiles(state,too_many)
            ranked.assert_not_called()
