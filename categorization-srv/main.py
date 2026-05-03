from contextlib import asynccontextmanager
from typing import List

from fastapi import FastAPI
from pydantic import BaseModel

import model as embedding_model
from categorize import assign_categories


@asynccontextmanager
async def lifespan(app: FastAPI):
    embedding_model.load()
    yield


app = FastAPI(lifespan=lifespan)


class CategorizeItem(BaseModel):
    id: str
    name: str


class CategorizeRequest(BaseModel):
    items: List[CategorizeItem]


class CategorizeResultItem(BaseModel):
    id: str
    category: str


@app.post("/categorize", response_model=List[CategorizeResultItem])
def categorize(request: CategorizeRequest) -> List[CategorizeResultItem]:
    names = [item.name for item in request.items]
    categories = assign_categories(names)
    return [
        CategorizeResultItem(id=item.id, category=category)
        for item, category in zip(request.items, categories)
    ]


@app.get("/health")
def health():
    return {"status": "ok"}
