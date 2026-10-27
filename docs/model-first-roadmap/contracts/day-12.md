# Day 12: Precision and recall

**Date:** 2026-11-07 (Asia/Kolkata)
**Engineering budget:** 120 minutes. DSA is separate and unchanged.
**Prerequisite:** Day 11 confusion matrix

## Learn

Threshold choices trade missed positives against false alarms

## Worked teaching example

Thresholding p>=0.50 gives [1,1,0,0]; thresholding at 0.75 gives [1,0,0,0]. Against labels [1,0,1,0], the first has TP/TN/FP/FN=1/1/1/1, precision .5 and recall .5; the second has 1/2/0/1, precision 1 and recall .5. Threshold changes trade false alarms against missed positives; neither threshold is universal.

**Source:** [3.3. Tuning the decision threshold for class prediction](https://scikit-learn.org/stable/modules/classification_threshold.html)

## Learner-owned AI/model work

Calculate precision/recall and exact confusion counts at thresholds 0.50 and 0.75

**Acceptance command:** `python3 -m unittest ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_12_undefined_metrics_remain_none ai.model_building.learner_acceptance.test_days_02_30.Day02To30Acceptance.test_day_12_two_thresholds_show_precision_recall_tradeoff`
**Acceptance:** Exact labels, counts and precision/recall match independent enumeration
**Negative control:** Undefined denominator must be explicit, not silently reported as 1.0; threshold comparison cannot reuse test data to tune


**Evidence category:** `dataset_hygiene_and_independent_evaluation`

## Evidence

Record exact command/result, changed AI/model paths, assistance, actual metrics and limitations; no completion is inferred.

The learner acceptance is expected to remain red while its explicit TODO is unimplemented. Instructor fixtures and tests do not count as learner work. Record attempts accurately; a calendar date does not confer completion.
