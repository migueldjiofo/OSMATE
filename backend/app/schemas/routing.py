from typing import Any, Literal

from pydantic import BaseModel, Field


TravelMode = Literal["walking", "bicycle", "car"]


class RoutingRequest(BaseModel):
    start_lat: float = Field(..., ge=-90.0, le=90.0)
    start_lon: float = Field(..., ge=-180.0, le=180.0)
    destination_lat: float = Field(..., ge=-90.0, le=90.0)
    destination_lon: float = Field(..., ge=-180.0, le=180.0)
    mode: TravelMode = "walking"


class RoutingResponse(BaseModel):
    mode: TravelMode
    provider: str
    distance_m: int
    duration_s: int
    distance_text: str
    duration_text: str
    geojson: dict[str, Any]
    warnings: list[str] = []