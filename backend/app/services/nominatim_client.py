from dataclasses import dataclass
from typing import Any

import httpx

from app.core.config import get_settings


@dataclass(frozen=True)
class GeocodingResult:
    """Ergebnis einer Ortsnamensuche ueber Nominatim."""

    lat: float
    lon: float


class NominatimClientError(RuntimeError):
    """Allgemeiner Fehler beim Zugriff auf die Nominatim-API."""


class NominatimNoResultError(ValueError):
    """Fehler, wenn Nominatim keinen passenden Ort findet."""


async def geocode_place_name(
    place_name: str,
    client: httpx.AsyncClient | None = None,
    nominatim_url: str | None = None,
) -> GeocodingResult:
    """Geokodiert einen Ortsnamen ueber die Nominatim-API."""
    settings = get_settings()
    target_url = nominatim_url or settings.nominatim_url

    timeout = httpx.Timeout(20.0, connect=10.0)
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
            response = await active_client.get(
                f"{target_url.rstrip('/')}/search",
                params={
                    "q": place_name,
                    "format": "jsonv2",
                    "limit": 1,
                },
            )

        except httpx.TimeoutException as exc:
            raise NominatimClientError(
                "Die Anfrage an die Nominatim-API hat zu lange gedauert."
            ) from exc

        except httpx.RequestError as exc:
            raise NominatimClientError(
                "Die Nominatim-API konnte nicht erreicht werden."
            ) from exc

        if response.status_code == 429:
            raise NominatimClientError(
                "Die Nominatim-API hat die Anfrage wegen zu vieler Anfragen abgelehnt."
            )

        try:
            response.raise_for_status()

        except httpx.HTTPStatusError as exc:
            raise NominatimClientError(
                f"Die Nominatim-API hat mit Status {response.status_code} geantwortet."
            ) from exc

        try:
            payload: Any = response.json()

        except ValueError as exc:
            raise NominatimClientError(
                "Die Nominatim-API hat keine gueltige JSON-Antwort geliefert."
            ) from exc

        if not isinstance(payload, list):
            raise NominatimClientError(
                "Die Nominatim-Antwort hat ein unerwartetes Format."
            )

        if len(payload) == 0:
            raise NominatimNoResultError(
                f"Der Ort '{place_name}' konnte nicht gefunden werden."
            )

        first_result = payload[0]

        if not isinstance(first_result, dict):
            raise NominatimClientError(
                "Das erste Nominatim-Ergebnis hat ein unerwartetes Format."
            )

        try:
            lat = float(first_result["lat"])
            lon = float(first_result["lon"])

        except (KeyError, TypeError, ValueError) as exc:
            raise NominatimClientError(
                "Das Nominatim-Ergebnis enthaelt keine gueltigen Koordinaten."
            ) from exc

        return GeocodingResult(
            lat=lat,
            lon=lon,
        )

    finally:
        if should_close_client:
            await active_client.aclose()
