from __future__ import annotations

import json
import os
import re
from dataclasses import dataclass
from typing import Any

from app.agent.planner import PlanningError
from app.schemas.agent_plan import AgentPlan
from app.schemas.search_request import SearchRequest


@dataclass(frozen=True)
class LlmPlannerConfig:
    enabled: bool
    api_key: str | None
    model: str


def get_llm_planner_config() -> LlmPlannerConfig:
    mode = os.getenv("OSMATE_AGENT_MODE", "rule_based").strip().lower()
    api_key = os.getenv("GEMINI_API_KEY")
    model = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")

    return LlmPlannerConfig(
        enabled=mode in {"gemini", "hybrid"},
        api_key=api_key,
        model=model,
    )


def build_llm_prompt(request_data: SearchRequest, allowed_tags: set[tuple[str, str]]) -> str:
    allowed_tag_lines = "\n".join(
        f"- {key}={value}" for key, value in sorted(allowed_tags)
    )

    return f"""
Du bist der OSMATE-Planungsagent fuer OpenStreetMap-Suchanfragen.
Extrahiere aus der Nutzeranfrage einen strukturierten Suchplan.
Antworte ausschliesslich mit gueltigem JSON.

Nutzeranfrage:
{request_data.query}

Erlaubte OpenStreetMap-Tags:
{allowed_tag_lines}

Pflichtformat:
{{
  "intent": "poi_search",
  "category": "string",
  "osm_tags": {{"key": "value"}},
  "confidence_score": 0.0,
  "unmatched_terms": [],
  "warnings": [],
  "summary_de": "string"
}}

Regeln:
- Nutze nur Tags aus der erlaubten Liste.
- Erfinde keine OpenStreetMap-Tags.
- Wenn qualitative Begriffe nicht sicher abbildbar sind, fuege sie in unmatched_terms ein.
- Wenn die Anfrage nicht sicher verstanden wird, setze confidence_score unter 0.6.
""".strip()


def parse_llm_json_response(text: str) -> dict[str, Any]:
    cleaned = text.strip()

    fenced_match = re.search(r"```(?:json)?\s*(.*?)```", cleaned, flags=re.DOTALL)
    if fenced_match:
        cleaned = fenced_match.group(1).strip()

    try:
        payload = json.loads(cleaned)
    except json.JSONDecodeError as exc:
        raise PlanningError("Die LLM-Antwort konnte nicht als JSON gelesen werden.") from exc

    if not isinstance(payload, dict):
        raise PlanningError("Die LLM-Antwort enthaelt kein JSON-Objekt.")

    return payload


def create_llm_agent_plan(
    request_data: SearchRequest,
    allowed_tags: set[tuple[str, str]],
) -> AgentPlan:
    config = get_llm_planner_config()

    if not config.enabled:
        raise PlanningError("Der LLM-Planer ist nicht aktiviert.")

    if not config.api_key:
        raise PlanningError("GEMINI_API_KEY ist nicht konfiguriert.")

    from google import genai

    prompt = build_llm_prompt(request_data=request_data, allowed_tags=allowed_tags)

    client = genai.Client(api_key=config.api_key)
    response = client.models.generate_content(
        model=config.model,
        contents=prompt,
    )

    payload = parse_llm_json_response(response.text or "")

    osm_tags = payload.get("osm_tags", {})
    if not isinstance(osm_tags, dict) or not osm_tags:
        raise PlanningError("Der LLM-Planer hat keine gueltigen OpenStreetMap-Tags geliefert.")

    invalid_tags = [
        (str(key), str(value))
        for key, value in osm_tags.items()
        if (str(key), str(value)) not in allowed_tags
    ]

    if invalid_tags:
        raise PlanningError("Der LLM-Planer hat nicht erlaubte OpenStreetMap-Tags geliefert.")

    if request_data.lat is None or request_data.lon is None:
        raise PlanningError("Fuer den LLM-Planer muessen Koordinaten vorhanden sein.")

    confidence_score = float(payload.get("confidence_score", 0.6))

    return AgentPlan(
        intent=str(payload.get("intent", "poi_search")),
        category=str(payload.get("category", "unknown")),
        osm_tags={str(key): str(value) for key, value in osm_tags.items()},
        spatial_filter={
            "type": "radius",
            "radius_m": request_data.radius_m,
            "lat": request_data.lat,
            "lon": request_data.lon,
        },
        limit=request_data.limit,
        confidence_score=confidence_score,
        unmatched_terms=list(payload.get("unmatched_terms", [])),
        warnings=list(payload.get("warnings", [])),
        summary_de=str(payload.get("summary_de", "LLM-gestuetzter Suchplan.")),
        planner_type="gemini",
        llm_fallback_recommended=confidence_score < 0.6,
    )