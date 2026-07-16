from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_search_plan_endpoint_returns_agent_plan() -> None:
    response = client.post(
        "/api/v1/search/plan",
        json={
            "query": "Finde Cafes mit Terrasse in meiner Naehe",
            "lat": 52.52,
            "lon": 13.405,
            "radius_m": 1000,
            "limit": 10,
            "language": "de",
        },
    )

    assert response.status_code == 200

    payload = response.json()

    assert payload["intent"] == "poi_search"
    assert payload["category"] == "cafe"
    assert payload["osm_tags"]["amenity"] == "cafe"
    assert payload["osm_tags"]["outdoor_seating"] == "yes"
    assert payload["spatial_filter"]["radius_m"] == 1000
    assert payload["limit"] == 10
    assert payload["planner_type"] == "rule_based"


def test_search_plan_endpoint_returns_planning_error_for_unknown_category() -> None:
    response = client.post(
        "/api/v1/search/plan",
        json={
            "query": "Finde schoene Dinge in meiner Naehe",
            "lat": 52.52,
            "lon": 13.405,
            "radius_m": 1000,
            "limit": 10,
            "language": "de",
        },
    )

    assert response.status_code == 422

    payload = response.json()

    assert payload["code"] == "planning_error"
    assert "OSM-Kategorie" in payload["message"]
