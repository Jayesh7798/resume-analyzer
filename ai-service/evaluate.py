import json
from pathlib import Path

from sklearn.metrics import roc_auc_score

from matcher import compute_match
from semantic import embed_resume
from skills import extract_skills

THRESHOLD = 50  # hybrid score at or above this counts as "good match"; tune it and re-run
DATA = Path(__file__).parent / "eval" / "dataset.json"

METHODS = {
    "Keyword only (TF-IDF)": "keyword_score",
    "Skills only": "skill_score",
    "Semantic only": "semantic_score",
    "Hybrid (0.5 / 0.3 / 0.2)": "overall_score",
}


def skill_names(text):
    return [s["name"] for s in extract_skills(text)]


def build_rows(data):
    rows = []
    for rid, resume_text in data["resumes"].items():
        resume_skills = skill_names(resume_text)
        resume_embeddings = embed_resume(resume_text)
        for jid, job in data["jobs"].items():
            result = compute_match(
                resume_text,
                job["text"],
                resume_skills,
                skill_names(job["text"]),
                job.get("min_experience", 0),
                resume_embeddings,
            )
            rows.append({"resume": rid, "job": jid, "label": data["labels"][rid][jid], **result})
    return rows


def top1_accuracy(rows, key):
    """For each resume: is its best-scoring job a relevant one? A tie that includes
    an irrelevant job counts as wrong."""
    resumes = sorted({r["resume"] for r in rows})
    correct = 0
    for rid in resumes:
        mine = [r for r in rows if r["resume"] == rid]
        best = max(r[key] for r in mine)
        top = [r for r in mine if r[key] == best]
        if all(r["label"] == 1 for r in top):
            correct += 1
    return correct, len(resumes)


def main():
    data = json.loads(DATA.read_text(encoding="utf-8"))
    rows = build_rows(data)
    labels = [r["label"] for r in rows]
    print(f"{len(rows)} resume/job pairs, {sum(labels)} relevant\n")

    print(f"{'Method':28}{'AUC':>6}{'Top-1':>9}")
    for name, key in METHODS.items():
        auc = roc_auc_score(labels, [r[key] for r in rows])
        hits, total = top1_accuracy(rows, key)
        print(f"{name:28}{auc:6.2f}{hits:>6}/{total}")

    tp = fp = fn = tn = 0
    for r in rows:
        predicted = r["overall_score"] >= THRESHOLD
        if predicted and r["label"]:
            tp += 1
        elif predicted and not r["label"]:
            fp += 1
        elif not predicted and r["label"]:
            fn += 1
        else:
            tn += 1
    precision = tp / (tp + fp) if tp + fp else 0.0
    recall = tp / (tp + fn) if tp + fn else 0.0
    f1 = 2 * precision * recall / (precision + recall) if precision + recall else 0.0

    print(f"\nHybrid at threshold {THRESHOLD}:")
    print(f"  true positives {tp}, false positives {fp}, false negatives {fn}, true negatives {tn}")
    print(f"  precision {precision:.2f}, recall {recall:.2f}, F1 {f1:.2f}")

    print("\nPer-pair scores:")
    print(f"{'resume':7}{'job':5}{'label':6}{'keyword':>9}{'skills':>8}{'semantic':>10}{'fit':>6}{'overall':>9}")
    for r in rows:
        predicted = r["overall_score"] >= THRESHOLD
        flag = ""
        if predicted and not r["label"]:
            flag = "  <- FALSE POSITIVE"
        elif not predicted and r["label"]:
            flag = "  <- FALSE NEGATIVE"
        print(f"{r['resume']:7}{r['job']:5}{r['label']:<6}{r['keyword_score']:9.1f}"
              f"{r['skill_score']:8.1f}{r['semantic_score']:10.1f}{r['fit_score']:6.1f}"
              f"{r['overall_score']:9.1f}{flag}")


if __name__ == "__main__":
    main()