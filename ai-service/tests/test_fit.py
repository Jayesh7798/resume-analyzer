from fit import (
    compute_fit,
    degree_levels,
    education_fit,
    experience_fit,
    years_of_experience,
)


def test_years_found_before_the_word_experience():
    assert years_of_experience("3 years of experience with Java") == 3


def test_years_found_after_the_word_experience():
    assert years_of_experience("Experience: 2 years in backend work") == 2


def test_no_years_mentioned():
    assert years_of_experience("Java developer with Spring Boot projects") == 0


def test_absurd_values_are_ignored():
    assert years_of_experience("100 years of experience") == 0


def test_experience_fit_without_requirement_is_full():
    score, note = experience_fit("anything", 0)
    assert score == 100 and note is None


def test_experience_fit_partial():
    score, note = experience_fit("1 year of experience in Java", 2)
    assert score == 50
    assert note is not None


def test_experience_fit_none_found():
    score, note = experience_fit("Java developer", 2)
    assert score == 0
    assert "none" in note


def test_btech_is_detected_as_bachelor():
    assert 2 in degree_levels("B.Tech in Computer Science")


def test_masters_degree_is_detected():
    assert 3 in degree_levels("Master's degree in Computer Science")


def test_the_word_be_is_not_a_degree():
    assert degree_levels("This will be great") == set()


def test_scrum_master_is_not_a_degree():
    assert degree_levels("Certified Scrum Master") == set()


def test_education_fit_met():
    score, note = education_fit("B.Tech in CS", "Bachelor's degree required")
    assert score == 100 and note is None


def test_education_fit_missing():
    score, note = education_fit("Java developer", "Bachelor's degree required")
    assert score == 0
    assert "bachelor's" in note


def test_education_fit_without_requirement_is_full():
    score, note = education_fit("Java developer", "Java developer wanted")
    assert score == 100 and note is None


def test_fit_is_the_average_of_experience_and_education():
    fit, notes = compute_fit(
        "Java developer with 3 years of experience",
        "Bachelor's degree required",
        2,
    )
    assert fit == 50  # experience 100, education 0
    assert len(notes) == 1