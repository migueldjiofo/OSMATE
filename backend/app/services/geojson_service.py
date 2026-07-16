from typing import Any


def _extract_coordinates(element: dict[str, Any]) -> list[float] | None:
    """Extrahiert Punktkoordinaten aus einem Overpass-Element."""
    if element.get("type") == "node":
        lat = element.get("lat")
        lon = element.get("lon")
    else:
        center = element.get("center", {})
        lat = center.get("lat")
        lon = center.get("lon")

    if not isinstance(lat, (int, float)) or not isinstance(lon, (int, float)):
        return None

    return [float(lon), float(lat)]


def _element_to_feature(element: dict[str, Any]) -> dict[str, Any] | None:
    """Wandelt ein Overpass-Element in ein GeoJSON-Feature um."""
    coordinates = _extract_coordinates(element)

    if coordinates is None:
        return None

    tags = element.get("tags", {})
    if not isinstance(tags, dict):
        tags = {}

    osm_type = element.get("type", "unknown")
    osm_id = element.get("id", "unknown")

    return {
        "type": "Feature",
        "id": f"{osm_type}/{osm_id}",
        "geometry": {
            "type": "Point",
            "coordinates": coordinates,
        },
        "properties": {
            "osm_id": osm_id,
            "osm_type": osm_type,
            "name": tags.get("name"),
            "tags": tags,
        },
    }


def convert_overpass_to_geojson(overpass_payload: dict[str, Any]) -> dict[str, Any]:
    """Konvertiert eine Overpass-Antwort in eine GeoJSON-FeatureCollection."""
    elements = overpass_payload.get("elements", [])

    if not isinstance(elements, list):
        elements = []

    features = []

    for element in elements:
        if not isinstance(element, dict):
            continue

        feature = _element_to_feature(element)

        if feature is not None:
            features.append(feature)

    return {
        "type": "FeatureCollection",
        "features": features,
    }
