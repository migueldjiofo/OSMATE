from __future__ import annotations

from dataclasses import dataclass


@dataclass(frozen=True)
class CategoryRule:
    category: str
    tags: dict[str, str]
    keywords: tuple[str, ...]
    summary_de: str

    @property
    def key(self) -> str:
        return self.category


@dataclass(frozen=True)
class OptionalTagRule:
    tags: dict[str, str]
    keywords: tuple[str, ...]
    summary_de: str


CATEGORY_RULES: tuple[CategoryRule, ...] = (
    CategoryRule(
        category="cafe",
        tags={"amenity": "cafe"},
        keywords=("cafe", "cafes", "cafÃƒÆ’Ã‚Â©", "cafÃƒÆ’Ã‚Â©s", "kaffee", "coffee"),
        summary_de="Es werden CafÃƒÆ’Ã‚Â©s gesucht.",
    ),
    CategoryRule(
        category="spaeti",
        tags={"shop": "convenience"},
        keywords=("spaeti", "spÃƒÆ’Ã‚Â¤ti", "spaetis", "spÃƒÆ’Ã‚Â¤tis", "kiosk", "convenience"),
        summary_de="Es werden SpÃƒÆ’Ã‚Â¤tis beziehungsweise Kioske gesucht.",
    ),
    CategoryRule(
        category="restaurant",
        tags={"amenity": "restaurant"},
        keywords=("restaurant", "restaurants", "essen", "food", "dinner", "lunch"),
        summary_de="Es werden Restaurants gesucht.",
    ),
    CategoryRule(
        category="supermarket",
        tags={"shop": "supermarket"},
        keywords=("supermarkt", "supermaerkte", "supermÃƒÆ’Ã‚Â¤rkte", "supermarket", "grocery", "lebensmittel"),
        summary_de="Es werden SupermÃƒÆ’Ã‚Â¤rkte gesucht.",
    ),
    CategoryRule(
        category="playground",
        tags={"leisure": "playground"},
        keywords=("spielplatz", "spielplÃƒÆ’Ã‚Â¤tze", "spielplaetze", "playground", "kinder"),
        summary_de="Es werden SpielplÃƒÆ’Ã‚Â¤tze gesucht.",
    ),
    CategoryRule(
        category="park",
        tags={"leisure": "park"},
        keywords=("park", "parks", "grÃƒÆ’Ã‚Â¼nflÃƒÆ’Ã‚Â¤che", "gruenflaeche", "green space"),
        summary_de="Es werden Parks gesucht.",
    ),
    CategoryRule(
        category="pharmacy",
        tags={"amenity": "pharmacy"},
        keywords=("apotheke", "apotheken", "pharmacy"),
        summary_de="Es werden Apotheken gesucht.",
    ),
    CategoryRule(
        category="atm",
        tags={"amenity": "atm"},
        keywords=("geldautomat", "geldautomaten", "atm", "cash"),
        summary_de="Es werden Geldautomaten gesucht.",
    ),
    CategoryRule(
        category="bakery",
        tags={"shop": "bakery"},
        keywords=("baeckerei", "bÃƒÆ’Ã‚Â¤ckerei", "baeckereien", "bÃƒÆ’Ã‚Â¤ckereien", "bakery", "bread", "brot"),
        summary_de="Es werden BÃƒÆ’Ã‚Â¤ckereien gesucht.",
    ),
    CategoryRule(
        category="library",
        tags={"amenity": "library"},
        keywords=("bibliothek", "bibliotheken", "library", "bÃƒÆ’Ã‚Â¼cherei", "buecherei"),
        summary_de="Es werden Bibliotheken gesucht.",
    ),
    CategoryRule(
        category="hospital",
        tags={"amenity": "hospital"},
        keywords=("krankenhaus", "krankenhÃƒÆ’Ã‚Â¤user", "krankenhaeuser", "hospital", "clinic", "klinik"),
        summary_de="Es werden KrankenhÃƒÆ’Ã‚Â¤user gesucht.",
    ),
    CategoryRule(
        category="school",
        tags={"amenity": "school"},
        keywords=("schule", "schulen", "school"),
        summary_de="Es werden Schulen gesucht.",
    ),
    CategoryRule(
        category="charging_station",
        tags={"amenity": "charging_station"},
        keywords=("ladestation", "ladestationen", "ladesaeule", "ladesÃƒÆ’Ã‚Â¤ule", "charging station", "ev charging", "elektroauto"),
        summary_de="Es werden Ladestationen gesucht.",
    ),
    CategoryRule(
        category="bus_stop",
        tags={"highway": "bus_stop"},
        keywords=("bushaltestelle", "bushaltestellen", "bus stop", "haltestelle", "busstation"),
        summary_de="Es werden Bushaltestellen gesucht.",
    ),
)


