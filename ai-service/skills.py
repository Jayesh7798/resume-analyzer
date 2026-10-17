import json
from pathlib import Path

import spacy
from spacy.matcher import PhraseMatcher

nlp = spacy.blank("en")

_taxonomy = json.loads(
    (Path(__file__).parent / "skills.json").read_text(encoding="utf-8")
)
_by_name = {entry["name"]: entry for entry in _taxonomy}

_matcher = PhraseMatcher(nlp.vocab, attr="LOWER")
for entry in _taxonomy:
    terms = [entry["name"], *entry["aliases"]]
    _matcher.add(entry["name"], [nlp.make_doc(term) for term in terms])


def extract_skills(text: str) -> list[dict]:
    # collapse newlines and extra spaces so "Spring\nBoot" still matches "Spring Boot"
    clean = " ".join(text.split())
    doc = nlp.make_doc(clean)

    counts: dict[str, int] = {}
    for match_id, _start, _end in _matcher(doc):
        name = nlp.vocab.strings[match_id]
        counts[name] = counts.get(name, 0) + 1

    return [
        {"name": name, "category": _by_name[name]["category"], "mentions": count}
        for name, count in sorted(counts.items(), key=lambda item: (-item[1], item[0]))
    ]