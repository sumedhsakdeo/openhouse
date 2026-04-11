"""FastAPI application entry point for the Lance sidecar service."""

import logging

from fastapi import FastAPI

from lance_sidecar.routes import router

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(name)s %(levelname)s %(message)s")

app = FastAPI(
    title="OpenHouse Lance Sidecar",
    description="REST API wrapping LanceDB operations for the OpenHouse control plane",
    version="0.1.0",
)

app.include_router(router)


@app.get("/health")
def health():
    return {"status": "ok"}
