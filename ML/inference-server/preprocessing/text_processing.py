"""
preprocessing/text_processing.py
──────────────────────────────────
Shared text cleaning and normalisation utilities.
Used by the priority model (and future BERT-based models).
"""

import re
import unicodedata
from typing import Optional


# ──────────────────────────────────────────────
# Stop-word list (lightweight — no NLTK dependency)
# ──────────────────────────────────────────────

_STOP_WORDS = frozenset([
    "a", "an", "the", "and", "or", "but", "in", "on", "at", "to",
    "for", "of", "with", "by", "from", "is", "it", "be", "as", "was",
    "are", "were", "been", "has", "have", "had", "do", "does", "did",
    "will", "would", "could", "should", "may", "might", "must", "shall",
    "not", "no", "nor", "so", "yet", "both", "either", "neither",
    "this", "that", "these", "those", "i", "we", "you", "he", "she",
    "they", "what", "which", "who", "whom", "when", "where", "why",
    "how", "all", "each", "every", "any", "more", "most", "other",
    "into", "through", "during", "before", "after", "above", "below",
    "then", "once", "here", "there", "than", "too", "very", "just",
    "about", "up", "out", "if", "can", "its", "also",
])


# ──────────────────────────────────────────────
# Core cleaning function
# ──────────────────────────────────────────────

def clean_text(
    text: Optional[str],
    *,
    lowercase: bool = True,
    remove_numbers: bool = False,
    remove_stopwords: bool = False,
    max_length: Optional[int] = None,
) -> str:
    """
    Clean and normalise a text string for ML input.

    Parameters
    ----------
    text             : Raw input string (may be None / empty)
    lowercase        : Convert to lowercase (default True)
    remove_numbers   : Strip numeric tokens (default False — numbers carry signal)
    remove_stopwords : Remove common English stop words (default False)
    max_length       : Truncate to this many characters after cleaning

    Returns
    -------
    Cleaned string ready for TF-IDF or tokeniser.
    """
    if not text:
        return ""

    # Normalise unicode (è → e, etc.)
    text = unicodedata.normalize("NFKD", text).encode("ascii", "ignore").decode("ascii")

    if lowercase:
        text = text.lower()

    # Remove URLs
    text = re.sub(r"https?://\S+|www\.\S+", " ", text)

    # Remove email addresses
    text = re.sub(r"\S+@\S+\.\S+", " ", text)

    # Replace non-alphanumeric (except space) with space
    text = re.sub(r"[^a-zA-Z0-9\s]", " ", text)

    if remove_numbers:
        text = re.sub(r"\b\d+\b", " ", text)

    # Collapse whitespace
    text = re.sub(r"\s+", " ", text).strip()

    if remove_stopwords:
        tokens = text.split()
        tokens = [t for t in tokens if t not in _STOP_WORDS]
        text = " ".join(tokens)

    if max_length and len(text) > max_length:
        text = text[:max_length]

    return text


# ──────────────────────────────────────────────
# Title and description specific processors
# ──────────────────────────────────────────────

def process_title(title: Optional[str]) -> str:
    """
    Clean a task title.
    Keeps numbers (e.g. "error 500") and lowercase.
    """
    return clean_text(title, lowercase=True, remove_numbers=False, max_length=200)


def process_description(description: Optional[str]) -> str:
    """
    Clean a task description.
    More aggressive: removes stop words to surface meaningful terms.
    """
    return clean_text(
        description,
        lowercase=True,
        remove_numbers=False,
        remove_stopwords=True,
        max_length=1000,
    )


# ──────────────────────────────────────────────
# Tag processor
# ──────────────────────────────────────────────

def process_tags(tags: list) -> str:
    """
    Join a list of tag strings into a single cleaned string.
    Used when tags are concatenated into text features.
    """
    if not tags:
        return ""
    joined = " ".join(str(t) for t in tags)
    return clean_text(joined, lowercase=True, remove_numbers=False)
