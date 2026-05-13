"""
Convierte el campo `quantity` de Open Food Facts al vocabulario de unidades
que usa el backend: 'litros', 'gramos', 'kilogramos', 'unidades'.
"""

import re
from typing import Tuple, Optional

_QUANTITY_RE = re.compile(
    r"^\s*([\d.,]+)\s*"
    r"(kg|g|gr|gramo[s]?|kilogramo[s]?|l|litro[s]?|ml|cl|dl|"
    r"ud[s]?|unidad(?:es)?|pack[s]?|pieza[s]?|lata[s]?|bote[s]?|"
    r"oz|lb)?\s*",
    re.IGNORECASE,
)

_VOLUME_UNITS = {"l", "litro", "litros", "ml", "cl", "dl"}
_WEIGHT_UNITS = {"g", "gr", "gramo", "gramos"}
_KG_UNITS = {"kg", "kilogramo", "kilogramos"}


def parse(raw: Optional[str]) -> Tuple[Optional[float], str]:
    """
    Devuelve (cantidad_numérica, unidad_estándar).
    Si no se puede parsear: (None, 'unidades').
    """
    if not raw:
        return None, "unidades"

    m = _QUANTITY_RE.match(raw.strip())
    if not m:
        return None, "unidades"

    value_str, unit_str = m.group(1), (m.group(2) or "").lower()
    value_str = value_str.replace(",", ".")

    try:
        value = float(value_str)
    except ValueError:
        return None, "unidades"

    if not unit_str:
        return value, "unidades"

    if unit_str in _VOLUME_UNITS:
        # Normalizar a litros
        if unit_str == "ml":
            return round(value / 1000, 4), "litros"
        if unit_str == "cl":
            return round(value / 100, 4), "litros"
        if unit_str == "dl":
            return round(value / 10, 4), "litros"
        return value, "litros"

    if unit_str in _KG_UNITS:
        return value, "kilogramos"

    if unit_str in _WEIGHT_UNITS:
        # Pasar a kilogramos si supera 2000 g (p. ej. "2000g" → 2 kg)
        if value >= 2000:
            return round(value / 1000, 3), "kilogramos"
        return value, "gramos"

    return value, "unidades"


def standard_unit(raw: Optional[str]) -> str:
    """Solo devuelve la unidad estándar, sin la cantidad."""
    _, unit = parse(raw)
    return unit
