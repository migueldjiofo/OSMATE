from typing import Any

from pydantic import BaseModel, Field

from app.schemas.agent_plan import AgentPlan


class SearchResponse(BaseModel):
    """Antwortmodell fuer eine vollstaendige OSMATE-Suche."""

    original_query: str
    agent_plan: AgentPlan
    overpass_ql: str
    geojson: dict[str, Any]
    result_count: int = Field(ge=0)
    warnings: list[str] = Field(default_factory=list)
    execution_steps: list[str] = Field(default_factory=list)
