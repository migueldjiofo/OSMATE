from typing import Any

from fastapi.testclient import TestClient

from app.main import app
from app.schemas.routing import RoutingRequest, RoutingResponse

client = TestClient(app)


def test_routing_endpoint_returns_route(monkeypatch: Any) -> None:
    async def fake_calculate_route(request: RoutingRequest) -> RoutingResponse:
        return RoutingResponse(
            mode=request.mode,
            provider="test",
            distance_m=1200,
            duration_s=900,
            distance_text="1.2 km",
            duration_text="15 min",
            geojson={
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "properties": {
                            "mode": request.mode,
                            "provider": "test",
                        },
                        "geometry": {
                            "type": "LineString",
                            "coordinates": [
                                [request.start_lon, request.start_lat],
                                [
                                    request.destination_lon,
                                    request.destination_lat,
                                ],
                            ],
                        },
                    }
                ],
            },
            warnings=[],
        )

    monkeypatch.setattr(
        "app.api.routes.routing.calculate_route",
        fake_calculate_route,
    )

    response = client.post(
        "/api/v1/routing/route",
        json={
            "start_lat": 52.52,
            "start_lon": 13.405,
            "destination_lat": 52.521,
            "destination_lon": 13.41,
            "mode": "walking",
        },
    )

    assert response.status_code == 200

    payload = response.json()

    assert payload["mode"] == "walking"
    assert payload["distance_m"] == 1200
    assert payload["duration_s"] == 900
    assert payload["geojson"]["type"] == "FeatureCollection"


def test_routing_endpoint_rejects_invalid_mode() -> None:
    response = client.post(
        "/api/v1/routing/route",
        json={
            "start_lat": 52.52,
            "start_lon": 13.405,
            "destination_lat": 52.521,
            "destination_lon": 13.41,
            "mode": "train",
        },
    )

    assert response.status_code == 422