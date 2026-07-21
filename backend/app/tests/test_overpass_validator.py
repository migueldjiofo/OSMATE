from app.services.overpass_validator import (
    correct_overpass_query,
    validate_or_correct_overpass_query,
    validate_overpass_query,
)


def test_valid_overpass_query_is_accepted():
    query = """
[out:json][timeout:25];
(
  node["amenity"="cafe"](around:1000,52.52,13.405);
);
out center;
"""

    result = validate_overpass_query(query)

    assert result.is_valid is True
    assert result.errors == []


def test_empty_overpass_query_is_rejected():
    result = validate_overpass_query("")

    assert result.is_valid is False
    assert len(result.errors) >= 1


def test_missing_output_format_can_be_corrected():
    query = """
(
  node["amenity"="cafe"](around:1000,52.52,13.405);
);
out center;
"""

    result = correct_overpass_query(query)

    assert result.is_valid is True
    assert result.corrected_query is not None
    assert "[out:json]" in result.corrected_query


def test_missing_out_statement_can_be_corrected():
    query = """
[out:json][timeout:25];
(
  node["amenity"="cafe"](around:1000,52.52,13.405);
);
"""

    result = validate_or_correct_overpass_query(query)

    assert result.is_valid is True
    assert result.corrected_query is not None
    assert "out center" in result.corrected_query


def test_unbalanced_brackets_are_rejected():
    query = """
[out:json][timeout:25];
(
  node["amenity"="cafe"(around:1000,52.52,13.405);
);
out center;
"""

    result = validate_or_correct_overpass_query(query)

    assert result.is_valid is False
    assert len(result.errors) >= 1