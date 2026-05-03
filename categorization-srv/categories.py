import json
import os
from pathlib import Path
from typing import Dict


DEFAULT_CATEGORIES_FILE = Path(__file__).resolve().parents[1] / "shared" / "categories.json"
CATEGORIES_FILE = Path(os.getenv("CATEGORIES_FILE", DEFAULT_CATEGORIES_FILE))


def _load_categories() -> dict:
    with CATEGORIES_FILE.open(encoding="utf-8") as categories_file:
        return json.load(categories_file)


_CONFIG = _load_categories()
_CATEGORY_DEFINITIONS = _CONFIG["categories"]

CATEGORIES: Dict[str, str] = {
    category["name"]: category["embeddingText"]
    for category in _CATEGORY_DEFINITIONS
}

_FALLBACK_CATEGORIES = [
    category["name"]
    for category in _CATEGORY_DEFINITIONS
    if category.get("fallback") is True
]

if len(_FALLBACK_CATEGORIES) != 1:
    raise ValueError("Exactly one category must be marked as fallback")

FALLBACK_CATEGORY = _FALLBACK_CATEGORIES[0]
