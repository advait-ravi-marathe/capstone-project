"""Pinned local generation runtime. No network fallback or canned response."""
from __future__ import annotations

import hashlib
import json
import os
import time
from pathlib import Path

from .contracts import RuntimeUnavailable


MODEL_ID = "Qwen/Qwen2.5-0.5B-Instruct"
MODEL_REVISION = "7ae557604adf67be50417f59c2c2f167def9a775"
LOCK = json.loads((Path(__file__).with_name("model_lock.json")).read_text(encoding="utf-8"))


def _sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for block in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


class LocalGenerator:
    def __init__(self, torch, tokenizer, model, stopping_criteria_type, stopping_criteria_list_type,
                 timeout_seconds: float = 45.0):
        self.torch, self.tokenizer, self.model = torch, tokenizer, model
        self.model_id, self.model_revision = MODEL_ID, MODEL_REVISION
        self.deadline_type = _deadline_type(stopping_criteria_type)
        self.stopping_criteria_list_type = stopping_criteria_list_type
        if type(timeout_seconds) not in (int, float) or not 1 <= timeout_seconds <= 300:
            raise ValueError("generation timeout must be between 1 and 300 seconds")
        self.timeout_seconds = float(timeout_seconds)

    def generate_messages(self, messages: list[dict[str, str]], max_new_tokens: int = 160) -> tuple[str, float]:
        if (not isinstance(messages, list) or not messages
                or any(not isinstance(item, dict) or item.get("role") not in {"user", "system"}
                       or not isinstance(item.get("content"), str) for item in messages)):
            raise ValueError("messages must contain user/system text records")
        if type(max_new_tokens) is not int or not 1 <= max_new_tokens <= 256:
            raise ValueError("max_new_tokens must be an integer in [1, 256]")
        deadline = self.deadline_type(self.timeout_seconds)
        prompt = self.tokenizer.apply_chat_template(messages, tokenize=False, add_generation_prompt=True)
        inputs = self.tokenizer(prompt, return_tensors="pt", truncation=True, max_length=2048)
        deadline.raise_if_expired()
        started = time.perf_counter()
        with self.torch.inference_mode():
            output = self.model.generate(**inputs, max_new_tokens=max_new_tokens, do_sample=False,
                                         pad_token_id=self.tokenizer.eos_token_id,
                                         stopping_criteria=self.stopping_criteria_list_type([deadline]))
        if deadline.expired:
            raise TimeoutError(f"generation exceeded the {self.timeout_seconds:g}s step-aware deadline")
        elapsed_ms = (time.perf_counter() - started) * 1000
        new_tokens = output[0, inputs["input_ids"].shape[1]:]
        answer = self.tokenizer.decode(new_tokens, skip_special_tokens=True).strip()
        if not answer:
            raise RuntimeError("pinned local model returned empty generation")
        return answer, elapsed_ms


def _deadline_type(stopping_criteria_type):
    """Build a Transformers-compatible criterion; a single slow kernel may overrun."""
    class GenerationDeadline(stopping_criteria_type):
        def __init__(self, timeout_seconds: float):
            super().__init__()
            self._started = time.perf_counter()
            self._timeout = timeout_seconds
            self.expired = False

        def __call__(self, input_ids, scores, **kwargs):
            if time.perf_counter() - self._started >= self._timeout:
                self.expired = True
                return True
            return False

        def raise_if_expired(self):
            if time.perf_counter() - self._started >= self._timeout:
                self.expired = True
                raise TimeoutError(f"generation exceeded the {self._timeout:g}s deadline before decoding")

    return GenerationDeadline


class LocalModels:
    @classmethod
    def load(cls, root: Path | None = None) -> LocalGenerator:
        root = root or Path(os.environ.get("FDE_V2_RUNTIME_DIR", "~/.cache/fde-v2/models")).expanduser()
        model_dir = root / "generation"
        manifest_path = root / "runtime-manifest.json"
        if not manifest_path.is_file():
            raise RuntimeUnavailable(f"pinned local model is not installed under {root}")
        try:
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError) as exc:
            raise RuntimeUnavailable("local runtime manifest is unreadable") from exc
        if not isinstance(manifest, dict):
            raise RuntimeUnavailable("local runtime manifest must be a JSON object")
        record = manifest.get("generation", {})
        if not isinstance(record, dict):
            raise RuntimeUnavailable("local generation record must be a JSON object")
        expected_files = set(LOCK["generation"]["expected_files"])
        manifested_files = record.get("files")
        if (manifest.get("trust_remote_code") is not False
                or record.get("repository") != MODEL_ID or record.get("revision") != MODEL_REVISION
                or not isinstance(manifested_files, dict)
                or set(manifested_files) != expected_files):
            raise RuntimeUnavailable("runtime manifest does not match the pinned generation model")
        if model_dir.is_symlink() or not model_dir.is_dir():
            raise RuntimeUnavailable("pinned model directory is absent or is a symlink")
        base = model_dir.resolve()
        for relative, expected in manifested_files.items():
            try:
                candidate = model_dir / relative
                path = candidate.resolve(strict=True)
            except (OSError, TypeError, ValueError) as exc:
                raise RuntimeUnavailable(f"pinned model file missing or unreadable: {relative}") from exc
            if (candidate.is_symlink() or base not in path.parents or not path.is_file()
                    or not isinstance(expected, str) or _sha256(path) != expected):
                raise RuntimeUnavailable(f"pinned model file missing or checksum mismatch: {relative}")
        top_level = {item.name for item in model_dir.iterdir()}
        unexpected = top_level - expected_files - {".cache"}
        if unexpected:
            raise RuntimeUnavailable(f"unverified files beside pinned model are not allowed: {sorted(unexpected)}")
        cache_dir = model_dir / ".cache"
        if cache_dir.exists() and (cache_dir.is_symlink() or not cache_dir.is_dir()):
            raise RuntimeUnavailable("model download metadata cache is unsafe")
        try:
            import torch
            from transformers import AutoModelForCausalLM, AutoTokenizer, StoppingCriteria, StoppingCriteriaList
        except ImportError as exc:
            raise RuntimeUnavailable("install the pinned Sprint 1 Python requirements first") from exc
        torch.set_num_threads(min(4, os.cpu_count() or 1))
        tokenizer = AutoTokenizer.from_pretrained(model_dir, local_files_only=True, trust_remote_code=False)
        model = AutoModelForCausalLM.from_pretrained(model_dir, local_files_only=True, trust_remote_code=False)
        model.eval()
        timeout = float(os.environ.get("FDE_V2_GENERATION_TIMEOUT_SECONDS", "45"))
        return LocalGenerator(torch, tokenizer, model, StoppingCriteria, StoppingCriteriaList, timeout)


