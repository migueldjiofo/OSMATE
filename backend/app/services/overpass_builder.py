import re

from app.schemas.agent_plan import AgentPlan

SAFE_TAG_PATTERN = re.compile(r"^[A-Za-z0-9:_-]+$")


class OverpassBuildError(ValueError):
    """Fehler beim Erzeugen einer Overpass-QL-Abfrage."""


def _validate_tag_key(key: str) -> None:
    """Validiert einen OSM-Tag-Schluessel fuer die sichere Query-Erzeugung."""
    if not SAFE_TAG_PATTERN.match(key):
        raise OverpassBuildError(f"Ungueltiger OSM-Tag-Schluessel: {key}")


def _escape_tag_value(value: str) -> str:
    """Escaped OSM-Tag-Werte fuer Overpass QL."""
    return value.replace("\\", "\\\\").replace('"', '\\"')


def _format_tag_filters(osm_tags: dict[str, str]) -> str:
    """Formatiert OSM-Tags als Overpass-Filter."""
    if not osm_tags:
        raise OverpassBuildError("Ein Overpass-Query benoetigt mindestens ein OSM-Tag.")

    filters: list[str] = []

    for key, value in osm_tags.items():
        _validate_tag_key(key)
        escaped_value = _escape_tag_value(value)
        filters.append(f'["{key}"="{escaped_value}"]')

    return "".join(filters)


def build_overpass_query(plan: AgentPlan) -> str:
    """Erzeugt eine Overpass-QL-Abfrage aus einem validierten Agentenplan."""
    if plan.spatial_filter.type != "radius":
        raise OverpassBuildError("Aktuell wird nur der raeumliche Filter 'radius' unterstuetzt.")

    tag_filters = _format_tag_filters(plan.osm_tags)
    radius_m = plan.spatial_filter.radius_m
    lat = plan.spatial_filter.lat
    lon = plan.spatial_filter.lon
    limit = plan.limit

    around_filter = f"(around:{radius_m},{lat:.6f},{lon:.6f})"

    return "\n".join(
        [
            "[out:json][timeout:25];",
            "(",
            f"  node{tag_filters}{around_filter};",
            f"  way{tag_filters}{around_filter};",
            f"  relation{tag_filters}{around_filter};",
            ");",
            f"out center {limit};",
        ]
    )
