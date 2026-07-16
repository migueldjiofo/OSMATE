from typing import Any

import httpx

from app.core.config import get_settings


class OverpassClientError(RuntimeError):
    """Allgemeiner Fehler beim Zugriff auf die Overpass-API."""


class OverpassTimeoutError(OverpassClientError):
    """Fehler bei einer Zeitueberschreitung der Overpass-API."""


class OverpassRateLimitError(OverpassClientError):
    """Fehler bei einer Ratenbegrenzung durch die Overpass-API."""


class OverpassServerError(OverpassClientError):
    """Fehler bei temporaeren Serverproblemen der Overpass-API."""


async def execute_overpass_query(
    overpass_ql: str,
    client: httpx.AsyncClient | None = None,
    overpass_url: str | None = None,
) -> dict[str, Any]:
    """Sendet eine Overpass-QL-Abfrage an die Overpass-API."""
    settings = get_settings()
    target_url = overpass_url or settings.overpass_url

    timeout = httpx.Timeout(30.0, connect=10.0)
    headers = {
        "User-Agent": "OSMATE/0.1 academic GIS project",
    }

    active_client = client or httpx.AsyncClient(
        timeout=timeout,
        headers=headers,
    )

    should_close_client = client is None

    try:
        try:
            response = await active_client.post(
                target_url,
                data={"data": overpass_ql},
            )

        except httpx.TimeoutException as exc:
            raise OverpassTimeoutError(
                "Die Anfrage an die Overpass-API hat zu lange gedauert."
            ) from exc

        except httpx.RequestError as exc:
            raise OverpassClientError(
                "Die Overpass-API konnte nicht erreicht werden."
            ) from exc

        if response.status_code == 429:
            raise OverpassRateLimitError(
                "Die Overpass-API hat die Anfrage wegen zu vieler Anfragen abgelehnt."
            )

        if response.status_code in {502, 503, 504}:
            raise OverpassServerError(
                "Die Overpass-API ist temporaer nicht verfuegbar."
            )

        try:
            response.raise_for_status()

        except httpx.HTTPStatusError as exc:
            raise OverpassClientError(
                f"Die Overpass-API hat mit Status {response.status_code} geantwortet."
            ) from exc

        try:
            payload = response.json()

        except ValueError as exc:
            raise OverpassClientError(
                "Die Overpass-API hat keine gueltige JSON-Antwort geliefert."
            ) from exc

        if not isinstance(payload, dict):
            raise OverpassClientError(
                "Die Overpass-Antwort hat ein unerwartetes Format."
            )

        elements = payload.get("elements")

        if not isinstance(elements, list):
            raise OverpassClientError(
                "Die Overpass-Antwort enthaelt keine gueltige Elementliste."
            )

        return payload

    finally:
        if should_close_client:
            await active_client.aclose()
