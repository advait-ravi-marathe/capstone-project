# Start here

You do **not** need to build the app. The app, fixtures, database setup and test runner are supplied. Your part is to learn Python and implement one small function at a time.

## Open the existing project

If the project is already open in VS Code, look in the left Explorer for the top-level `README.md` and `ai` folder. Choose **Terminal → New Terminal**; the new terminal should start inside the project folder. If it is not open, choose **File → Open Folder** and select the existing `capstone-project` folder (the one containing both `README.md` and `ai`). Do not create a new project or install packages for Day 1. If you cannot find the folder, stop and ask for the existing repository location instead of guessing a path.

In that terminal, check Python:

```sh
python3 --version
```

This project uses Python 3.12. If `python3` is missing or reports a different version, ask before changing your computer setup.

## First: see the supplied example

Open a terminal in the repository folder and run:

```sh
python3 -m ai.fde_v2.coverstory.day01_baseline
```

This searches ten fictional sample documents. You should see `northstar-deductible` ranked first. It is ordinary keyword search, not an AI model. No API key, model download, cloud account or frontend setup is needed for this first step.

## Then: try your first tiny function

Open [`ai/fde_v2/beginner_first15/learner.py`](../../ai/fde_v2/beginner_first15/learner.py). Find `document_title`. It has a `NotImplementedError` because this is your small task. A dictionary is a set of labeled values: in `{"title": "Appeals"}`, `"title"` is the label and `"Appeals"` is its value. Return the `"title"` value, or an empty string if the label is missing.

Worked example: `record.get("title", "")` looks up the title and supplies an empty-string default. The complete example and your different assigned input are in [Day 1](days/day-01.md).

Use `python3`, including on macOS; do not assume the command `python` exists.

Run the check:

```sh
python3 -m ai.fde_v2.beginner_first15.acceptance --day 1
```

Before you implement the function, the check says `NOT YET`. After a correct implementation, it prints `Day 01: PASS — learner behavior matches the expected result`. That only checks this tiny exercise; it does not mark a whole day, milestone or project complete.

## Your daily time

For the first 15 days, use 60 minutes for DSA in its separate repository, 30 minutes for AWS study, and 90 minutes for the guided AI/Python task. Each AI page names one short reading, a worked example, one small task and its exact check. The total is 180 minutes. Python documentation is the default; Udemy is an optional supplement only when the named lesson is already available to you. No purchase or enrollment is needed.

Start with [Day 1](days/day-01.md). For the rest of the route, use the [roadmap index](README.md). The accepted contracts and deeper technical detail remain available under the links on those pages when you need them.