OPTIONAL_TAG_RULES: tuple[OptionalTagRule, ...] = (
    OptionalTagRule(
        tags={"outdoor_seating": "yes"},
        keywords=("terrasse", "auÃƒÆ’Ã…Â¸enbereich", "aussenbereich", "outdoor seating", "sitzplÃƒÆ’Ã‚Â¤tze drauÃƒÆ’Ã…Â¸en", "sitzplaetze draussen"),
        summary_de="mit AuÃƒÆ’Ã…Â¸enbereich",
    ),
    OptionalTagRule(
        tags={"internet_access": "wlan"},
        keywords=("wlan", "wifi", "wi-fi", "internet"),
        summary_de="mit Internetzugang",
    ),
    OptionalTagRule(
        tags={"wheelchair": "yes"},
        keywords=("barrierefrei", "rollstuhl", "wheelchair"),
        summary_de="barrierefrei",
    ),
    OptionalTagRule(
        tags={"diet:vegan": "yes"},
        keywords=("vegan", "vegane"),
        summary_de="mit veganem Angebot",
    ),
    OptionalTagRule(
        tags={"takeaway": "yes"},
        keywords=("takeaway", "mitnehmen", "zum mitnehmen"),
        summary_de="mit Take-away-Angebot",
    ),
    OptionalTagRule(
        tags={"smoking": "outside"},
        keywords=("rauchen", "smoking", "smoker"),
        summary_de="mit ausgewiesenem Raucherbereich",
    ),
    OptionalTagRule(
        tags={"playground:water": "yes"},
        keywords=("wasserspiel", "wasserspiele", "water play", "wasserspielplatz"),
        summary_de="mit Wasserspielen",
    ),
)


UNSUPPORTED_SEMANTIC_TERMS: tuple[str, ...] = (
    "ruhig",
    "ruhige",
    "guenstig",
    "gÃƒÆ’Ã‚Â¼nstig",
    "billig",
    "cheap",
    "preiswert",
    "schÃƒÆ’Ã‚Â¶n",
    "schoen",
    "beliebt",
    "popular",
)


def normalize_text(text: str) -> str:
    normalized = text.lower().strip()

    replacements = {
        "ÃƒÆ’Ã‚Â¤": "ae",
        "ÃƒÆ’Ã‚Â¶": "oe",
        "ÃƒÆ’Ã‚Â¼": "ue",
        "ÃƒÆ’Ã…Â¸": "ss",
        "ÃƒÆ’Ã‚Â©": "e",
        "ÃƒÆ’Ã‚Â¨": "e",
        "ÃƒÆ’Ã‚Âª": "e",
        "ÃƒÆ’Ã‚Â¡": "a",
        "ÃƒÆ’Ã‚Â ": "a",
        "ÃƒÆ’Ã‚Â¢": "a",
    }

    for source, target in replacements.items():
        normalized = normalized.replace(source, target)

    return normalized


def normalize_query(query: str) -> str:
    return normalize_text(query)


def find_category(query: str) -> tuple[CategoryRule | None, tuple[str, ...]]:
    normalized_query = normalize_text(query)

    for rule in CATEGORY_RULES:
        matched_terms = find_matching_keywords(
            normalized_query,
            rule.keywords,
        )

        if matched_terms:
            return rule, matched_terms

    return None, ()


def detect_category(query: str) -> CategoryRule | None:
    category, _ = find_category(query)
    return category


def detect_primary_category(query: str) -> CategoryRule | None:
    return detect_category(query)


def detect_category_rule(query: str) -> CategoryRule | None:
    return detect_category(query)


def find_category_rule(query: str) -> CategoryRule | None:
    return detect_category(query)


def find_matching_category_rule(query: str) -> CategoryRule | None:
    return detect_category(query)


