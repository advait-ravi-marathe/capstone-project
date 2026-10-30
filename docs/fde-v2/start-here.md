# Start here

You do **not** need to build the app. The app, fixtures, database setup and test runner are supplied. Your part is to learn Python and implement one small function at a time.

## First: see the supplied example

Open a terminal in the repository folder and run:

```sh
python -m ai.fde_v2.coverstory.day01_baseline
```

This searches ten fictional sample documents. You should see `northstar-deductible` ranked first. It is ordinary keyword search, not an AI model. No API key, model download, cloud account or frontend setup is needed for this first step.

## Then: try your first tiny function

Open [`ai/fde_v2/beginner_first15/learner.py`](../../ai/fde_v2/beginner_first15/learner.py). Find `document_title`. It has a `NotImplementedError` because this is your small task. Make it return the `title` value from a dictionary, or an empty string when there is no title.

Worked example: `{"title": "Appeals"}` should produce `Appeals`. Your assigned example is in [Day 1](days/day-01.md).

Run the check:

```sh
python -m ai.fde_v2.beginner_first15.acceptance --day 1
```

Before you implement the function, the check says `NOT YET`. After a correct implementation, it prints `Day 01: PASS — learner behavior matches the expected result`. That only checks this tiny exercise; it does not mark a whole day, milestone or project complete.

## Your daily time

For the first 15 days, use 60 minutes for DSA in its separate repository, 30 minutes for AWS study, and 90 minutes for the guided AI/Python task. Each AI page names one short reading, a worked example, one small task and its exact check. The total is 180 minutes. Python documentation is the default; Udemy is an optional supplement only when the named lesson is already available to you. No purchase or enrollment is needed.

Start with [Day 1](days/day-01.md). For the rest of the route, use the [roadmap index](README.md). The accepted contracts and deeper technical detail remain available under the links on those pages when you need them.
