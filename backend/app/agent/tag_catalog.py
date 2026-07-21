from __future__ import annotations

import re
import unicodedata
from dataclasses import dataclass
from typing import Iterable


@dataclass(frozen=True)
class CategoryRule:
    category: str
    tags: dict[str, str]
    keywords: tuple[str, ...]
    summary_label_de: str
    confidence_score: float = 0.82

    @property
    def key(self) -> str:
        return self.category

    @property
    def osm_tags(self) -> dict[str, str]:
        return self.tags

    @property
    def confidence(self) -> float:
        return self.confidence_score

    @property
    def label(self) -> str:
        return self.summary_label_de


@dataclass(frozen=True)
class OptionalTagRule:
    tags: dict[str, str]
    keywords: tuple[str, ...]
    matched_label: str
    warning: str | None = None


CATEGORY_RULES: tuple[CategoryRule, ...] = (
    CategoryRule(
        category="cafe",
        tags={"amenity": "cafe"},
        keywords=("cafe", "cafes", "cafÃƒÆ’Ã‚Â©", "cafÃƒÆ’Ã‚Â©s", "kaffee", "kaffeebar"),
        summary_label_de="Cafe",
        confidence_score=0.90,
    ),
    CategoryRule(
        category="spaeti",
        tags={"shop": "convenience"},
        keywords=("spaeti", "spaetis", "spati", "spatis", "spÃƒÆ’Ã‚Â¤ti", "spÃƒÆ’Ã‚Â¤tis", "kiosk", "kioske", "convenience store", "late shop"),
        summary_label_de="SpÃƒÆ’Ã‚Â¤ti",
        confidence_score=0.86,
    ),
    CategoryRule(
        category="restaurant",
        tags={"amenity": "restaurant"},
        keywords=("restaurant", "restaurants", "essen", "lokal", "lokale", "imbiss"),
        summary_label_de="Restaurant",
        confidence_score=0.82,
    ),
    CategoryRule(
        category="supermarket",
        tags={"shop": "supermarket"},
        keywords=("supermarkt", "supermaerkte", "supermÃƒÆ’Ã‚Â¤rkte", "lebensmittel", "grocery", "supermarket", "supermarkets"),
        summary_label_de="Supermarkt",
        confidence_score=0.82,
    ),
    CategoryRule(
        category="playground",
        tags={"leisure": "playground"},
        keywords=("spielplatz", "spielplaetze", "spielplÃƒÆ’Ã‚Â¤tze", "spielplaetzen", "spielplÃƒÆ’Ã‚Â¤tzen", "playground", "playgrounds", "kinderspielplatz"),
        summary_label_de="Spielplatz",
        confidence_score=0.86,
    ),
    CategoryRule(
        category="park",
        tags={"leisure": "park"},
        keywords=("park", "parks", "gruenanlage", "grÃƒÆ’Ã‚Â¼nanlage", "garten", "gaerten", "gÃƒÆ’Ã‚Â¤rten"),
        summary_label_de="Park",
        confidence_score=0.82,
    ),
    CategoryRule(
        category="pharmacy",
        tags={"amenity": "pharmacy"},
        keywords=("apotheke", "apotheken", "pharmacy", "pharmacies"),
        summary_label_de="Apotheke",
        confidence_score=0.82,
    ),
    CategoryRule(
        category="atm",
        tags={"amenity": "atm"},
        keywords=("geldautomat", "geldautomaten", "atm", "bankautomat", "bankautomaten"),
        summary_label_de="Geldautomat",
        confidence_score=0.82,
    ),
    CategoryRule(
        category="bakery",
        tags={"shop": "bakery"},
        keywords=("baeckerei", "baeckereien", "bÃƒÆ’Ã‚Â¤ckerei", "bÃƒÆ’Ã‚Â¤ckereien", "backerei", "backereien", "bakery", "bakeries"),
        summary_label_de="BÃƒÆ’Ã‚Â¤ckerei",
        confidence_score=0.84,
    ),
    CategoryRule(
        category="library",
        tags={"amenity": "library"},
        keywords=("bibliothek", "bibliotheken", "library", "libraries"),
        summary_label_de="Bibliothek",
        confidence_score=0.82,
    ),
    CategoryRule(
        category="hospital",
        tags={"amenity": "hospital"},
        keywords=("krankenhaus", "krankenhaeuser", "krankenhÃƒÆ’Ã‚Â¤user", "hospital", "hospitals", "klinik", "kliniken"),
        summary_label_de="Krankenhaus",
        confidence_score=0.82,
    ),
    CategoryRule(
        category="school",
        tags={"amenity": "school"},
        keywords=("schule", "schulen", "school", "schools"),
        summary_label_de="Schule",
        confidence_score=0.82,
    ),
    CategoryRule(
        category="charging_station",
        tags={"amenity": "charging_station"},
        keywords=("ladestation", "ladestationen", "ladesaeule", "ladesÃƒÆ’Ã‚Â¤ule", "ladesaeulen", "ladesÃƒÆ’Ã‚Â¤ulen", "charging station", "ev charging"),
        summary_label_de="Ladestation",
        confidence_score=0.82,
    ),
    CategoryRule(
        category="bus_stop",
        tags={"highway": "bus_stop"},
        keywords=("bushaltestelle", "bushaltestellen", "bus stop", "bus stops", "haltestelle", "haltestellen"),
        summary_label_de="Bushaltestelle",
        confidence_score=0.82,
    ),
)