class LocalEmbeddings:
    """Load a user-provided, hash-manifested local SentenceTransformer only.

    This class never accepts a Hub name and sets local_files_only=True. The
    starter currently has no embedding weights, so the real-embedding gate is
    intentionally unavailable until an approved, independently verified
    artifact is supplied outside Git.
    """

    @staticmethod
    def _digest(path: Path) -> str:
        digest = hashlib.sha256()
        with path.open("rb") as handle:
            for block in iter(lambda: handle.read(1024 * 1024), b""):
                digest.update(block)
        return digest.hexdigest()

    @classmethod
    def load(cls, model_dir: Path, manifest_path: Path):
        model_dir = model_dir.expanduser()
        manifest_path = manifest_path.expanduser()
        if model_dir.is_symlink() or not model_dir.is_dir() or manifest_path.is_symlink() or not manifest_path.is_file():
            raise RuntimeUnavailable("verified local embedding model and manifest are not installed")
        try:
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError) as exc:
            raise RuntimeUnavailable("embedding runtime manifest is unreadable") from exc
        record = manifest.get("embedding") if isinstance(manifest, dict) else None
        if not isinstance(record, dict):
            raise RuntimeUnavailable("embedding manifest must contain an embedding object")
        repository = record.get("repository")
        revision = record.get("revision")
        license_name = record.get("license")
        dimension = record.get("dimension")
        normalize = record.get("normalize_embeddings")
        files = record.get("files")
        if (not isinstance(repository, str) or not repository.strip()
                or not isinstance(revision, str) or len(revision) != 40
                or any(ch not in "0123456789abcdef" for ch in revision.lower())
                or not isinstance(license_name, str) or not license_name.strip()
                or type(dimension) is not int or dimension < 1 or type(normalize) is not bool
                or not isinstance(files, dict) or not files):
            raise RuntimeUnavailable("embedding manifest metadata is incomplete")
        base = model_dir.resolve(strict=True)
        expected = set()
        for relative, checksum in files.items():
            if (not isinstance(relative, str) or not relative or Path(relative).is_absolute()
                    or ".." in Path(relative).parts or "" in Path(relative).parts):
                raise RuntimeUnavailable("embedding model file list contains an unsafe path")
            candidate = model_dir / relative
            try:
                resolved = candidate.resolve(strict=True)
            except (OSError, ValueError, TypeError) as exc:
                raise RuntimeUnavailable("embedding model file is missing") from exc
            if (candidate.is_symlink() or base not in resolved.parents
                    or not resolved.is_file() or not isinstance(checksum, str)
                    or len(checksum) != 64 or cls._digest(resolved) != checksum):
                raise RuntimeUnavailable("embedding model file failed path or SHA-256 validation")
            expected.add(relative)
        actual = set()
        for path in model_dir.rglob("*"):
            if path.is_symlink():
                raise RuntimeUnavailable("embedding model tree contains a symlink")
            if path.is_file():
                actual.add(path.relative_to(model_dir).as_posix())
        if actual != expected:
            raise RuntimeUnavailable("embedding model file manifest is incomplete or has extras")
        try:
            from sentence_transformers import SentenceTransformer
        except ImportError as exc:
            raise RuntimeUnavailable("install the pinned Sprint 2 Python requirements first") from exc
        try:
            model = SentenceTransformer(str(base), device="cpu", trust_remote_code=False,
                                        local_files_only=True, token=False)
        except Exception as exc:
            raise RuntimeUnavailable("local embedding model could not be loaded without network access") from exc
        model.eval()
        return _LocalEmbeddingEncoder(model, repository, revision, dimension, normalize, license_name)


class _LocalEmbeddingEncoder:
    def __init__(self, model, model_id: str, revision: str, dimension: int,
                 normalize_embeddings: bool, license_name: str):
        self.model, self.model_id, self.model_revision = model, model_id, revision
        self.dimension, self.normalize_embeddings, self.license = dimension, normalize_embeddings, license_name

    def encode(self, texts: list[str]) -> list[list[float]]:
        if not isinstance(texts, list) or not texts or any(not isinstance(t, str) or not t.strip() for t in texts):
            raise ValueError("embedding inputs must be a nonempty list of strings")
        result = self.model.encode(texts, convert_to_numpy=True,
                                   normalize_embeddings=self.normalize_embeddings,
                                   show_progress_bar=False, device="cpu")
        rows = result.tolist() if hasattr(result, "tolist") else result
        if not isinstance(rows, list) or any(not isinstance(row, list) for row in rows):
            raise RuntimeError("embedding runtime did not return a matrix")
        if len(rows) != len(texts) or any(len(row) != self.dimension for row in rows):
            raise RuntimeError("embedding runtime output shape differs from its verified manifest")
        return rows
