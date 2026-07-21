import math
import os
from typing import Any

import httpx

from app.schemas.routing import RoutingRequest, RoutingResponse

EARTH_RADIUS_METERS = 6_371_000.0

MODE_TO_OSRM_PROFILE = {
    "walking": "foot",
    "bicycle": "bike",
    "car": "driving",
}

MODE_SPEED_KMH = {
    "walking": 5.0,
    "bicycle": 15.0,
    "car": 35.0,
}


async def calculate_route(request: RoutingRequest) -> RoutingResponse:
    return await calculate_osrm_route(request)


async def calculate_osrm_route(request: RoutingRequest) -> RoutingResponse:
    base_url = os.getenv(
        "OSMATE_ROUTING_BASE_URL",
        "https://router.project-osrm.org",
    ).rstrip("/")

    profile = MODE_TO_OSRM_PROFILE[request.mode]

    coordinates = (
        f"{request.start_lon},{request.start_lat};"
        f"{request.destination_lon},{request.destination_lat}"
    )

    url = f"{base_url}/route/v1/{profile}/{coordinates}"

    params = {
        "overview": "full",
        "geometries": "geojson",
        "steps": "false",
    }

    try:
        async with httpx.AsyncClient(timeout=15.0) as client:
            response = await client.get(
                url,
                params=params,
            )

        response.raise_for_status()
        payload = response.json()

        if payload.get("code") != "Ok" or not payload.get("routes"):
            return create_direct_fallback_route(
                request=request,
                warning="Routing-Service konnte keine Route berechnen.",
            )

        route = payload["routes"][0]
        geometry = route.get("geometry")

        if not isinstance(geometry, dict):
            return create_direct_fallback_route(
                request=request,
                warning="Routing-Service hat keine GeoJSON-Geometrie geliefert.",
            )

        distance_m = int(round(float(route.get("distance", 0.0))))
        duration_s = int(round(float(route.get("duration", 0.0))))

        return RoutingResponse(
            mode=request.mode,
            provider="osrm",
            distance_m=distance_m,
            duration_s=duration_s,
            distance_text=format_distance(distance_m),
            duration_text=format_duration(duration_s),
            geojson=create_route_feature_collection(
                geometry=geometry,
                mode=request.mode,
                provider="osrm",
                distance_m=distance_m,
                duration_s=duration_s,
            ),
            warnings=[],
        )
    except Exception as error:
        return create_direct_fallback_route(
            request=request,
            warning=f"Routing-Service nicht erreichbar: {error}",
        )


def create_direct_fallback_route(
    request: RoutingRequest,
    warning: str,
) -> RoutingResponse:
    distance_m = int(
        round(
            calculate_direct_distance_meters(
                start_lat=request.start_lat,
                start_lon=request.start_lon,
                destination_lat=request.destination_lat,
                destination_lon=request.destination_lon,
            )
        )
    )

    speed_kmh = MODE_SPEED_KMH[request.mode]
    duration_s = int(round((distance_m / 1000.0) / speed_kmh * 3600.0))

    geometry = {
        "type": "LineString",
        "coordinates": [
            [request.start_lon, request.start_lat],
            [request.destination_lon, request.destination_lat],
        ],
    }

    return RoutingResponse(
        mode=request.mode,
        provider="direct_fallback",
        distance_m=distance_m,
        duration_s=max(60, duration_s),
        distance_text=format_distance(distance_m),
        duration_text=format_duration(max(60, duration_s)),
        geojson=create_route_feature_collection(
            geometry=geometry,
            mode=request.mode,
            provider="direct_fallback",
            distance_m=distance_m,
            duration_s=max(60, duration_s),
        ),
        warnings=[warning],
    )


def create_route_feature_collection(
    geometry: dict[str, Any],
    mode: str,
    provider: str,
    distance_m: int,
    duration_s: int,
) -> dict[str, Any]:
    return {
        "type": "FeatureCollection",
        "features": [
            {
                "type": "Feature",
                "properties": {
                    "mode": mode,
                    "provider": provider,
                    "distance_m": distance_m,
                    "duration_s": duration_s,
                },
                "geometry": geometry,
            }
        ],
    }


def calculate_direct_distance_meters(
    start_lat: float,
    start_lon: float,
    destination_lat: float,
    destination_lon: float,
) -> float:
    start_lat_rad = math.radians(start_lat)
    destination_lat_rad = math.radians(destination_lat)
    delta_lat_rad = math.radians(destination_lat - start_lat)
    delta_lon_rad = math.radians(destination_lon - start_lon)

    a = (
        math.sin(delta_lat_rad / 2) * math.sin(delta_lat_rad / 2)
        + math.cos(start_lat_rad)
        * math.cos(destination_lat_rad)
        * math.sin(delta_lon_rad / 2)
        * math.sin(delta_lon_rad / 2)
    )

    c = 2 * math.atan2(
        math.sqrt(a),
        math.sqrt(1 - a),
    )

    return EARTH_RADIUS_METERS * c


def format_distance(distance_m: int) -> str:
    if distance_m < 1000:
        return f"{distance_m} m"

    return f"{distance_m / 1000.0:.1f} km"


def format_duration(duration_s: int) -> str:
    duration_minutes = max(1, round(duration_s / 60))

    if duration_minutes < 60:
        return f"{duration_minutes} min"

    hours = duration_minutes // 60
    minutes = duration_minutes % 60

    return f"{hours} h {minutes} min"