OPTIONAL_TAG_RULES: tuple[OptionalTagRule, ...] = (
    OptionalTagRule(
        tags={"outdoor_seating": "yes"},
        keywords=("terrasse", "terasse", "aussensitzplaetze", "auÃƒÆ’Ã…Â¸ensitzplÃƒÆ’Ã‚Â¤tze", "draussen sitzen", "drauÃƒÆ’Ã…Â¸en sitzen", "outdoor seating"),
        matched_label="outdoor_seating",
    ),
    OptionalTagRule(
        tags={"internet_access": "wlan"},
        keywords=("wlan", "wifi", "wi-fi", "internet"),
        matched_label="internet_access",
    ),
    OptionalTagRule(
        tags={"wheelchair": "yes"},
        keywords=("barrierefrei", "rollstuhl", "wheelchair", "accessible"),
        matched_label="wheelchair",
    ),
    OptionalTagRule(
        tags={"diet:vegan": "yes"},
        keywords=("vegan", "veganes", "veganem", "vegane", "veganer"),
        matched_label="diet:vegan",
    ),
    OptionalTagRule(
        tags={"takeaway": "yes"},
        keywords=("takeaway", "mitnehmen", "zum mitnehmen", "to go"),
        matched_label="takeaway",
    ),
    OptionalTagRule(
        tags={"playground": "water"},
        keywords=("wasserspiel", "wasserspiele", "wasserspielen", "wasser spielplatz", "water play", "water playground"),
        matched_label="water_play",
    ),
)


UNSUPPORTED_SEMANTIC_TERMS: tuple[str, ...] = (
    "ruhig",
    "guenstig",
    "gÃƒÆ’Ã‚Â¼nstig",
    "billig",
    "cheap",
    "preiswert",
    "schoen",
    "schÃƒÆ’Ã‚Â¶n",
    "beliebt",
    "popular",
)


STOPWORDS: set[str] = {
    "finde",
    "finden",
    "suche",
    "suchen",
    "zeige",
    "zeig",
    "welche",
    "welcher",
    "welches",
    "mit",
    "ohne",
    "in",
    "im",
    "der",
    "die",
    "das",
    "den",
    "dem",
    "des",
    "von",
    "vom",
    "umkreis",
    "radius",
    "naehe",
    "nahe",
    "meiner",
    "meine",
    "mein",
    "einem",
    "eine",
    "einer",
    "meter",
    "metern",
    "kilometer",
    "kilometern",
    "km",
    "m",
    "und",
    "oder",
    "bitte",
    "angebot",
}


def normalize_text(value: str) -> str:
    text = value.lower()
    text = text.replace("ÃƒÆ’Ã‚Â¤", "ae")
    text = text.replace("ÃƒÆ’Ã‚Â¶", "oe")
    text = text.replace("ÃƒÆ’Ã‚Â¼", "ue")
    text = text.replace("ÃƒÆ’Ã…Â¸", "ss")
    text = unicodedata.normalize("NFKD", text)
    text = "".join(character for character in text if not unicodedata.combining(character))
    text = re.sub(r"[^a-z0-9:]+", " ", text)
    return re.sub(r"\s+", " ", text).strip()


