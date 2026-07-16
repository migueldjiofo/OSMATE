from fastapi import APIRouter, status
from fastapi.responses import JSONResponse

from app.agent.planner import PlanningError, create_agent_plan
from app.agent.validator import PlanValidationError
from app.schemas.agent_plan import AgentPlan
from app.schemas.error_response import ErrorResponse
from app.schemas.search_request import SearchRequest

router = APIRouter(prefix="/search", tags=["search"])


@router.post(
    "/plan",
    response_model=AgentPlan,
    responses={
        status.HTTP_422_UNPROCESSABLE_ENTITY: {
            "model": ErrorResponse,
            "description": (
                "Die Suchanfrage konnte nicht in einen validen "
                "Agentenplan umgewandelt werden."
            ),
        }
    },
)
async def create_search_plan(request_data: SearchRequest) -> AgentPlan | JSONResponse:
    """Erstellt einen validierten Agentenplan aus einer natuerlichen Suchanfrage."""
    try:
        return create_agent_plan(request_data)

    except (PlanningError, PlanValidationError) as exc:
        error = ErrorResponse(
            code="planning_error",
            message=str(exc),
        )

        return JSONResponse(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            content=error.model_dump(),
        )