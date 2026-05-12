"""
Transforma un dict de TheMealDB al payload de CreateRecipeRequest del backend.

Retorna None si la receta no cumple las condiciones mínimas (título, ingredientes, steps).
"""

import re
from typing import Optional

from ingredient_matcher import IngredientMatcher
from translations import generate_title, translate_text

# Tiempos estimados por categoría TheMealDB (en minutos)
_COOKING_TIME_BY_CATEGORY: dict[str, int] = {
    "Pasta":        20,
    "Chicken":      35,
    "Beef":         60,
    "Lamb":         60,
    "Pork":         45,
    "Seafood":      25,
    "Vegetarian":   30,
    "Vegan":        30,
    "Breakfast":    20,
    "Dessert":      40,
    "Side":         20,
    "Starter":      20,
    "Miscellaneous":30,
}

_MIN_MATCHED_INGREDIENTS = 3
_MAX_STEPS = 6


def _parse_measure(measure_str: str) -> tuple[float, str]:
    """
    Convierte una medida de TheMealDB al vocabulario del backend.
    Ejemplos: "3/4 cup" → (0.75, "unidades"), "500g" → (500, "gramos"), "2 tbsp" → (2, "unidades").
    """
    if not measure_str:
        return 1.0, "unidades"

    s = measure_str.strip().lower()

    # Fracciones simples: "1/2", "3/4", "1/3"
    frac_match = re.match(r"^(\d+)/(\d+)", s)
    whole_frac = re.match(r"^(\d+)\s+(\d+)/(\d+)", s)

    if whole_frac:
        whole = float(whole_frac.group(1))
        num = float(whole_frac.group(2))
        den = float(whole_frac.group(3))
        qty = whole + num / den
    elif frac_match:
        qty = float(frac_match.group(1)) / float(frac_match.group(2))
    else:
        num_match = re.match(r"^([\d.,]+)", s)
        qty = float(num_match.group(1).replace(",", ".")) if num_match else 1.0

    # Determinar unidad
    if re.search(r"\b(g|gr|gram[s]?)\b", s):
        return qty, "gramos"
    if re.search(r"\b(kg|kilogram[s]?)\b", s):
        return qty * 1000, "gramos"
    if re.search(r"\b(ml|milliliter[s]?|millilitre[s]?)\b", s):
        return round(qty / 1000, 4), "litros"
    if re.search(r"\b(l|liter[s]?|litre[s]?)\b", s):
        return qty, "litros"
    if re.search(r"\b(oz|ounce[s]?)\b", s):
        return round(qty * 28.35, 1), "gramos"
    if re.search(r"\b(lb|pound[s]?)\b", s):
        return round(qty * 453.6, 1), "gramos"
    if re.search(r"\b(cup[s]?)\b", s):
        return qty, "unidades"
    if re.search(r"\b(tbsp|tablespoon[s]?)\b", s):
        return qty, "unidades"
    if re.search(r"\b(tsp|teaspoon[s]?)\b", s):
        return qty, "unidades"

    return qty, "unidades"


def _parse_ingredients(meal: dict, matcher: IngredientMatcher) -> list[dict]:
    """
    Extrae los pares strIngredientN/strMeasureN y los mapea a productIds.
    Devuelve solo los que matchean.
    """
    ingredients = []
    for i in range(1, 21):
        name = (meal.get(f"strIngredient{i}") or "").strip()
        if not name:
            break
        measure = (meal.get(f"strMeasure{i}") or "").strip()

        product_id = matcher.match(name)
        if product_id is None:
            continue

        quantity, unit = _parse_measure(measure)
        ingredients.append({
            "productId": product_id,
            "quantity": max(quantity, 0.01),
            "unit": unit,
        })

    return ingredients


def _split_instructions_into_steps(instructions: str) -> list[str]:
    """
    Divide el bloque de instrucciones en pasos individuales.
    Aplica la traducción por diccionario antes de dividir.
    """
    translated = translate_text(instructions)

    # Intentar split por doble salto de línea (párrafos)
    paragraphs = re.split(r"\r?\n\r?\n+", translated)
    steps = [p.strip() for p in paragraphs if len(p.strip()) >= 20]

    # Fallback: si no hay párrafos, dividir por frases largas
    if len(steps) < 2:
        sentences = re.split(r"(?<=[.!?])\s+", translated)
        # Agrupar en bloques de ~2 frases
        grouped = []
        buffer = []
        for sent in sentences:
            sent = sent.strip()
            if not sent:
                continue
            buffer.append(sent)
            if len(buffer) >= 2:
                grouped.append(" ".join(buffer))
                buffer = []
        if buffer:
            grouped.append(" ".join(buffer))
        steps = [g for g in grouped if len(g) >= 20]

    # Cap a _MAX_STEPS; concatenar el excedente en el último
    if len(steps) > _MAX_STEPS:
        overflow = " ".join(steps[_MAX_STEPS - 1:])
        steps = steps[: _MAX_STEPS - 1] + [overflow]

    return steps


def to_create_recipe_request(
    meal: dict,
    matcher: IngredientMatcher,
) -> Optional[dict]:
    """
    Convierte un dict de TheMealDB al payload de CreateRecipeRequest.
    Devuelve None si la receta no cumple los requisitos mínimos.
    """
    # 1. Título en español
    title = generate_title(meal)
    if not title:
        return None

    # 2. Ingredientes matcheados
    ingredients = _parse_ingredients(meal, matcher)
    if len(ingredients) < _MIN_MATCHED_INGREDIENTS:
        return None

    # 3. Steps
    raw_instructions = (meal.get("strInstructions") or "").strip()
    steps_texts = _split_instructions_into_steps(raw_instructions) if raw_instructions else []

    # Mínimo 1 step genérico si no hay instrucciones
    if not steps_texts:
        ingredient_names = [
            f"ingrediente {i+1}" for i in range(len(ingredients))
        ]
        steps_texts = [
            f"Prepara los ingredientes: {', '.join(ingredient_names[:5])}.",
            "Cocina según las instrucciones de la receta original. Sirve caliente.",
        ]

    steps = [
        {
            "stepNumber": idx + 1,
            "description": text,
            "timerMinutes": None,
        }
        for idx, text in enumerate(steps_texts)
    ]

    # 4. Heurísticas para campos derivados
    n_ing = len(ingredients)
    if n_ing <= 5:
        difficulty = "Fácil"
    elif n_ing <= 10:
        difficulty = "Media"
    else:
        difficulty = "Difícil"

    category = meal.get("strCategory", "")
    cooking_time = _COOKING_TIME_BY_CATEGORY.get(category, 30)

    # Descripción: primera frase de las instrucciones traducidas
    description = None
    if raw_instructions:
        translated_intro = translate_text(raw_instructions)
        first_sentence = re.split(r"(?<=[.!?])\s", translated_intro)[0]
        description = first_sentence[:200].strip() or None

    return {
        "title": title,
        "description": description,
        "imageUrl": meal.get("strMealThumb") or None,
        "cookingTimeMinutes": cooking_time,
        "difficulty": difficulty,
        "servings": 4,
        "ingredients": ingredients,
        "steps": steps,
    }
