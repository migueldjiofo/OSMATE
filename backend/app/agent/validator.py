from app.agent.tag_catalog import get_allowed_tags
from app.schemas.agent_plan import AgentPlan


class PlanValidationError(ValueError):
    """Fehler bei der Validierung eines Agentenplans."""


def validate_agent_plan(
    plan: AgentPlan,
    max_radius_m: int,
    max_limit: int,
) -> None:
    """Validiert einen strukturierten Agentenplan vor der Weiterverarbeitung."""
    if plan.spatial_filter.radius_m > max_radius_m:
        raise PlanValidationError(
            f"Der Suchradius darf maximal {max_radius_m} Meter betragen."
        )

    if plan.limit > max_limit:
        raise PlanValidationError(
            f"Die Ergebnisanzahl darf maximal {max_limit} betragen."
        )

    allowed_tags = get_allowed_tags()

    for key, value in plan.osm_tags.items():
        if (key, value) not in allowed_tags:
            raise PlanValidationError(
                f"Das OSM-Tag {key}={value} ist fuer den regelbasierten Agenten nicht erlaubt."
            )
