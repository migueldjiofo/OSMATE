from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_search_overpass_query_endpoint_returns_query() -> None:
    response = client.post(
        "/api/v1/search/overpass-query",
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

    assert payload["agent_plan"]["category"] == "cafe"
    assert payload["agent_plan"]["planner_type"] == "rule_based"
    assert "[out:json][timeout:25];" in payload["overpass_ql"]
    assert 'node["amenity"="cafe"]["outdoor_seating"="yes"]' in payload["overpass_ql"]
    assert "(around:1000,52.520000,13.405000)" in payload["overpass_ql"]
