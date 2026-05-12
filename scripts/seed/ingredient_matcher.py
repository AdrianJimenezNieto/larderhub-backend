"""
Fuzzy matching entre nombres de ingredientes de TheMealDB (en inglés)
y los productos del catálogo del backend (en español).

Flujo: ingrediente_en → translate_term() → nombre_es → fuzz.extractOne() → productId
"""

import re
from typing import Optional

from thefuzz import process as fuzz_process

from translations import translate_term


def _normalize(name: str) -> str:
    """Lower-case, sin paréntesis, sin números sueltos, strip."""
    name = name.lower().strip()
    name = re.sub(r"\(.*?\)", "", name)   # quitar "(2 units)" etc.
    name = re.sub(r"\b\d+\b", "", name)   # quitar números solos
    name = re.sub(r"\s+", " ", name).strip()
    return name


class IngredientMatcher:
    def __init__(self, product_index: dict[str, int], threshold: int = 70):
        """
        product_index: mapa {nombre_normalizado_es → productId}
        threshold: puntuación mínima de similitud (0-100).
        """
        self._threshold = threshold
        # Normalizar las claves del índice
        self._products: dict[str, int] = {
            _normalize(name): pid for name, pid in product_index.items()
        }
        self._product_names = list(self._products.keys())
        self._unmatched: list[str] = []

    def match(self, ingredient_en: str) -> Optional[int]:
        """
        Devuelve el productId del mejor match para el ingrediente en inglés,
        o None si no supera el umbral. Registra internamente los no-matcheados.
        """
        if not ingredient_en or not ingredient_en.strip():
            return None

        # Traducir al español antes del fuzzy
        es = translate_term(ingredient_en.strip())
        normalized = _normalize(es)

        if not normalized or not self._product_names:
            self._unmatched.append(ingredient_en)
            return None

        result = fuzz_process.extractOne(
            normalized,
            self._product_names,
            score_cutoff=self._threshold,
        )

        if result is None:
            self._unmatched.append(ingredient_en)
            return None

        matched_name, _score = result
        return self._products[matched_name]

    def unmatched(self) -> list[str]:
        """Términos que no alcanzaron el umbral de similitud."""
        return list(self._unmatched)

    def clear_unmatched(self) -> None:
        self._unmatched.clear()
