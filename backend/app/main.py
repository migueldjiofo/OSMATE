from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.routes.health import router as health_router
from app.api.routes.search import router as search_router
from app.api.routes.routing import router as routing_router
from app.core.config import get_settings


def create_app() -> FastAPI:
    """Erstellt und konfiguriert die FastAPI-Anwendung."""
    settings = get_settings()

    app = FastAPI(
        title=settings.app_name,
        version="0.1.0",
        docs_url="/docs",
        redoc_url="/redoc",
    )

    app.add_middleware(
        CORSMiddleware,
        allow_origins=[
            "http://localhost",
            "http://127.0.0.1",
            "http://10.0.2.2",
        ],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    app.include_router(health_router, prefix=settings.api_prefix)
    app.include_router(search_router, prefix=settings.api_prefix)

    @app.get("/")
    async def root() -> dict[str, str]:
        """Gibt eine einfache Startmeldung fuer das Backend zurueck."""
        return {
            "status": "running",
            "service": settings.app_name,
            "docs": "/docs",
        }

    return app


app = create_app()
app.include_router(routing_router, prefix="/api/v1/routing", tags=["routing"])
