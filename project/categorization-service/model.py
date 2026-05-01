from typing import List

import numpy as np
from sentence_transformers import SentenceTransformer

from categories import CATEGORIES

MODEL_NAME = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"

model: SentenceTransformer
category_names: List[str]
category_embeddings: np.ndarray


def load():
    global model, category_names, category_embeddings
    model = SentenceTransformer(MODEL_NAME)
    category_names = list(CATEGORIES.keys())
    category_embeddings = model.encode(list(CATEGORIES.values()), normalize_embeddings=True)


def encode(texts: List[str]) -> np.ndarray:
    return model.encode(texts, normalize_embeddings=True)
