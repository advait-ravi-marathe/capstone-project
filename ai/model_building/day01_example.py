"""Instructor-owned Python/JSON and one-gradient-step worked example."""

from __future__ import annotations

import json
import math


def sigmoid(value: float) -> float:
    return 1.0 / (1.0 + math.exp(-value))


def loss(weights: list[float], examples: list[tuple[list[float], int]]) -> float:
    total = 0.0
    for features, label in examples:
        probability = sigmoid(weights[0] + sum(w * x for w, x in zip(weights[1:], features)))
        total -= label * math.log(probability) + (1 - label) * math.log(1 - probability)
    return total / len(examples)


def reference_step(weights: list[float], examples: list[tuple[list[float], int]], rate: float) -> list[float]:
    """Transparent instructor reference; learner code is in learner.py."""
    gradient = [0.0] * len(weights)
    for features, label in examples:
        prediction = sigmoid(weights[0] + sum(w * x for w, x in zip(weights[1:], features)))
        error = prediction - label
        gradient[0] += error
        for index, feature in enumerate(features, start=1):
            gradient[index] += error * feature
    scale = rate / len(examples)
    return [weight - scale * value for weight, value in zip(weights, gradient)]


def main() -> None:
    examples = [([1.0, 0.0], 1), ([0.0, 1.0], 0)]
    start = [0.0, 0.0, 0.0]
    updated = reference_step(start, examples, 0.2)
    report = {
        "features": ["mentions_waiting_period", "mentions_exclusion"],
        "labels": [label for _, label in examples],
        "weights_before": start,
        "weights_after_one_cpu_step": updated,
        "mean_loss_before": round(loss(start, examples), 6),
        "mean_loss_after": round(loss(updated, examples), 6),
        "warning": "Two synthetic rows demonstrate arithmetic; this is not a useful or validated policy model.",
    }
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
