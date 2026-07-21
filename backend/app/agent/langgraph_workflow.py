from __future__ import annotations

from typing import Any, TypedDict

from langgraph.graph import END, StateGraph

from app.agent.llm_planner import build_llm_prompt, parse_llm_json_response
from app.agent.tag_catalog import get_allowed_tags
from app.schemas.search_request import SearchRequest


class OsmateAgentState(TypedDict, total=False):
    request: SearchRequest
    prompt: str
    raw_response: str
    parsed_response: dict[str, Any]
    error: str


def build_prompt_node(state: OsmateAgentState) -> OsmateAgentState:
    request = state["request"]
    state["prompt"] = build_llm_prompt(
        request_data=request,
        allowed_tags=get_allowed_tags(),
    )
    return state


def parse_response_node(state: OsmateAgentState) -> OsmateAgentState:
    raw_response = state.get("raw_response", "{}")
    state["parsed_response"] = parse_llm_json_response(raw_response)
    return state


def create_osmate_agent_graph():
    graph = StateGraph(OsmateAgentState)
    graph.add_node("build_prompt", build_prompt_node)
    graph.add_node("parse_response", parse_response_node)

    graph.set_entry_point("build_prompt")
    graph.add_edge("build_prompt", "parse_response")
    graph.add_edge("parse_response", END)

    return graph.compile()