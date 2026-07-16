from functools import lru_cache
from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

BACKEND_DIR = Path(__file__).resolve().parents[2]
ENV_FILE = BACKEND_DIR / ".env"


class Settings(BaseSettings):
    """Zentrale Anwendungskonfiguration fuer das OSMATE-Backend."""

    model_config = SettingsConfigDict(
        env_file=str(ENV_FILE),
        env_file_encoding="utf-8",
        extra="ignore",
    )

    app_name: str = "OSMATE Backend"
    app_env: str = "development"
    app_debug: bool = True
    api_prefix: str = "/api/v1"

    backend_host: str = "127.0.0.1"
    backend_port: int = 8000

    overpass_url: str = "https://overpass-api.de/api/interpreter"
    nominatim_url: str = "https://nominatim.openstreetmap.org"

    default_search_radius_m: int = 1000
    max_search_radius_m: int = 3000
    default_result_limit: int = 20
    max_result_limit: int = 100

    cache_enabled: bool = True
    cache_database_path: str = "data/cache/osmate_cache.sqlite3"
    cache_ttl_seconds: int = 86400


@lru_cache
def get_settings() -> Settings:
    """Liest die Konfiguration einmalig und stellt sie wiederverwendbar bereit."""
    return Settings()
