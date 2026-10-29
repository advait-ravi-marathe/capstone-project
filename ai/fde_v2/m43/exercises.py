"""Original, bounded M4.3 scratch exercises and executable reference controls.

Fixtures are instructor mechanics only. Running these controls never produces
learner-attempt, peer-review, or professional-experience evidence.
"""
from __future__ import annotations

import hashlib
import os
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path
from typing import Any


EXERCISES = {
    "A": {
        "source": "def total(values):\n    return sum(values[:-1])\n",
        "test": (
            "import unittest\nfrom exercise import total\n"
            "class TotalTests(unittest.TestCase):\n"
            " def test_multiple_values(self): self.assertEqual(total([2,3,5]),10)\n"
            " def test_one_value(self): self.assertEqual(total([4]),4)\n"
            " def test_empty(self): self.assertEqual(total([]),0)\n"
        ),
        "expected_source": "def total(values):\n    return sum(values)\n",
        "root_defect": "last-element-excluded",
    },
    "B": {
        "source": "def display_name(user):\n    return user.get('name', '').strip()\n",
        "test": (
            "import unittest\nfrom exercise import display_name\n"
            "class DisplayNameTests(unittest.TestCase):\n"
            " def test_contract_field(self): self.assertEqual(display_name({'display_name':'  Ada  '}),'Ada')\n"
            " def test_empty(self): self.assertEqual(display_name({'display_name':' '}),'')\n"
        ),
        "expected_source": "def display_name(user):\n    return user.get('display_name', '').strip()\n",
        "root_defect": "wrong-contract-key",
    },
}
WORKED = {
    "source": "def total_label(value):\n    return f'total: {value}'\n",
    "test": (
        "import unittest\nfrom example import total_label\n"
        "class WorkedTests(unittest.TestCase):\n"
        " def test_label_contract(self): self.assertEqual(total_label(5),'Total: 5')\n"
    ),
    "expected_source": "def total_label(value):\n    return f'Total: {value}'\n",
}


def _sha256(raw: bytes) -> str:
    return hashlib.sha256(raw).hexdigest()


def _assert_empty_directory(root: Path) -> None:
    if root.is_symlink():
        raise PermissionError("exercise workspace cannot be a symlink")
    root.mkdir(parents=True, exist_ok=True)
    if not root.is_dir() or any(root.iterdir()):
        raise PermissionError("exercise setup requires a new empty scratch directory")


def _safe_root(root: str | Path) -> Path:
    supplied=Path(root)
    if supplied.is_symlink():
        raise PermissionError("exercise workspace cannot be a symlink")
    resolved=supplied.resolve(strict=True)
    if not resolved.is_dir():
        raise PermissionError("exercise workspace must be a directory")
    return resolved


def materialize_exercises(root: str | Path) -> dict[str, Any]:
    """Create A/B and unrelated worked example in a new owned scratch folder."""
    base=Path(root)
    _assert_empty_directory(base)
    manifest={"classification":"instructor_fixture_only","exercises":{},"worked_example":{}}
    for exercise_id,spec in EXERCISES.items():
        folder=base/"exercises"/exercise_id
        folder.mkdir(parents=True)
        source=folder/"exercise.py"; test=folder/"test_exercise.py"
        source.write_text(spec["source"],encoding="utf-8")
        test.write_text(spec["test"],encoding="utf-8")
        manifest["exercises"][exercise_id]={"root_defect":spec["root_defect"],
            "initial_source_sha256":_sha256(source.read_bytes()),
            "test_sha256":_sha256(test.read_bytes()),"source_path":str(source.relative_to(base))}
    folder=base/"worked-example"; folder.mkdir()
    source=folder/"example.py"; test=folder/"test_example.py"
    source.write_text(WORKED["source"],encoding="utf-8")
    test.write_text(WORKED["test"],encoding="utf-8")
    manifest["worked_example"]={"initial_source_sha256":_sha256(source.read_bytes()),
        "test_sha256":_sha256(test.read_bytes()),"source_path":str(source.relative_to(base))}
    return manifest


def _run_tests(folder: Path, module: str) -> dict[str, Any]:
    if folder.is_symlink() or not folder.resolve(strict=True).is_dir():
        raise PermissionError("test workspace must be a real directory")
    command=[sys.executable,"-m","unittest","-v",module]
    result=subprocess.run(command,cwd=folder,capture_output=True,text=True,timeout=20,
        env={**os.environ,"PYTHONDONTWRITEBYTECODE":"1"})
    output=result.stdout+result.stderr
    return {"command":command,"returncode":result.returncode,
        "status":"PASS" if result.returncode==0 else "FAIL",
        "output_sha256":_sha256(output.encode()),
        "output_tail":output[-3000:]}


