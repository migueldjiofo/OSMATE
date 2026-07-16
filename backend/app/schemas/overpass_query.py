from pydantic import BaseModel

from app.schemas.agent_plan import AgentPlan


class OverpassQueryResponse(BaseModel):
    """Antwortmodell fuer eine generierte Overpass-QL-Abfrage."""

    agent_plan: AgentPlan
    overpass_ql: str
