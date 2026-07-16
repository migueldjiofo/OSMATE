import re
from dataclasses import dataclass


@dataclass(frozen=True)
class OsmCategory:
    """Beschreibt eine unterstuetzte OSM-Kategorie."""

    key: str
    tags: dict[str, str]
    aliases: tuple[str, ...]


@dataclass(frozen=True)
class OptionalTagRule:
    """Beschreibt eine optionale Regel zur Erkennung zusaetzlicher OSM-Tags."""

    key: str
    value: str
    aliases: tuple[str, ...]


SUPPORTED_CATEGORIES: dict[str, OsmCategory] = {
    "cafe": OsmCategory(
        key="cafe",
        tags={"amenity": "cafe"},
        aliases=("cafe", "cafes", "café", "cafés", "kaffee", "coffee"),
    ),
    "spaeti": OsmCategory(
        key="spaeti",
        tags={"shop": "convenience"},
        aliases=("spaeti", "spaetkauf", "späti", "kiosk", "late shop", "convenience"),
    ),
    "restaurant": OsmCategory(
        key="restaurant",
        tags={"amenity": "restaurant"},
        aliases=("restaurant", "restaurants", "essen", "food"),
    ),
    "supermarket": OsmCategory(
        key="supermarket",
        tags={"shop": "supermarket"},
        aliases=("supermarkt", "supermarket", "lebensmittel"),
    ),
    "playground": OsmCategory(
        key="playground",
        tags={"leisure": "playground"},
        aliases=("spielplatz", "spielplaetze", "playground"),
    ),
    "park": OsmCategory(
        key="park",
        tags={"leisure": "park"},
        aliases=("park", "parks", "gruenanlage", "grünanlage"),
    ),
    "pharmacy": OsmCategory(
        key="pharmacy",
        tags={"amenity": "pharmacy"},
        aliases=("apotheke", "pharmacy"),
    ),
    "atm": OsmCategory(
        key="atm",
        tags={"amenity": "atm"},
        aliases=("geldautomat", "atm", "bankautomat"),
    ),
}

OPTIONAL_TAG_RULES: tuple[OptionalTagRule, ...] = (
    OptionalTagRule(
        key="outdoor_seating",
        value="yes",
        aliases=("terrasse", "terrace", "outdoor seating", "draussen", "draußen"),
    ),
    OptionalTagRule(
        key="internet_access",
        value="wlan",
        aliases=("wlan", "wifi", "wi-fi", "internet"),
    ),
    OptionalTagRule(
        key="wheelchair",
        value="yes",
        aliases=("barrierefrei", "rollstuhl", "wheelchair"),
    ),
    OptionalTagRule(
        key="diet:vegan",
        value="yes",
        aliases=("vegan", "vegane"),
    ),
    OptionalTagRule(
        key="takeaway",
        value="yes",
        aliases=("takeaway", "zum mitnehmen"),
    ),
    OptionalTagRule(
        key="smoking",
        value="no",
        aliases=("rauchfrei", "nicht rauchen", "nichtraucher"),
    ),
    OptionalTagRule(
        key="water_play",
        value="yes",
        aliases=("wasserspiel", "wasserspiele", "water play"),
    ),
)

UNSUPPORTED_SEMANTIC_TERMS: dict[str, str] = {
    "ruhig": "Der Begriff 'ruhig' ist in OSM nicht zuverlaessig als Standard-Tag abbildbar.",
    "calm": "Der Begriff 'calm' ist in OSM nicht zuverlaessig als Standard-Tag abbildbar.",
    "guenstig": "Der Begriff 'guenstig' ist in OSM nicht zuverlaessig als Standard-Tag abbildbar.",
    "günstig": "Der Begriff 'guenstig' ist in OSM nicht zuverlaessig als Standard-Tag abbildbar.",
    "billig": "Der Begriff 'billig' ist in OSM nicht zuverlaessig als Standard-Tag abbildbar.",
    "cheap": "Der Begriff 'cheap' ist in OSM nicht zuverlaessig als Standard-Tag abbildbar.",
    "preiswert": (
        "Der Begriff 'preiswert' ist in OSM nicht zuverlaessig "
        "als Standard-Tag abbildbar."
    ),
}

STOPWORDS: set[str] = {
    "finde",
    "suche",
    "zeig",
    "zeige",
    "mir",
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
    "meiner",
    "meine",
    "naehe",
    "nähe",
    "umkreis",
    "von",
    "und",
    "oder",
    "ein",
    "eine",
    "einen",
    "einem",
    "bitte",
    "near",
    "me",
    "with",
    "and",
    "around",
    "within",
    "km",
    "kilometer",
    "meter",
    "metern",
    "m",
}


def normalize_text(value: str) -> str:
    """Normalisiert Text fuer eine robuste regelbasierte Auswertung."""
    normalized = value.lower()
    replacements = {
        "ä": "ae",
        "ö": "oe",
        "ü": "ue",
        "ß": "ss",
        "é": "e",
        "è": "e",
        "ê": "e",
        "à": "a",
        "ç": "c",
    }

    for old, new in replacements.items():
        normalized = normalized.replace(old, new)

    return re.sub(r"\s+", " ", normalized).strip()


def tokenize(value: str) -> list[str]:
    """Zerlegt eine Anfrage in einfache Suchbegriffe."""
    return re.findall(r"[a-zA-Z0-9:]+", normalize_text(value))


def find_category(query: str) -> tuple[OsmCategory | None, set[str]]:
    """Ermittelt die wahrscheinlichste OSM-Kategorie."""
    normalized_query = normalize_text(query)

    for category in SUPPORTED_CATEGORIES.values():
        for alias in category.aliases:
            normalized_alias = normalize_text(alias)
            if normalized_alias in normalized_query:
                return category, set(tokenize(alias))

    return None, set()


def detect_optional_tags(query: str) -> tuple[dict[str, str], set[str], list[str], list[str]]:
    """Ermittelt optionale OSM-Tags und nicht eindeutig abbildbare Begriffe."""
    normalized_query = normalize_text(query)
    detected_tags: dict[str, str] = {}
    matched_terms: set[str] = set()
    warnings: list[str] = []
    unsupported_terms: list[str] = []

    for rule in OPTIONAL_TAG_RULES:
        for alias in rule.aliases:
            normalized_alias = normalize_text(alias)
            if normalized_alias in normalized_query:
                detected_tags[rule.key] = rule.value
                matched_terms.update(tokenize(alias))

    for term, warning in UNSUPPORTED_SEMANTIC_TERMS.items():
        normalized_term = normalize_text(term)
        if normalized_term in normalized_query:
            unsupported_terms.append(normalized_term)
            warnings.append(warning)

    return detected_tags, matched_terms, warnings, unsupported_terms


def get_allowed_tags() -> set[tuple[str, str]]:
    """Liefert alle vom regelbasierten Agenten erlaubten OSM-Tags."""
    allowed_tags: set[tuple[str, str]] = set()

    for category in SUPPORTED_CATEGORIES.values():
        allowed_tags.update(category.tags.items())

    for rule in OPTIONAL_TAG_RULES:
        allowed_tags.add((rule.key, rule.value))

    return allowed_tags


def extract_unmatched_terms(query: str, matched_terms: set[str]) -> list[str]:
    """Ermittelt relevante Begriffe, die nicht durch Regeln abgedeckt wurden."""
    unmatched: list[str] = []

    for token in tokenize(query):
        if token in STOPWORDS:
            continue

        if token.isdigit():
            continue

        if token in matched_terms:
            continue

        if token not in unmatched:
            unmatched.append(token)

    return unmatched
