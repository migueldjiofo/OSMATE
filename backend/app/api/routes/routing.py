from fastapi import APIRouter

from app.schemas.routing import RoutingRequest, RoutingResponse
from app.services.routing_service import calculate_route

router = APIRouter()


@router.post("/route", response_model=RoutingResponse)
async def create_route(
    request: RoutingRequest,
) -> RoutingResponse:
    return await calculate_route(request)