from typing import List

import numpy as np

from categories import FALLBACK_CATEGORY
import model as embedding_model

SIMILARITY_THRESHOLD = 0.30


def assign_categories(names: List[str]) -> List[str]:
    item_embeddings = embedding_model.encode(names)

    # cosine similarity via dot product (embeddings are L2-normalized)
    scores = item_embeddings @ embedding_model.category_embeddings.T

    results = []
    for item_scores in scores:
        best_idx = int(np.argmax(item_scores))
        best_score = float(item_scores[best_idx])
        category = embedding_model.category_names[best_idx] if best_score >= SIMILARITY_THRESHOLD else FALLBACK_CATEGORY
        results.append(category)

    return results
