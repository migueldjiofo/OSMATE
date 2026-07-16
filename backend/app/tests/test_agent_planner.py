import pytest

from app.agent.planner import PlanningError, create_agent_plan
from app.schemas.search_request import SearchRequest


def test_agent_creates_cafe_plan_with_terrace() -> None:
    request_data = SearchRequest(
        query="Finde Cafes mit Terrasse in meiner Naehe",
        lat=52.52,
        lon=13.405,
        radius_m=1000,
        limit=10,
    )

    plan = create_agent_plan(request_data)

    assert plan.category == "cafe"
    assert plan.osm_tags["amenity"] == "cafe"
    assert plan.osm_tags["outdoor_seating"] == "yes"
    assert plan.spatial_filter.radius_m == 1000
    assert plan.limit == 10
    assert plan.confidence_score >= 0.75
    assert plan.planner_type == "rule_based"
    assert plan.llm_fallback_recommended is False


def test_agent_marks_ambiguous_terms_for_future_llm_fallback() -> None:
    request_data = SearchRequest(
        query="Finde ruhige Cafes mit WLAN und guenstig in meiner Naehe",
        lat=52.52,
        lon=13.405,
        radius_m=1000,
        limit=10,
    )

    plan = create_agent_plan(request_data)

    assert plan.category == "cafe"
    assert plan.osm_tags["amenity"] == "cafe"
    assert plan.osm_tags["internet_access"] == "wlan"
    assert "ruhige" in plan.unmatched_terms or "ruhig" in plan.unmatched_terms
    assert "guenstig" in plan.unmatched_terms
    assert len(plan.warnings) >= 1
    assert plan.llm_fallback_recommended is True


def test_agent_rejects_unknown_category() -> None:
    request_data = SearchRequest(
        query="Finde schoene Dinge in meiner Naehe",
        lat=52.52,
        lon=13.405,
        radius_m=1000,
        limit=10,
    )

    with pytest.raises(PlanningError):
        create_agent_plan(request_data)
