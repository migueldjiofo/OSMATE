from app.services.cache_service import get_cached_json, make_cache_key, set_cached_json


def test_cache_service_stores_and_reads_json_payload(tmp_path) -> None:
    database_path = tmp_path / "cache.sqlite3"
    cache_key = make_cache_key(
        "test",
        {
            "query": "Finde Cafes",
            "lat": 52.52,
        },
    )

    payload = {
        "elements": [
            {
                "id": 1,
                "type": "node",
            }
        ]
    }

    set_cached_json(
        cache_key=cache_key,
        payload=payload,
        ttl_seconds=60,
        database_path=database_path,
    )

    cached_payload = get_cached_json(
        cache_key=cache_key,
        database_path=database_path,
    )

    assert cached_payload == payload


def test_cache_service_returns_none_for_expired_payload(tmp_path) -> None:
    database_path = tmp_path / "cache.sqlite3"
    cache_key = make_cache_key("test", "expired-query")

    set_cached_json(
        cache_key=cache_key,
        payload={"value": "expired"},
        ttl_seconds=-1,
        database_path=database_path,
    )

    cached_payload = get_cached_json(
        cache_key=cache_key,
        database_path=database_path,
    )

    assert cached_payload is None


def test_cache_key_is_stable_for_same_payload() -> None:
    first_key = make_cache_key(
        "test",
        {
            "b": 2,
            "a": 1,
        },
    )

    second_key = make_cache_key(
        "test",
        {
            "a": 1,
            "b": 2,
        },
    )

    assert first_key == second_key
