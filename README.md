# Capstone roadmap

The active curriculum is [AWS FDE v2.4](docs/fde-v2/README.md): 270 learning days across 12 milestones, with separate DSA, role/AWS, and AI project blocks. Learner progress remains **Not started** until supported by actual learner evidence.

Start with the [Day 1 guide](docs/fde-v2/days/day-01.md). The runnable application is a local Python/FastAPI service and React UI. The baseline command is:

```sh
python -m ai.fde_v2.coverstory.day01_baseline
```

This is an instructor-owned synthetic keyword baseline, not generated output or proof of learner capability. See [runtime status](docs/fde-v2/runtime-verification.md) for the recorded model-access blocker. No live cloud, hosted-model, customer, or production result is claimed.

The repository keeps the DSA repository separate; it does not copy DSA problems or solutions here. The two current FDE v2 scaffold-review receipts remain under `evidence/fde-v2/`. Legacy unattempted placeholders, instructor smoke receipts, and standalone CAP assignments were removed from the active tree after the cleanup audit found no completed learner-attempt evidence; their Git history remains available. Superseded Java-first and model-building-first curriculum is removed from the active tree; the accepted contracts, attribution, and review records for the current roadmap remain under `docs/fde-v2/`.

## Local setup and checks

Use Python 3.12 and install the pinned local dependencies in a virtual environment. The PyTorch CPU index avoids a GPU build; these commands do not fetch model weights or contact a model provider.

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

Some integration/evidence gates intentionally remain blocked until the learner supplies the required local capability or separately authorized external access. A reference-suite pass does not change learner status.
