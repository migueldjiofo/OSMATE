import pytest

from app.services.overpass_builder import OverpassBuildError, build_overpass_query
from app.schemas.agent_plan import AgentPlan, SpatialFilter


def test_build_overpass_query_from_agent_plan() -> None:
    plan = AgentPlan(
        category="cafe",
        osm_tags={
            "amenity": "cafe",
            "outdoor_seating": "yes",
        },
        spatial_filter=SpatialFilter(
            radius_m=1000,
            lat=52.52,
            lon=13.405,
        ),
        limit=10,
        confidence_score=0.9,
        summary_de="Regelbasierte Suche nach cafe im Umkreis von 1000 Metern.",
    )

    overpass_ql = build_overpass_query(plan)

    assert "[out:json][timeout:25];" in overpass_ql
    assert 'node["amenity"="cafe"]["outdoor_seating"="yes"]' in overpass_ql
    assert 'way["amenity"="cafe"]["outdoor_seating"="yes"]' in overpass_ql
    assert 'relation["amenity"="cafe"]["outdoor_seating"="yes"]' in overpass_ql
    assert "(around:1000,52.520000,13.405000)" in overpass_ql
    assert "out center 10;" in overpass_ql


def test_build_overpass_query_rejects_invalid_tag_key() -> None:
    plan = AgentPlan(
        category="invalid",
        osm_tags={
            'amenity"]->.x;': "cafe",
        },
        spatial_filter=SpatialFilter(
            radius_m=1000,
            lat=52.52,
            lon=13.405,
        ),
        limit=10,
        confidence_score=0.9,
        summary_de="Ungueltiger Testplan.",
    )

    with pytest.raises(OverpassBuildError):
        build_overpass_query(plan)
