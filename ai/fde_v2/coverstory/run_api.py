from __future__ import annotations

import logging

import uvicorn

from .api import app


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
    uvicorn.run(app, host="127.0.0.1", port=8765, log_level="info")
