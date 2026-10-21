"""Learner adversarial assertions for train sensitivity and bounded work."""
import unittest
from fractions import Fraction as F
from ai.streamforge.content_io import ensure_work_bound
from ai.streamforge.content_models import ProfileCounts, ProfileWeights
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
        self.assertEqual(F(1,3),F(1,3))
        self.assertEqual(F(-1,12),F(1,6)-F(1,4))

    def test_work_guard_rejects_truncation_boundary(self):
        self.assertEqual(2500,ensure_work_bound(50,50))
        with self.assertRaises(ValueError):ensure_work_bound(51,50)
