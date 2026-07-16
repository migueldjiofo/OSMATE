from app.agent.planner import PlanningError
from app.schemas.search_request import SearchRequest
from app.services.nominatim_client import (
    NominatimNoResultError,
    geocode_place_name,
)


async def resolve_spatial_context(request_data: SearchRequest) -> SearchRequest:
    """Stellt sicher, dass eine Suchanfrage nutzbare Koordinaten enthaelt."""
    has_lat = request_data.lat is not None
    has_lon = request_data.lon is not None

    if has_lat and has_lon:
        return request_data

    if has_lat != has_lon:
        raise PlanningError(
            "Latitude und Longitude muessen gemeinsam angegeben werden."
        )

    if request_data.place_name is None or request_data.place_name.strip() == "":
        raise PlanningError(
            "Es muessen entweder Koordinaten oder ein Ortsname angegeben werden."
        )

    try:
        geocoding_result = await geocode_place_name(request_data.place_name)

    except NominatimNoResultError as exc:
        raise PlanningError(str(exc)) from exc

    return request_data.model_copy(
        update={
            "lat": geocoding_result.lat,
            "lon": geocoding_result.lon,
        }
    )
