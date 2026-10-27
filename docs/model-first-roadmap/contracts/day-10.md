# Day 10: Majority and centroid baselines

**Date:** 2026-11-05 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Days 7–9

## Learn

A simple baseline can expose class imbalance and weak modeling

## Worked teaching example

A majority baseline predicts the most frequent training label (this contract breaks ties toward 0). A nearest-centroid classifier averages each class's training rows, then assigns a query to the closest centroid by squared Euclidean distance. Never use held-out rows to form the centroids.

**Source:** [NearestCentroid](https://scikit-learn.org/stable/modules/generated/sklearn.neighbors.NearestCentroid.html)

## Learner-owned AI/model work

Compare majority-class and nearest-centroid predictions; fit centroids only on training rows

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_10_majority_tie_breaks_to_zero ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_10_nearest_centroid_is_fit_on_training_rows`
**Acceptance:** Exact majority and centroid predictions match independently computed values
**Negative control:** A baseline that learns from held-out labels, breaks ties inconsistently, or mixes feature axes fails


**Evidence category:** `dataset_hygiene_and_independent_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