def detect_optional_tags(query: str) -> tuple[dict[str, str], tuple[str, ...], list[str], tuple[str, ...]]:
    normalized_query = normalize_text(query)
    tags: dict[str, str] = {}
    matched_terms: list[str] = []
    warnings: list[str] = []
    unsupported_terms: list[str] = []

    for rule in OPTIONAL_TAG_RULES:
        rule_matches = find_matching_keywords(
            normalized_query,
            rule.keywords,
        )

        if rule_matches:
            tags.update(rule.tags)
            matched_terms.extend(rule_matches)

    for term in UNSUPPORTED_SEMANTIC_TERMS:
        normalized_term = normalize_text(term)

        if normalized_term in normalized_query:
            unsupported_terms.append(term)
            warnings.append(
                f"Der Begriff '{term}' kann regelbasiert nur eingeschrÃ¤nkt bewertet werden."
            )

    return tags, tuple(matched_terms), warnings, tuple(unsupported_terms)

def detect_unsupported_terms(query: str) -> tuple[str, ...]:
    normalized_query = normalize_text(query)
    matched_terms: list[str] = []

    for term in UNSUPPORTED_SEMANTIC_TERMS:
        normalized_term = normalize_text(term)

        if normalized_term in normalized_query:
            matched_terms.append(term)

    return tuple(matched_terms)


def extract_unmatched_terms(
    query: str,
    matched_terms: tuple[str, ...] = (),
) -> list[str]:
    normalized_query = normalize_text(query)

    ignored_terms = {
        "finde",
        "find",
        "suche",
        "such",
        "in",
        "der",
        "die",
        "das",
        "den",
        "dem",
        "mit",
        "ohne",
        "und",
        "oder",
        "near",
        "nearby",
        "nahe",
        "nähe",
        "naehe",
        "umkreis",
        "von",
        "im",
        "am",
        "an",
        "zu",
        "zur",
        "zum",
        "einen",
        "eine",
        "ein",
        "meiner",
        "meine",
        "mein",
        "bitte",
    }

    normalized_matched_terms = {
        normalize_text(term)
        for term in matched_terms
    }

    unmatched_terms: list[str] = []

    for term in normalized_query.replace(",", " ").replace(".", " ").split():
        cleaned_term = term.strip()

        if not cleaned_term:
            continue

        if cleaned_term in ignored_terms:
            continue

        if any(cleaned_term in matched_term or matched_term in cleaned_term for matched_term in normalized_matched_terms):
            continue

        if cleaned_term not in unmatched_terms:
            unmatched_terms.append(cleaned_term)

    return unmatched_terms

def contains_any_keyword(
    query: str,
    keywords: tuple[str, ...],
) -> bool:
    return bool(
        find_matching_keywords(
            normalize_text(query),
            keywords,
        )
    )


def find_matching_keywords(
    normalized_query: str,
    keywords: tuple[str, ...],
) -> tuple[str, ...]:
    matched_terms: list[str] = []

    for keyword in keywords:
        normalized_keyword = normalize_text(keyword)

        if normalized_keyword in normalized_query:
            matched_terms.append(keyword)

    return tuple(matched_terms)


def merge_tags(
    category_rule: CategoryRule,
    optional_tags: dict[str, str] | tuple[OptionalTagRule, ...],
) -> dict[str, str]:
    tags = dict(category_rule.tags)

    if isinstance(optional_tags, dict):
        tags.update(optional_tags)
        return tags

    for rule in optional_tags:
        tags.update(rule.tags)

    return tags


def build_summary(
    category_rule: CategoryRule,
    optional_tags: dict[str, str] | tuple[OptionalTagRule, ...],
) -> str:
    parts = [category_rule.summary_de]

    if isinstance(optional_tags, dict):
        return " ".join(parts)

    parts.extend(rule.summary_de for rule in optional_tags)
    return " ".join(parts)


def get_allowed_tags() -> set[tuple[str, str]]:
    allowed_tags: set[tuple[str, str]] = set()

    for rule in CATEGORY_RULES:
        for key, value in rule.tags.items():
            allowed_tags.add((key, value))

    for rule in OPTIONAL_TAG_RULES:
        for key, value in rule.tags.items():
            allowed_tags.add((key, value))

    return allowed_tags
