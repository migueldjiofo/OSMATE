from typing import Literal

from pydantic import BaseModel, Field


class SpatialFilter(BaseModel):
    """Raeumlicher Filter fuer eine OSM-Suche."""

    type: Literal["radius"] = "radius"
    radius_m: int = Field(ge=50, le=10000)
    lat: float = Field(ge=-90, le=90)
    lon: float = Field(ge=-180, le=180)


class AgentPlan(BaseModel):
    """Strukturierter Suchplan des OSMATE-Agenten."""

    intent: Literal["poi_search"] = "poi_search"
    category: str
    osm_tags: dict[str, str]
    spatial_filter: SpatialFilter
    limit: int = Field(ge=1, le=500)
    confidence_score: float = Field(ge=0.0, le=1.0)
    unmatched_terms: list[str] = []
    warnings: list[str] = []
    summary_de: str
    planner_type: Literal["rule_based"] = "rule_based"
    llm_fallback_recommended: bool = False
