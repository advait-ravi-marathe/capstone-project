from __future__ import annotations

import logging
import os
import time
import uuid
from collections.abc import Callable
from pathlib import Path

from fastapi import FastAPI, HTTPException, Request
from fastapi.exceptions import RequestValidationError
from pydantic import BaseModel, Field
from starlette.responses import JSONResponse

from .contracts import RuntimeUnavailable
from .corpus import CorpusStore
from .models import LocalModels
from .reference import generate_text as checked_generate_text

logger = logging.getLogger("fde_v2.coverstory")


class GenerateRequest(BaseModel):
    question: str = Field(min_length=1, max_length=2000)


def _default_generator():
    return LocalModels.load()


def create_app(generator_factory: Callable[[], object] | None = None) -> FastAPI:
    app = FastAPI(title="CoverStory AI local learning app", version="0.1.0", docs_url="/docs")
    factory = generator_factory or _default_generator
    app.state.generator = None

    def generator():
        if app.state.generator is None:
            try:
                app.state.generator = factory()
            except RuntimeUnavailable:
                raise
            except Exception as exc:
                logger.exception("local model initialization failed")
                raise RuntimeUnavailable("pinned local model is unavailable") from exc
        return app.state.generator

    @app.middleware("http")
    async def request_log(request: Request, call_next):
        request_id = request.headers.get("x-request-id") or str(uuid.uuid4())
        request.state.request_id = request_id
        started = time.perf_counter()
        response = await call_next(request)
        response.headers["x-request-id"] = request_id
        logger.info("request_id=%s method=%s path=%s status=%s elapsed_ms=%.1f",
                    request_id, request.method, request.url.path, response.status_code,
                    (time.perf_counter() - started) * 1000)
        return response

    @app.exception_handler(RequestValidationError)
    async def invalid_request(_request: Request, _exc: RequestValidationError):
        return JSONResponse(status_code=422, content={"error": "invalid_request",
                                                     "message": "request fields are outside the accepted contract"})

    @app.get("/health")
    def health():
        return {"status": "ok", "mode": "local-learning-prototype"}

    @app.get("/documents")
    def documents():
        root = Path(os.environ.get("FDE_V2_DATA_DIR", ".runtime/fde-v2")).resolve()
        path = root / "corpus.sqlite3"
        if not path.is_file():
            return {"documents": []}
        try:
            return {"documents": CorpusStore(path).metadata()}
        except Exception as exc:
            logger.exception("document metadata read failed")
            raise HTTPException(status_code=500, detail="document metadata is unavailable") from exc

    @app.post("/generate")
    def generate(body: GenerateRequest, request: Request):
        try:
            answer, elapsed = checked_generate_text(generator(), body.question)
        except RuntimeUnavailable as exc:
            raise HTTPException(status_code=503, detail="pinned local generation is unavailable") from exc
        except TimeoutError as exc:
            raise HTTPException(status_code=504, detail="local generation exceeded its bounded deadline") from exc
        except Exception as exc:
            logger.exception("generation failed request_id=%s", request.state.request_id)
            raise HTTPException(status_code=502, detail="local generation failed") from exc
        return {"answer": answer, "elapsed_ms": round(elapsed, 2),
                "provider": "pinned-local-model", "request_id": request.state.request_id}

    return app


app = create_app()
