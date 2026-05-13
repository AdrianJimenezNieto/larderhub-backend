"""
Mapeo entre tags de Open Food Facts y las categorías en español del catálogo.

Las categorías españolas son las que usa el modelo Product del backend.
Se prueban en orden; la primera coincidencia gana.
"""

from typing import Optional

# (off_tag_substring, categoría_española)
# Se compara contra el contenido de `categories_tags` del producto OFF.
_RULES = [
    ("olive-oils",          "Aceites"),
    ("vegetable-oils",      "Aceites"),
    ("vegetable-fats",      "Aceites"),
    ("legumes",             "Legumbres"),
    ("pulses",              "Legumbres"),
    ("meats",               "Carnes"),
    ("poultry",             "Carnes"),
    ("seafoods",            "Pescados"),
    ("fishes",              "Pescados"),
    ("dairies",             "Lácteos"),
    ("cheeses",             "Lácteos"),
    ("yogurts",             "Lácteos"),
    ("milks",               "Lácteos"),
    ("fruits",              "Frutas"),
    ("vegetables",          "Verduras"),
    ("cereals-and-potatoes","Cereales"),
    ("cereals",             "Cereales"),
    ("breads",              "Cereales"),
    ("beverages",           "Bebidas"),
    ("waters",              "Bebidas"),
    ("juices",              "Bebidas"),
    # fallback amplio — cualquier producto alimentario
    ("groceries",           "Despensa"),
    ("sauces",              "Despensa"),
    ("canned-foods",        "Despensa"),
    ("condiments",          "Despensa"),
    ("pastas",              "Despensa"),
    ("rice",                "Despensa"),
]

# Categorías que el script buscará activamente en OFF
# (off_tag para la query, categoría_española que se asignará)
SEARCH_TARGETS = [
    ("dairies",        "Lácteos"),
    ("fruits",         "Frutas"),
    ("vegetables",     "Verduras"),
    ("meats",          "Carnes"),
    ("seafoods",       "Pescados"),
    ("legumes",        "Legumbres"),
    ("cereals",        "Cereales"),
    ("olive-oils",     "Aceites"),
    ("beverages",      "Bebidas"),
    ("groceries",      "Despensa"),
]


def resolve_category(categories_tags: list[str]) -> Optional[str]:
    """
    Devuelve la categoría española para los tags de un producto OFF,
    o None si no hay coincidencia conocida.
    """
    normalized = [t.lower() for t in categories_tags]
    for tag_fragment, spanish_category in _RULES:
        if any(tag_fragment in t for t in normalized):
            return spanish_category
    return None
