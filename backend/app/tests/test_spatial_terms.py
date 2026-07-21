from app.agent.planner import create_agent_plan
from app.agent.spatial_terms import extract_radius_meters
from app.schemas.search_request import SearchRequest


def test_extracts_radius_from_500_meters():
    radius = extract_radius_meters(
        query="Finde Cafes im Umkreis von 500 Metern",
        fallback_radius_m=1000,
        default_radius_m=1000,
    )

    assert radius == 500


def test_extracts_radius_from_one_kilometer_word():
    radius = extract_radius_meters(
        query="Finde Spaetis im Umkreis von einem Kilometer",
        fallback_radius_m=500,
        default_radius_m=1000,
    )

    assert radius == 1000


def test_extracts_radius_from_two_km():
    radius = extract_radius_meters(
        query="Finde Apotheken im Umkreis von 2 km",
        fallback_radius_m=500,
        default_radius_m=1000,
    )

    assert radius == 2000


def test_uses_fallback_radius_without_explicit_radius():
    radius = extract_radius_meters(
        query="Finde Cafes in meiner Naehe",
        fallback_radius_m=750,
        default_radius_m=1000,
    )

    assert radius == 750


def test_agent_plan_uses_radius_from_query():
    request = SearchRequest(
        query="Finde Baeckereien im Umkreis von 500 Metern",
        lat=52.5214574,
        lon=13.4110794,
        radius_m=1000,
        limit=10,
        language="de",
    )

    plan = create_agent_plan(request)

    assert plan.category == "bakery"
    assert plan.spatial_filter.radius_m == 500
    assert any("Radius 500 Meter" in step for step in plan.planner_steps)