from pydantic import BaseModel


class HealthResponse(BaseModel):
    """Antwortmodell fuer den technischen Gesundheitsstatus des Backends."""

    status: str
    service: str
    environment: str
