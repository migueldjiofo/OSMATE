import pytest

from app.agent.planner import PlanningError
from app.schemas.search_request import SearchRequest
from app.services.nominatim_client import GeocodingResult
from app.services.spatial_resolver import resolve_spatial_context


@pytest.mark.anyio
async def test_resolve_spatial_context_uses_existing_coordinates() -> None:
    request_data = SearchRequest(
        query="Finde Cafes",
        lat=52.52,
        lon=13.405,
    )

    resolved_request = await resolve_spatial_context(request_data)

    assert resolved_request.lat == 52.52
    assert resolved_request.lon == 13.405


@pytest.mark.anyio
async def test_resolve_spatial_context_geocodes_place_name(monkeypatch) -> None:
    async def fake_geocode_place_name(place_name: str) -> GeocodingResult:
        assert place_name == "Berlin Alexanderplatz"
        return GeocodingResult(lat=52.52, lon=13.405)

    monkeypatch.setattr(
        "app.services.spatial_resolver.geocode_place_name",
        fake_geocode_place_name,
    )

    request_data = SearchRequest(
        query="Finde Cafes",
        place_name="Berlin Alexanderplatz",
    )

    resolved_request = await resolve_spatial_context(request_data)

    assert resolved_request.lat == 52.52
    assert resolved_request.lon == 13.405


@pytest.mark.anyio
async def test_resolve_spatial_context_rejects_missing_location() -> None:
    request_data = SearchRequest(
        query="Finde Cafes",
    )

    with pytest.raises(PlanningError):
        await resolve_spatial_context(request_data)
