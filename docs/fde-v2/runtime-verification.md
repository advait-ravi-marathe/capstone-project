# Sprint 1 model runtime verification

**Status: real local generation is blocked and unverified.** No weights were transferred or committed. Deterministic fake adapters validate only request bounds and HTTP behavior; they do not count as generated model output. Embeddings/vector search/RAG are later milestones, not Sprint 1 runtime passes.

## Pinned candidate and runtime shape

The verified Sprint 1 section map for inference and local-only loading is in [sources/sprint1-course-map.md](sources/sprint1-course-map.md). It identifies the current Transformers documentation sections and notes that pinned-version docs were not accessible during source review.

- Model: [Qwen/Qwen2.5-0.5B-Instruct](https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct), pinned revision `7ae557604adf67be50417f59c2c2f167def9a775`, Apache-2.0. See [`model_lock.json`](../../ai/fde_v2/coverstory/model_lock.json).
- Runtime: Transformers loads verified local files in the Python API process; no Ollama/llama.cpp server, external provider, mock or network inference fallback is configured.
- HTTP shape: React's same-origin `POST /generate` request reaches FastAPI on loopback `127.0.0.1:8765`. The backend loads the model locally from `FDE_V2_RUNTIME_DIR` (default `~/.cache/fde-v2/models`). `GET /health` and `GET /documents` are separate assistant-supplied paths.
- The generation adapter defaults to a45-second step-aware deadline, configurable from1–300 seconds with `FDE_V2_GENERATION_TIMEOUT_SECONDS`. It checks between decoder steps and maps expiry to HTTP504. One slow token/kernel call can overrun by that step; this is not an OS-level process kill or production SLA.
- API initialization is single-flight and the app permits one generation request at a time; an overlapping request gets HTTP429 rather than loading a second model or queueing unbounded model work. This is a small-memory local learning limit, not a throughput claim.
- `trust_remote_code` is false in both the lock and Transformers load calls. The model runtime binds only to loopback by default. Do not use private/corporate/customer data in this starter.

## Recorded access blocker

One separately authorized diagnostic GET was later made to the exact pinned metadata URL (without a token and without redirects or retries): `https://huggingface.co/api/models/Qwen/Qwen2.5-0.5B-Instruct/revision/7ae557604adf67be50417f59c2c2f167def9a775?blobs=true`. `curl` exited 56 with `CONNECT tunnel failed, response 403`; the proxy response was `HTTP/1.1 403 Forbidden`, `content-length: 16`, `content-type: text/plain`, `server: envoy`, and `connection: close`. The configured HTTPS proxy resolved to host `proxy`, port `8080` (credentials are not recorded). The CONNECT handshake failed before an origin HTTP response, so curl reported `http_code=000`; there is no evidence that Hugging Face itself returned 403 or that the proxy response identifies a policy rule. No model bytes were received. No retry, alternate route or network-setting change was made. The smallest unresolved requirement is network permission for HTTPS CONNECT through the configured proxy to `huggingface.co:443` for read-only metadata and, separately, official pinned-file downloads.

The first setup attempt called the official Hub metadata API through the configured proxy, before downloading files. The source invocation was `HfApi().model_info(repo_id="Qwen/Qwen2.5-0.5B-Instruct", revision="7ae557604adf67be50417f59c2c2f167def9a775", files_metadata=True, token=False)`. With the installed Hugging Face Hub client and its default endpoint, that maps to host `huggingface.co` and URL path `https://huggingface.co/api/models/Qwen/Qwen2.5-0.5B-Instruct/revision/7ae557604adf67be50417f59c2c2f167def9a775` with the `blobs=True` query parameter. **That first attempt did not retain the raw HTTP request URL or response headers/body.** The separately authorized diagnostic above records the later CONNECT response, but still does not establish the proxy policy reason or an origin response.

This is a proxy-mediated denial/response with unclear provenance, not a confirmed allowlist-policy decision. The smallest unresolved setup need is permission or network-policy clarification for HTTPS GET access to that pinned Hub metadata path and its official file downloads. No alternate endpoint, mirror, proxy, credentials or other environment was tried. Do not retry while this blocker remains unresolved.

## Real-only acceptance, when the blocker is resolved

These commands load only already-fetched and SHA-256-verified local artifacts. They do not fetch weights:

```sh
python scripts/fde_v2_generate_smoke.py --single
python scripts/fde_v2_generate_smoke.py --suite
```

Each command fails closed if the manifest/files are absent or invalid. The smoke script records the actual model ID/revision, prompt, output/failure, latency, runtime and machine; token counts are marked unavailable because this adapter does not expose them. A failed call remains a failed evidence gate. A test-double response can never satisfy this check.

The separate fetch command is `python scripts/fetch_fde_v2_models.py`; it was not run again in this release. Running it requires resolution of the blocker above. It uses only the pinned official model source/revision, disables `trust_remote_code`, and does not use a paid provider. Model files and runtime manifests stay outside Git.
