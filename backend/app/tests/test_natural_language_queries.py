from app.agent.planner import create_agent_plan
from app.schemas.search_request import SearchRequest


def _plan_for(query: str):
    request = SearchRequest(
        query=query,
        lat=52.5214574,
        lon=13.4110794,
        radius_m=1000,
        limit=10,
        language="de",
    )
    return create_agent_plan(request)


def test_detects_cafe_with_outdoor_seating():
    plan = _plan_for("Finde Cafes mit Terrasse")

    assert plan.category == "cafe"
    assert plan.osm_tags["amenity"] == "cafe"
    assert plan.osm_tags["outdoor_seating"] == "yes"


def test_detects_spaeti():
    plan = _plan_for("Finde Spätis in meiner Nähe")

    assert plan.category == "spaeti"
    assert plan.osm_tags["shop"] == "convenience"


def test_detects_playground_with_water_play():
    plan = _plan_for("Finde Spielplätze mit Wasserspielen")

    assert plan.category == "playground"
    assert plan.osm_tags["leisure"] == "playground"
    assert plan.osm_tags.get("playground") == "water" or plan.osm_tags.get("water_play") == "yes"


def test_detects_bakery():
    plan = _plan_for("Finde Bäckereien im Umkreis von 500 Metern")

    assert plan.category == "bakery"
    assert plan.osm_tags["shop"] == "bakery"


def test_detects_pharmacy():
    plan = _plan_for("Finde Apotheken in Berlin-Mitte")

    assert plan.category == "pharmacy"
    assert plan.osm_tags["amenity"] == "pharmacy"


def test_detects_restaurant_with_vegan_hint():
    plan = _plan_for("Finde Restaurants mit veganem Angebot")

    assert plan.category == "restaurant"
    assert plan.osm_tags["amenity"] == "restaurant"
    assert plan.osm_tags["diet:vegan"] == "yes"