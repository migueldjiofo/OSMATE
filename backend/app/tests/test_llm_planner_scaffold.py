import pytest

from app.agent.langgraph_workflow import create_osmate_agent_graph
from app.agent.llm_planner import build_llm_prompt, get_llm_planner_config, parse_llm_json_response
from app.agent.planner import PlanningError
from app.agent.tag_catalog import get_allowed_tags
from app.schemas.search_request import SearchRequest


def test_build_llm_prompt_contains_query_and_allowed_tags():
    request = SearchRequest(
        query="Finde Cafes mit Terrasse",
        lat=52.52,
        lon=13.405,
        radius_m=1000,
        limit=10,
        language="de",
    )

    prompt = build_llm_prompt(
        request_data=request,
        allowed_tags=get_allowed_tags(),
    )

    assert "Finde Cafes mit Terrasse" in prompt
    assert "amenity=cafe" in prompt
    assert "gueltigem JSON" in prompt


def test_parse_llm_json_response_accepts_plain_json():
    payload = parse_llm_json_response(
        '{"intent":"poi_search","category":"cafe","osm_tags":{"amenity":"cafe"}}'
    )

    assert payload["category"] == "cafe"
    assert payload["osm_tags"]["amenity"] == "cafe"


def test_parse_llm_json_response_accepts_fenced_json():
    payload = parse_llm_json_response(
        '```json\n{"intent":"poi_search","category":"pharmacy","osm_tags":{"amenity":"pharmacy"}}\n```'
    )

    assert payload["category"] == "pharmacy"
    assert payload["osm_tags"]["amenity"] == "pharmacy"


def test_parse_llm_json_response_rejects_invalid_json():
    with pytest.raises(PlanningError):
        parse_llm_json_response("kein json")


def test_llm_config_is_rule_based_by_default(monkeypatch):
    monkeypatch.delenv("OSMATE_AGENT_MODE", raising=False)
    monkeypatch.delenv("GEMINI_API_KEY", raising=False)

    config = get_llm_planner_config()

    assert config.enabled is False
    assert config.api_key is None


def test_langgraph_workflow_can_be_created():
    graph = create_osmate_agent_graph()

    assert graph is not None