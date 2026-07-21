from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class OverpassValidationResult:
    is_valid: bool
    errors: list[str]
    corrected_query: str | None = None


def validate_overpass_query(overpass_ql: str) -> OverpassValidationResult:
    errors: list[str] = []
    query = overpass_ql.strip()

    if not query:
        errors.append("Die Overpass-QL-Anfrage ist leer.")

    if "[out:json]" not in query:
        errors.append("Die Overpass-QL-Anfrage enthaelt kein JSON-Ausgabeformat.")

    if "out center" not in query and "out body" not in query:
        errors.append("Die Overpass-QL-Anfrage enthaelt keine gueltige Ausgabeanweisung.")

    if query.count("(") != query.count(")"):
        errors.append("Die Klammerstruktur der Overpass-QL-Anfrage ist ungueltig.")

    if query.count("[") != query.count("]"):
        errors.append("Die Tag-Filter-Struktur der Overpass-QL-Anfrage ist ungueltig.")

    if "{{" in query or "}}" in query:
        errors.append("Die Overpass-QL-Anfrage enthaelt nicht ersetzte Platzhalter.")

    if errors:
        return OverpassValidationResult(is_valid=False, errors=errors)

    return OverpassValidationResult(is_valid=True, errors=[])


def correct_overpass_query(overpass_ql: str) -> OverpassValidationResult:
    query = overpass_ql.strip()

    if query and "[out:json]" not in query:
        query = "[out:json][timeout:25];\n" + query

    if query and "out center" not in query and "out body" not in query:
        query = query.rstrip()
        if not query.endswith(";"):
            query = query + ";"
        query = query + "\nout center;"

    validation_result = validate_overpass_query(query)

    if validation_result.is_valid:
        return OverpassValidationResult(
            is_valid=True,
            errors=[],
            corrected_query=query,
        )

    return OverpassValidationResult(
        is_valid=False,
        errors=validation_result.errors,
        corrected_query=query,
    )


def validate_or_correct_overpass_query(overpass_ql: str) -> OverpassValidationResult:
    validation_result = validate_overpass_query(overpass_ql)

    if validation_result.is_valid:
        return validation_result

    return correct_overpass_query(overpass_ql)