from fastapi import APIRouter, status
from fastapi.responses import JSONResponse

from app.agent.planner import PlanningError, create_agent_plan
from app.agent.validator import PlanValidationError
from app.schemas.agent_plan import AgentPlan
from app.schemas.error_response import ErrorResponse
from app.schemas.overpass_query import OverpassQueryResponse
from app.schemas.search_request import SearchRequest
from app.services.overpass_builder import OverpassBuildError, build_overpass_query

router = APIRouter(prefix="/search", tags=["search"])

PLANNING_ERROR_RESPONSES = {
    status.HTTP_422_UNPROCESSABLE_CONTENT: {
        "model": ErrorResponse,
        "description": (
            "Die Suchanfrage konnte nicht in einen validen "
            "Agentenplan umgewandelt werden."
        ),
    }
}


def _planning_error_response(exc: Exception) -> JSONResponse:
    """Erstellt eine standardisierte Fehlerantwort fuer Planungsfehler."""
    error = ErrorResponse(
        code="planning_error",
        message=str(exc),
    )

    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_CONTENT,
        content=error.model_dump(),
    )


@router.post(
    "/plan",
    response_model=AgentPlan,
    responses=PLANNING_ERROR_RESPONSES,
)
async def create_search_plan(request_data: SearchRequest) -> AgentPlan | JSONResponse:
    """Erstellt einen validierten Agentenplan aus einer natuerlichen Suchanfrage."""
    try:
        return create_agent_plan(request_data)

    except (PlanningError, PlanValidationError) as exc:
        return _planning_error_response(exc)


@router.post(
    "/overpass-query",
    response_model=OverpassQueryResponse,
    responses=PLANNING_ERROR_RESPONSES,
)
async def create_search_overpass_query(
    request_data: SearchRequest,
) -> OverpassQueryResponse | JSONResponse:
    """Erstellt einen Agentenplan und leitet daraus eine Overpass-QL-Abfrage ab."""
    try:
        agent_plan = create_agent_plan(request_data)
        overpass_ql = build_overpass_query(agent_plan)

        return OverpassQueryResponse(
            agent_plan=agent_plan,
            overpass_ql=overpass_ql,
        )

    except (PlanningError, PlanValidationError, OverpassBuildError) as exc:
        return _planning_error_response(exc)
