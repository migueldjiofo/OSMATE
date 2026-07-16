import asyncio

import httpx
import pytest

from app.services.nominatim_client import (
    NominatimNoResultError,
    geocode_place_name,
)


def test_geocode_place_name_with_mocked_success_response() -> None:
    async def run_test():
        def handler(request: httpx.Request) -> httpx.Response:
            assert request.method == "GET"
            return httpx.Response(
                status_code=200,
                json=[
                    {
                        "lat": "52.520000",
                        "lon": "13.405000",
                    }
                ],
            )

        transport = httpx.MockTransport(handler)

        async with httpx.AsyncClient(transport=transport) as client:
            return await geocode_place_name(
                "Berlin Alexanderplatz",
                client=client,
                nominatim_url="https://nominatim.test",
            )

    result = asyncio.run(run_test())

    assert result.lat == 52.52
    assert result.lon == 13.405


def test_geocode_place_name_raises_no_result_error() -> None:
    async def run_test() -> None:
        def handler(request: httpx.Request) -> httpx.Response:
            assert request.method == "GET"
            return httpx.Response(
                status_code=200,
                json=[],
            )

        transport = httpx.MockTransport(handler)

        async with httpx.AsyncClient(transport=transport) as client:
            await geocode_place_name(
                "Unbekannter Testort",
                client=client,
                nominatim_url="https://nominatim.test",
            )

    with pytest.raises(NominatimNoResultError):
        asyncio.run(run_test())
