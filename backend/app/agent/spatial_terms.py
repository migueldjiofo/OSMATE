from __future__ import annotations

import re

NUMBER_WORDS_DE: dict[str, int] = {
    "ein": 1,
    "eine": 1,
    "einen": 1,
    "einem": 1,
    "zwei": 2,
    "drei": 3,
    "vier": 4,
    "fuenf": 5,
    "funf": 5,
    "fünf": 5,
    "sechs": 6,
    "sieben": 7,
    "acht": 8,
    "neun": 9,
    "zehn": 10,
}


def normalize_spatial_text(value: str) -> str:
    text = value.lower()
    text = text.replace("ä", "ae")
    text = text.replace("ö", "oe")
    text = text.replace("ü", "ue")
    text = text.replace("ß", "ss")
    return re.sub(r"\s+", " ", text).strip()


def _parse_number(value: str) -> int | None:
    normalized_value = normalize_spatial_text(value)

    if normalized_value.isdigit():
        return int(normalized_value)

    return NUMBER_WORDS_DE.get(normalized_value)


def extract_radius_meters(
    query: str,
    fallback_radius_m: int | None,
    default_radius_m: int,
) -> int:
    normalized_query = normalize_spatial_text(query)

    kilometer_pattern = re.search(
        r"(?:umkreis|radius|entfernung)\s+von\s+([0-9]+|ein|eine|einen|einem|zwei|drei|vier|fuenf|funf|fünf|sechs|sieben|acht|neun|zehn)\s*(kilometer|kilometern|km)",
        normalized_query,
    )
    if kilometer_pattern:
        number = _parse_number(kilometer_pattern.group(1))
        if number is not None:
            return number * 1000

    meter_pattern = re.search(
        r"(?:umkreis|radius|entfernung)\s+von\s+([0-9]+|ein|eine|einen|einem|zwei|drei|vier|fuenf|funf|fünf|sechs|sieben|acht|neun|zehn)\s*(meter|metern|m)",
        normalized_query,
    )
    if meter_pattern:
        number = _parse_number(meter_pattern.group(1))
        if number is not None:
            return number

    compact_kilometer_pattern = re.search(r"([0-9]+)\s*km", normalized_query)
    if compact_kilometer_pattern:
        return int(compact_kilometer_pattern.group(1)) * 1000

    compact_meter_pattern = re.search(r"([0-9]+)\s*m", normalized_query)
    if compact_meter_pattern:
        return int(compact_meter_pattern.group(1))

    return fallback_radius_m or default_radius_m