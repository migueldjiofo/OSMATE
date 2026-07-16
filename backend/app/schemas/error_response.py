from pydantic import BaseModel


class ErrorResponse(BaseModel):
    """Standardisiertes Fehlermodell fuer API-Antworten."""

    code: str
    message: str
