from semantic import embed_resume, semantic_similarity


def test_paraphrase_scores_higher_than_an_unrelated_job():
    resume = embed_resume(
        "I developed server-side applications and web services using Spring, "
        "handling database access and user authentication."
    )
    related = semantic_similarity(
        resume, "Looking for a backend engineer to build and maintain REST APIs and data access layers."
    )
    unrelated = semantic_similarity(
        resume, "Looking for a graphic designer skilled in Photoshop, branding and illustration."
    )
    assert related > unrelated


def test_empty_resume_scores_zero():
    assert semantic_similarity(embed_resume(""), "any job text") == 0.0


def test_empty_job_text_scores_zero():
    assert semantic_similarity(embed_resume("Java developer"), "   ") == 0.0


def test_score_stays_between_0_and_100():
    score = semantic_similarity(embed_resume("Java developer"), "Java developer needed")
    assert 0 <= score <= 100