def _contains_phrase(normalized_query: str, keyword: str) -> bool:
    normalized_keyword = normalize_text(keyword)
    if not normalized_keyword:
        return False

    pattern = rf"(?<![a-z0-9]){re.escape(normalized_keyword)}(?![a-z0-9])"
    return re.search(pattern, normalized_query) is not None


def _matched_keywords(normalized_query: str, keywords: Iterable[str]) -> tuple[str, ...]:
    matches: list[str] = []

    for keyword in keywords:
        normalized_keyword = normalize_text(keyword)
        if normalized_keyword and _contains_phrase(normalized_query, keyword):
            matches.append(normalized_keyword)

    return tuple(dict.fromkeys(matches))


def find_category(query: str) -> tuple[CategoryRule | None, tuple[str, ...]]:
    normalized_query = normalize_text(query)

    if any(
        term in normalized_query
        for term in (
            "spielplatz",
            "spielplaetze",
            "spielplaetzen",
            "kinderspielplatz",
            "playground",
            "playgrounds",
        )
    ):
        for rule in CATEGORY_RULES:
            if rule.category == "playground":
                return rule, ("spielplatz",)

    for rule in CATEGORY_RULES:
        matches = _matched_keywords(normalized_query, rule.keywords)
        if matches:
            return rule, matches

    return None, tuple()


def detect_optional_tags(query: str) -> tuple[dict[str, str], tuple[str, ...], list[str], tuple[str, ...]]:
    normalized_query = normalize_text(query)
    tags: dict[str, str] = {}
    matched_terms: list[str] = []
    warnings: list[str] = []
    unsupported_terms: list[str] = []

    for rule in OPTIONAL_TAG_RULES:
        matches = _matched_keywords(normalized_query, rule.keywords)
        if matches:
            tags.update(rule.tags)
            matched_terms.extend(matches)
            if rule.warning:
                warnings.append(rule.warning)

    for term in UNSUPPORTED_SEMANTIC_TERMS:
        normalized_term = normalize_text(term)
        if _contains_phrase(normalized_query, term):
            unsupported_terms.append(normalized_term)

    if unsupported_terms:
        warnings.append(
            "Einige qualitative Begriffe koennen regelbasiert nur eingeschraenkt bewertet werden."
        )

    return tags, tuple(dict.fromkeys(matched_terms)), warnings, tuple(dict.fromkeys(unsupported_terms))


def extract_unmatched_terms(
    query: str,
    matched_terms: Iterable[str] | None = None,
    optional_matched_terms: Iterable[str] | None = None,
    unsupported_terms: Iterable[str] | None = None,
    **kwargs: object,
) -> list[str]:
    normalized_query = normalize_text(query)
    tokens = normalized_query.split()

    all_matched_terms: set[str] = set()

    for group in (
        matched_terms,
        optional_matched_terms,
        unsupported_terms,
        kwargs.get("category_matched_terms") if isinstance(kwargs.get("category_matched_terms"), Iterable) else None,
        kwargs.get("optional_terms") if isinstance(kwargs.get("optional_terms"), Iterable) else None,
    ):
        if group is None:
            continue
        for term in group:
            if isinstance(term, str):
                all_matched_terms.update(normalize_text(term).split())

    unmatched: list[str] = []

    for token in tokens:
        if token in STOPWORDS:
            continue
        if token in all_matched_terms:
            continue
        if token.isdigit():
            continue
        unmatched.append(token)

    return list(dict.fromkeys(unmatched))


def get_allowed_tags() -> set[tuple[str, str]]:
    allowed_tags: set[tuple[str, str]] = set()

    for rule in CATEGORY_RULES:
        allowed_tags.update(rule.tags.items())

    for rule in OPTIONAL_TAG_RULES:
        allowed_tags.update(rule.tags.items())

    return allowed_tags


__all__ = [
    "CategoryRule",
    "OptionalTagRule",
    "CATEGORY_RULES",
    "OPTIONAL_TAG_RULES",
    "UNSUPPORTED_SEMANTIC_TERMS",
    "normalize_text",
    "find_category",
    "detect_optional_tags",
    "extract_unmatched_terms",
    "get_allowed_tags",
]