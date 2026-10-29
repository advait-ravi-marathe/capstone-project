"""Deterministic synthetic fixtures; generated locally and never learner evidence."""
from __future__ import annotations
import random


def classification_fixture(rows: int = 160, seed: int = 2026):
    if type(rows) is not int or rows < 20: raise ValueError("at least 20 synthetic rows required")
    rng=random.Random(seed); result=[]
    for index in range(rows):
        x1=rng.uniform(-2,2); x2=rng.uniform(-2,2); noise=rng.uniform(-.5,.5)
        label=int(x1+0.65*x2+noise>0)
        result.append({"row_id":f"synthetic-{index:04d}","timestamp":index,
                       "features":[round(x1,6),round(x2,6)],"label":label})
    return result


def recommendation_fixture(users: int = 12, items: int = 10, positives_per_user: int = 4):
    if users < 4 or items < 6 or not 1 <= positives_per_user < items:
        raise ValueError("synthetic recommender fixture dimensions are too small")
    rows=[]; event=0
    for user_index in range(users):
        preferred={f"item-{(user_index+offset)%items:02d}" for offset in range(positives_per_user)}
        for item in sorted(preferred):
            rows.append({"event_id":f"e{event:05d}","user_id":f"user-{user_index:03d}",
                         "item_id":item,"timestamp":event,"event_kind":"synthetic_positive"})
            event+=1
    return rows


def experiment_fixture(units: int = 500):
    if type(units) is not int or units < 4: raise ValueError("at least four synthetic units required")
    return [f"synthetic-unit-{index:05d}" for index in range(units)]
