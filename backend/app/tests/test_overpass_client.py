import asyncio

import httpx
import pytest

from app.services.overpass_client import (
    OverpassRateLimitError,
    execute_overpass_query,
)


def test_execute_overpass_query_with_mocked_success_response() -> None:
    async def run_test() -> dict:
        def handler(request: httpx.Request) -> httpx.Response:
            assert request.method == "POST"
            return httpx.Response(
                status_code=200,
                json={
                    "elements": [],
                },
            )

        transport = httpx.MockTransport(handler)

        async with httpx.AsyncClient(transport=transport) as client:
            return await execute_overpass_query(
                "[out:json];node(1);out;",
                client=client,
                overpass_url="https://overpass.test/api/interpreter",
            )

    payload = asyncio.run(run_test())

    assert payload["elements"] == []


def test_execute_overpass_query_handles_rate_limit_response() -> None:
    async def run_test() -> None:
        def handler(request: httpx.Request) -> httpx.Response:
            assert request.method == "POST"
            return httpx.Response(status_code=429)

        transport = httpx.MockTransport(handler)

        async with httpx.AsyncClient(transport=transport) as client:
            await execute_overpass_query(
                "[out:json];node(1);out;",
                client=client,
                overpass_url="https://overpass.test/api/interpreter",
            )

    with pytest.raises(OverpassRateLimitError):
        asyncio.run(run_test())
