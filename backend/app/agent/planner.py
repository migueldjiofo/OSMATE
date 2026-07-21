from __future__ import annotations

from app.agent.tag_catalog import (
    detect_optional_tags,
    extract_unmatched_terms,
    find_category,
)
from app.core.config import get_settings
from app.schemas.agent_plan import AgentPlan
from app.schemas.search_request import SearchRequest


class PlanningError(Exception):
    pass


def create_agent_plan(request_data: SearchRequest) -> AgentPlan:
    """Erstellt einen strukturierten Suchplan aus einer natuerlichen Anfrage."""
    settings = get_settings()

    if request_data.lat is None or request_data.lon is None:
        raise PlanningError(
            "Fuer den regelbasierten Agenten muessen Koordinaten vorhanden sein."
        )

    category, category_terms = find_category(request_data.query)

    if category is None:
        raise PlanningError(
            "Die Anfrage konnte keiner unterstuetzten OSM-Kategorie zugeordnet werden."
        )

    optional_tags, optional_terms, warnings, unsupported_terms = detect_optional_tags(
        request_data.query
    )

    osm_tags = dict(category.tags)
    osm_tags.update(optional_tags)

    unmatched_terms = extract_unmatched_terms(
        request_data.query,
        matched_terms=category_terms,
        optional_matched_terms=optional_terms,
        unsupported_terms=unsupported_terms,
    )

    for term in unsupported_terms:
        if term not in unmatched_terms:
            unmatched_terms.append(term)

    llm_fallback_recommended = bool(unsupported_terms or unmatched_terms)

    confidence_score = category.confidence_score
    if unsupported_terms:
        confidence_score = min(confidence_score, 0.68)
    if unmatched_terms:
        confidence_score = min(confidence_score, 0.72)

    spatial_filter = {
        "type": "radius",
        "radius_m": request_data.radius_m or settings.default_radius_m,
        "lat": request_data.lat,
        "lon": request_data.lon,
    }

    planner_steps = [
        f"Nutzeranfrage analysiert: {request_data.query}",
        f"Kategorie erkannt: {category.category}",
        "OpenStreetMap-Tags gesetzt: "
        + ", ".join(f"{key}={value}" for key, value in osm_tags.items()),
        f"Raeumlicher Suchfilter gesetzt: Radius {spatial_filter['radius_m']} Meter",
    ]

    if unmatched_terms:
        planner_steps.append(
            "Nicht eindeutig abbildbare Begriffe erkannt: "
            + ", ".join(unmatched_terms)
        )

    if warnings:
        planner_steps.append(f"Hinweise erzeugt: {len(warnings)}")

    return AgentPlan(
        intent="poi_search",
        category=category.category,
        osm_tags=osm_tags,
        spatial_filter=spatial_filter,
        limit=request_data.limit,
        confidence_score=confidence_score,
        unmatched_terms=unmatched_terms,
        warnings=warnings,
        summary_de=(
            f"Regelbasierte Suche nach {category.category} "
            f"im Umkreis von {spatial_filter['radius_m']} Metern."
        ),
        planner_type="rule_based",
        llm_fallback_recommended=llm_fallback_recommended,
        planner_steps=planner_steps,
    )