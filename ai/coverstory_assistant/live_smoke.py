"""Explicitly opt-in live provider smoke call; never used by CI."""
from __future__ import annotations

import argparse
import os

from ai.coverstory_assistant.provider import OpenAIResponsesClient, ProviderConfig, ProviderFailure


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--confirm-provider-usage", action="store_true")
    args = parser.parse_args()
    if not args.confirm_provider_usage:
        parser.error("a live request needs explicit --confirm-provider-usage; no request was sent")
    key = os.environ.get("OPENAI_API_KEY", "")
    model = os.environ.get("OPENAI_MODEL", "")
    base_url = os.environ.get("OPENAI_BASE_URL", "https://api.openai.com/v1")
    if not key or not model:
        parser.error("set OPENAI_API_KEY and OPENAI_MODEL using user-managed configuration; no request was sent")
    client = OpenAIResponsesClient(ProviderConfig(api_key=key, model=model, base_url=base_url))
    try:
        text = client.generate(
            "Answer only from the supplied synthetic policy excerpt. If unsupported, abstain.",
            "Policy excerpt: Hail damage is excluded.\nQuestion: Is hail damage covered?",
        )
    except (ProviderFailure, ValueError) as exc:
        print(f"Live provider call failed safely: {exc}")
        return 1
    print("Live provider request completed. This output is not yet independently evaluated:")
    print(text)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
