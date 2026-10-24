from skills import extract_skills


def names(text):
    return {s["name"] for s in extract_skills(text)}


def test_java_is_not_found_inside_javascript():
    assert names("Experienced in JavaScript") == {"JavaScript"}


def test_matching_ignores_case():
    assert {"Spring Boot", "MySQL"} <= names("SPRING BOOT and mysql")


def test_alias_maps_to_the_canonical_name():
    assert "PostgreSQL" in names("Used Postgres for storage")


def test_multi_word_skill_split_across_lines():
    assert "Spring Boot" in names("Built with Spring\nBoot")


def test_mentions_are_counted():
    skills = extract_skills("Java, Java and more Java")
    java = next(s for s in skills if s["name"] == "Java")
    assert java["mentions"] == 3


def test_text_without_skills():
    assert names("I enjoy hiking and reading") == set()


def test_category_is_returned():
    assert extract_skills("Docker")[0]["category"] == "DevOps & Cloud"