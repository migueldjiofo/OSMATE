from app.agent.planner import create_agent_plan
from app.schemas.search_request import SearchRequest


def test_agent_plan_contains_planner_steps():
    request = SearchRequest(
        query="Finde Cafes mit Terrasse",
        lat=52.5214574,
        lon=13.4110794,
        radius_m=1000,
        limit=10,
        language="de",
    )

    plan = create_agent_plan(request)

    assert plan.category == "cafe"
    assert len(plan.planner_steps) >= 3
    assert any("Kategorie erkannt" in step for step in plan.planner_steps)
    assert any("OpenStreetMap-Tags" in step for step in plan.planner_steps)
    assert any("Radius" in step for step in plan.planner_steps)