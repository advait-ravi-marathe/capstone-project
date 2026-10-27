"""Learner-owned AI seams. These TODOs intentionally remain unimplemented."""


def build_policy_prompt(question: str, policy_excerpt: str) -> dict[str, str]:
    """Return the exact Responses API instructions/input pair required by Day 1."""
    raise NotImplementedError("Day 1 learner TODO: build the bounded policy prompt")


def build_responses_request(prompt: dict[str, str], model: str) -> dict:
    """Day 2: build a deterministic, bounded API request without sending it."""
    raise NotImplementedError("Day 2 learner TODO: build the Responses API request")


def extract_response_text(response: dict) -> str:
    """Day 4: extract completed output_text and reject other response states."""
    raise NotImplementedError("Day 4 learner TODO: extract completed output text")


def parse_structured_answer(raw: str) -> dict:
    """Day 5: parse and validate the exact answer/claims/citations/abstained schema."""
    raise NotImplementedError("Day 5 learner TODO: parse the structured answer")


def classify_provider_failure(status: int | None, error_kind: str) -> str:
    """Day 6: classify provider errors without exposing response content."""
    raise NotImplementedError("Day 6 learner TODO: classify provider failures")


def prompt_for_version(version: str) -> str:
    """Day 8: return a fixed, versioned prompt or reject unknown versions."""
    raise NotImplementedError("Day 8 learner TODO: select a prompt version")


def normalize_document(document: dict) -> dict:
    """Day 9: normalize whitespace while preserving source identity/version."""
    raise NotImplementedError("Day 9 learner TODO: normalize a document")


def chunk_document(document: dict, max_chars: int) -> list[dict]:
    """Day 10: split a normalized document into stable, source-bound chunks."""
    raise NotImplementedError("Day 10 learner TODO: chunk the document")


def lexical_search(query: str, chunks: list[dict], tenant_id: str, limit: int = 3) -> list[dict]:
    """Day 11: implement the deterministic lexical retrieval baseline."""
    raise NotImplementedError("Day 11 learner TODO: implement lexical retrieval")


def tiny_vector_score(query_vector: list[float], chunk_vector: list[float]) -> float:
    """Day 12: implement cosine similarity for tiny fixed vectors."""
    raise NotImplementedError("Day 12 learner TODO: implement cosine similarity")


def recall_at_k(ranked_ids: list[str], relevant_ids: set[str], k: int) -> dict:
    """Day 13: report fixed retrieval numerator, denominator, and recall@k."""
    raise NotImplementedError("Day 13 learner TODO: compute recall at k")


def build_citations(claims: list[dict], chunks: list[dict]) -> list[dict]:
    """Day 14: bind citations to exact retrieved chunk/document versions."""
    raise NotImplementedError("Day 14 learner TODO: build source citations")


def validate_citations(citations: list[dict], chunks: list[dict], tenant_id: str, version: str) -> bool:
    """Day 15: validate the entire citation set against authorized source rows."""
    raise NotImplementedError("Day 15 learner TODO: validate every citation")


def decide_abstention(answer: dict, citations: list[dict], threshold: float) -> dict:
    """Day 16: abstain when independent evidence is insufficient."""
    raise NotImplementedError("Day 16 learner TODO: decide whether to abstain")


def filter_authorized_chunks(principal: dict, chunks: list[dict]) -> list[dict]:
    """Day 17: filter by tenant/scope before ranking or prompt assembly."""
    raise NotImplementedError("Day 17 learner TODO: filter unauthorized chunks")


def denied_result(reason: str) -> dict:
    """Day 18: return a complete denial without answer or private metadata."""
    raise NotImplementedError("Day 18 learner TODO: construct a non-disclosing denial")


def assemble_grounded_input(question: str, excerpts: list[str]) -> dict[str, str]:
    """Day 19: keep fixed instructions separate from untrusted document text."""
    raise NotImplementedError("Day 19 learner TODO: separate instructions from excerpts")


def evaluate_cases(expected: list[dict], actual: list[dict]) -> list[dict]:
    """Day 21: evaluate actual outputs against independent fixed labels."""
    raise NotImplementedError("Day 21 learner TODO: evaluate fixed cases")


def summarize_metrics(records: list[dict]) -> dict:
    """Day 22: compute exact metrics with explicit denominators."""
    raise NotImplementedError("Day 22 learner TODO: summarize evaluation metrics")


def run_counterfactuals(candidate, fixed_cases: list[dict]) -> list[dict]:
    """Day 23: run controlled counterfactuals through the actual candidate path."""
    raise NotImplementedError("Day 23 learner TODO: detect counterfactual failures")


def make_safe_trace(trace: dict) -> dict:
    """Day 24: keep stable identifiers/version metadata and redact content/secrets."""
    raise NotImplementedError("Day 24 learner TODO: make a privacy-safe trace")


def usage_report(usage: dict | None) -> dict:
    """Day 25: distinguish observed usage from unknown usage."""
    raise NotImplementedError("Day 25 learner TODO: report provider usage honestly")


def integrated_report(cases: list[dict], outputs: list[dict], metadata: dict) -> dict:
    """Day 29: assemble a deterministic offline integrated result report."""
    raise NotImplementedError("Day 29 learner TODO: assemble the synthetic report")


def run_offline_pipeline(question: str, principal: dict, documents: list[dict], model_fixture) -> dict:
    """Day 29: compose retrieval, authorization, parsing, citation, and abstention seams."""
    raise NotImplementedError("Day 29 learner TODO: compose the offline grounded pipeline")


def closeout_report(evidence: dict) -> dict:
    """Day 30: keep code, understanding, offline, and live evidence separate."""
    raise NotImplementedError("Day 30 learner TODO: summarize evidence without upgrading status")
