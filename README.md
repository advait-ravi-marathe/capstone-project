# Start learning

Begin with the plain-language [Start here guide](docs/fde-v2/start-here.md). It shows one working example, then points to your first small Python task. The app, UI, API, fixtures and setup are supplied; you learn how they work and implement a small function at a time.

For the first 15 days, the daily budget is 60 minutes of DSA in the separate repository, 30 minutes of AWS learning, and 90 minutes of guided AI/Python practice. See the [day-by-day plan](docs/fde-v2/amendments/2026-10-30-beginner-first-15.md).

Your first command, which needs no model, API key or cloud account, is:

```sh
python -m ai.fde_v2.coverstory.day01_baseline
```

It searches fictional sample documents with ordinary keyword matching. Then open [Day 1](docs/fde-v2/days/day-01.md) and try `document_title` in the learner exercise module. The example is supplied-system behavior; it is not a claim that you have completed the task.

<details>
<summary>Full roadmap and developer setup</summary>

The full route is the [AWS FDE v2.4 roadmap](docs/fde-v2/README.md), with 270 core learning days and an additive production-ML extension through nominal Day 382. The assistant owns capstone frontend/backend plumbing; learner-owned DSA remains in its separate repository, and model/statistical/training work remains learner-owned. No learner progress is inferred from the authored scaffold.

For full local development, use Python 3.12 and pinned dependencies:

```sh
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --index-url https://download.pytorch.org/whl/cpu 'torch==2.14.1'
python -m pip install -r requirements-fde-v2-sprint1.txt -r requirements-fde-v2-sprint2.txt -r requirements-fde-v2-mcp.txt
npm --prefix frontend ci
```

```sh
python3 -m unittest discover -s scripts -p 'test_fde_v2_*.py'
python3 -m unittest scripts.test_external_track_boundary
python3 -m unittest discover -s ai/fde_v2/tests -p 'test_*.py'
cd frontend && npm test && npm run build
```

A reference-suite pass does not change learner status. Live model, cloud, customer, and production evidence remains separate.

</details>
