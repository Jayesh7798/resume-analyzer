from matcher import (
    FIT_WEIGHT,
    SEMANTIC_WEIGHT,
    SKILL_WEIGHT,
    compute_match,
    keyword_similarity,
    skill_match,
)


def test_weights_sum_to_one():
    assert abs(SKILL_WEIGHT + SEMANTIC_WEIGHT + FIT_WEIGHT - 1.0) < 1e-9


def test_skill_match_partial():
    score, matched, missing = skill_match(["Java", "MySQL"], ["Java", "Docker", "MySQL", "Git"])
    assert score == 50.0
    assert matched == ["Java", "MySQL"]
    assert missing == ["Docker", "Git"]


def test_skill_match_ignores_case():
    score, matched, _ = skill_match(["java"], ["Java"])
    assert score == 100.0 and matched == ["Java"]


def test_skill_match_when_the_job_has_no_skills():
    assert skill_match(["Java"], []) == (0.0, [], [])


def test_missing_skills_keep_the_job_order():
    _, _, missing = skill_match([], ["Docker", "Git", "Java"])
    assert missing == ["Docker", "Git", "Java"]


def test_keyword_similarity_identical_and_disjoint():
    assert keyword_similarity("alpha beta gamma", "alpha beta gamma") > 99
    assert keyword_similarity("alpha beta", "gamma delta") == 0.0


def test_overall_score_is_the_weighted_sum():
    result = compute_match(
        "Java developer. B.Tech in Computer Science. 3 years of experience with Spring Boot.",
        "Java developer with Spring Boot. Bachelor's degree required, 2 years of experience.",
        ["Java", "Spring Boot"],
        ["Java", "Spring Boot"],
        2,
    )
    expected = (
        SKILL_WEIGHT * result["skill_score"]
        + SEMANTIC_WEIGHT * result["semantic_score"]
        + FIT_WEIGHT * result["fit_score"]
    )
    assert abs(result["overall_score"] - expected) < 0.2  # scores are rounded to 1 decimal
    assert result["skill_score"] == 100
    assert result["fit_score"] == 100


def test_missing_requirements_lower_the_fit_and_are_explained():
    result = compute_match(
        "Java developer with Spring Boot projects.",
        "Java developer with Spring Boot. Bachelor's degree required.",
        ["Java", "Spring Boot"],
        ["Java", "Spring Boot"],
        2,
    )
    assert result["fit_score"] == 0
    assert "years of experience" in result["explanation"]
    assert "bachelor's" in result["explanation"]