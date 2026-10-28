# Sprint 1 model runtime verification

**Status: real local generation is blocked and unverified.** No weights were transferred or committed. Deterministic fake adapters validate only request bounds and HTTP behavior; they do not count as generated model output. Embeddings/vector search/RAG are later milestones, not Sprint 1 runtime passes.

## Pinned candidate and runtime shape

- Model: [Qwen/Qwen2.5-0.5B-Instruct](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct), pinned revision `7ae557604adf67be50417f59c2c2f167def9a775`, Apache-2.0. See [`model_lock.json`](../../ai/fde_v2/coverstory/model_lock.json).
- Runtime: Transformers loads verified local files in the Python API process; no Ollama/llama.cpp server, external provider, mock or network inference fallback is configured.
- HTTP shape: React's same-origin `POST /generate` request reaches FastAPI on loopback `127.0.0.1:8765`. The backend loads the model locally from `FDE_V2_RUNTIME_DIR` (default `~/.cache/fde-v2/models`). `GET /health` and `GET /documents` are separate assistant-supplied paths.
- The generation adapter defaults to a45-second step-aware deadline, configurable from1–300 seconds with `FDE_V2_GENERATION_TIMEOUT_SECONDS`. It checks between decoder steps and maps expiry to HTTP504. One slow token/kernel call can overrun by that step; this is not an OS-level process kill or production SLA.
- `trust_remote_code` is false in both the lock and Transformers load calls. The model runtime binds only to loopback by default. Do not use private/corporate/customer data in this starter.

## Recorded access blocker

The earlier setup attempt called the official Hub metadata API through the configured proxy, before downloading files. The source invocation was `HfApi().model_info(repo_id="Qwen/Qwen2.5-0.5B-Instruct", revision="7ae557604adf67be50417f59c2c2f167def9a775", files_metadata=True, token=False)`. With the installed Hugging Face Hub client and its default endpoint, that maps to host `huggingface.co` and URL path `https://huggingface.co/api/models/Qwen/Qwen2.5-0.5B-Instruct/revision/7ae557604adf67be50417f59c2c2f167def9a775` with the `blobs=True` query parameter. **The raw HTTP request URL and response headers/body were not retained**, so the full encoded URL and proxy policy reason cannot be independently confirmed from this checkout. The recorded error text is **HTTP 403 Forbidden** through the configured proxy; there is no evidence that this was a Hub credentials failure, and no model bytes were received.

This is a proxy-mediated denial/response with unclear provenance, not a confirmed allowlist-policy decision. The smallest unresolved setup need is permission or network-policy clarification for HTTPS GET access to that pinned Hub metadata path and its official file downloads. No alternate endpoint, mirror, proxy, credentials or other environment was tried. Do not retry while this blocker remains unresolved.

## Real-only acceptance, when the blocker is resolved

These commands load only already-fetched and SHA-256-verified local artifacts. They do not fetch weights:

```sh
python scripts/fde_v2_generate_smoke.py --single
python scripts/fde_v2_generate_smoke.py --suite
```

Each command fails closed if the manifest/files are absent or invalid. The smoke script records the actual model ID/revision, prompt, output/failure, latency, runtime and machine; token counts are marked unavailable because this adapter does not expose them. A failed call remains a failed evidence gate. A test-double response can never satisfy this check.

The separate fetch command is `python scripts/fetch_fde_v2_models.py`; it was not run again in this release. Running it requires resolution of the blocker above. It uses only the pinned official model source/revision, disables `trust_remote_code`, and does not use a paid provider. Model files and runtime manifests stay outside Git.
