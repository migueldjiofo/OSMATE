from pydantic import BaseModel, Field


class SearchRequest(BaseModel):
    """Eingabemodell fuer eine raeumliche Suchanfrage."""

    query: str = Field(min_length=2, max_length=500)
    lat: float | None = Field(default=None, ge=-90, le=90)
    lon: float | None = Field(default=None, ge=-180, le=180)
    place_name: str | None = Field(default=None, max_length=200)
    radius_m: int | None = Field(default=None, ge=50, le=10000)
    limit: int | None = Field(default=None, ge=1, le=500)
    language: str = Field(default="de", min_length=2, max_length=10)
