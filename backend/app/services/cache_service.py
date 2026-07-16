import hashlib
import json
import sqlite3
import time
from pathlib import Path
from typing import Any

from app.core.config import BACKEND_DIR, get_settings


def _resolve_database_path(database_path: str | Path | None = None) -> Path:
    """Ermittelt den vollstaendigen Pfad zur SQLite-Cache-Datenbank."""
    if database_path is None:
        path = Path(get_settings().cache_database_path)
    else:
        path = Path(database_path)

    if not path.is_absolute():
        path = BACKEND_DIR / path

    return path


def initialize_cache(database_path: str | Path | None = None) -> Path:
    """Initialisiert die SQLite-Cache-Datenbank, falls sie noch nicht existiert."""
    resolved_path = _resolve_database_path(database_path)
    resolved_path.parent.mkdir(parents=True, exist_ok=True)

    with sqlite3.connect(resolved_path) as connection:
        connection.execute(
            """
            CREATE TABLE IF NOT EXISTS cache_entries (
                cache_key TEXT PRIMARY KEY,
                payload TEXT NOT NULL,
                expires_at INTEGER NOT NULL,
                created_at INTEGER NOT NULL
            )
            """
        )

    return resolved_path


def make_cache_key(namespace: str, payload: dict[str, Any] | str) -> str:
    """Erzeugt einen stabilen Cache-Schluessel fuer eine Anfrage."""
    if isinstance(payload, str):
        normalized_payload = payload
    else:
        normalized_payload = json.dumps(
            payload,
            sort_keys=True,
            ensure_ascii=False,
            separators=(",", ":"),
        )

    digest = hashlib.sha256(normalized_payload.encode("utf-8")).hexdigest()

    return f"{namespace}:{digest}"


def get_cached_json(
    cache_key: str,
    database_path: str | Path | None = None,
) -> Any | None:
    """Liest einen JSON-Wert aus dem Cache, sofern er noch gueltig ist."""
    resolved_path = initialize_cache(database_path)
    now = int(time.time())

    with sqlite3.connect(resolved_path) as connection:
        row = connection.execute(
            """
            SELECT payload, expires_at
            FROM cache_entries
            WHERE cache_key = ?
            """,
            (cache_key,),
        ).fetchone()

        if row is None:
            return None

        payload_text, expires_at = row

        if int(expires_at) <= now:
            connection.execute(
                "DELETE FROM cache_entries WHERE cache_key = ?",
                (cache_key,),
            )
            return None

        try:
            return json.loads(payload_text)

        except json.JSONDecodeError:
            connection.execute(
                "DELETE FROM cache_entries WHERE cache_key = ?",
                (cache_key,),
            )
            return None


def set_cached_json(
    cache_key: str,
    payload: Any,
    ttl_seconds: int,
    database_path: str | Path | None = None,
) -> None:
    """Speichert einen JSON-kompatiblen Wert im Cache."""
    resolved_path = initialize_cache(database_path)
    now = int(time.time())
    expires_at = now + ttl_seconds
    payload_text = json.dumps(payload, ensure_ascii=False)

    with sqlite3.connect(resolved_path) as connection:
        connection.execute(
            """
            INSERT OR REPLACE INTO cache_entries (
                cache_key,
                payload,
                expires_at,
                created_at
            )
            VALUES (?, ?, ?, ?)
            """,
            (cache_key, payload_text, expires_at, now),
        )