def run_exercise(root: str | Path, exercise_id: str) -> dict[str, Any]:
    """Run actual unittest source in the selected scratch exercise directory."""
    if exercise_id not in EXERCISES:
        raise ValueError("exercise must be A or B")
    base=_safe_root(root)
    folder=base/"exercises"/exercise_id
    if folder.is_symlink() or not folder.resolve(strict=True).is_relative_to(base):
        raise PermissionError("exercise path escaped the scratch root")
    source=folder/"exercise.py"
    if source.is_symlink() or not source.resolve(strict=True).is_relative_to(base):
        raise PermissionError("exercise source escaped the scratch root")
    result=_run_tests(folder,"test_exercise")
    result.update({"exercise_id":exercise_id,"source_sha256":_sha256(source.read_bytes()),
                   "classification":"fixture_execution_not_learner_attempt"})
    return result


def run_reference_controls(root: str | Path) -> dict[str, Any]:
    """Execute red/green controls on disposable copies, never learner sources."""
    base=_safe_root(root)
    result={"classification":"instructor_fixture_only","exercises":{}}
    for exercise_id,spec in EXERCISES.items():
        original=base/"exercises"/exercise_id
        observed=_run_tests(original,"test_exercise")
        if observed["returncode"]==0:
            raise AssertionError(f"exercise {exercise_id} was expected to fail before the learner attempt")
        with tempfile.TemporaryDirectory(prefix=f"m43-reference-{exercise_id}-") as tmp:
            copy=Path(tmp)/exercise_id
            shutil.copytree(original,copy)
            source=copy/"exercise.py"
            current=source.read_text(encoding="utf-8")
            if current!=spec["source"]:
                raise ValueError(f"reference fixture {exercise_id} no longer matches the published baseline")
            source.write_text(spec["expected_source"],encoding="utf-8")
            green=_run_tests(copy,"test_exercise")
            if green["returncode"]!=0:
                raise AssertionError(f"exercise {exercise_id} reference control did not pass")
        result["exercises"][exercise_id]={"red":observed,"green_reference_copy":green,
            "initial_source_sha256":_sha256((original/"exercise.py").read_bytes()),
            "reference_patch_sha256":_sha256(spec["expected_source"].encode()),
            "learner_source_modified":False}
    worked=base/"worked-example"
    red=_run_tests(worked,"test_example")
    if red["returncode"]==0: raise AssertionError("unrelated worked example must first fail")
    with tempfile.TemporaryDirectory(prefix="m43-worked-example-") as tmp:
        copy=Path(tmp)/"worked-example"; shutil.copytree(worked,copy)
        source=copy/"example.py"
        if source.read_text(encoding="utf-8")!=WORKED["source"]:
            raise ValueError("worked example baseline changed")
        source.write_text(WORKED["expected_source"],encoding="utf-8")
        green=_run_tests(copy,"test_example")
        if green["returncode"]!=0: raise AssertionError("worked example reference patch did not pass")
    result["worked_example"]={"red":red,"green_reference_copy":green,"unrelated_to_exercises":True}
    result["status"]="MECHANICS_PASS"
    result["learner_completion"]=False
    return result


def run_clean_copy(root: str | Path, exercise_id: str) -> dict[str, Any]:
    """Copy only fixture source into an empty workspace and execute its tests."""
    if exercise_id not in EXERCISES:
        raise ValueError("exercise must be A or B")
    base=_safe_root(root)
    source=base/"exercises"/exercise_id
    if source.is_symlink() or not source.resolve(strict=True).is_relative_to(base):
        raise PermissionError("exercise escaped scratch root")
    with tempfile.TemporaryDirectory(prefix="m43-clean-copy-") as tmp:
        target=Path(tmp)/"clean"
        shutil.copytree(source,target)
        if set(p.name for p in target.iterdir())!={"exercise.py","test_exercise.py"}:
            raise AssertionError("clean copy contains unexpected files")
        result=_run_tests(target,"test_exercise")
        result.update({"classification":"clean_copy_fixture_not_peer_handoff",
                       "workspace_empty_before_copy":True,
                       "source_files":["exercise.py","test_exercise.py"]})
        return result


def run_attempt_tests(root: str | Path, exercise_id: str, *,
                      expected_initial_sha256: str) -> dict[str, Any]:
    """Run current attempt tests and bind result to the actual source bytes.

    This records mechanics only. The real attempt date, authorship and scores
    must be supplied by a learner/reviewer through the evidence workflow.
    """
    if exercise_id not in EXERCISES:
        raise ValueError("exercise must be A or B")
    base=_safe_root(root)
    folder=base/"exercises"/exercise_id
    source=folder/"exercise.py"
    if folder.is_symlink() or source.is_symlink() or not source.resolve(strict=True).is_relative_to(base):
        raise PermissionError("attempt source escaped scratch root")
    if not isinstance(expected_initial_sha256,str) or len(expected_initial_sha256)!=64:
        raise ValueError("the instructor manifest source hash is required")
    result=_run_tests(folder,"test_exercise")
    result.update({"exercise_id":exercise_id,
        "current_source_sha256":_sha256(source.read_bytes()),
        "source_changed_from_initial":_sha256(source.read_bytes())!=expected_initial_sha256,
        "classification":"attempt_test_receipt_requires_learner_provenance"})
    return result
