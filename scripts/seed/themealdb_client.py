"""
Cliente para la API pública de TheMealDB (https://www.themealdb.com).
Sin autenticación requerida (tier gratuito).
"""

import time
import requests
from typing import Iterator, Optional

_BASE_URL = "https://www.themealdb.com/api/json/v1/1"
_USER_AGENT = "LarderHub-Seed/1.0 (TFG DAW)"
_RETRY_WAIT = 3


def _session() -> requests.Session:
    s = requests.Session()
    s.headers.update({"User-Agent": _USER_AGENT})
    return s


_shared_session: Optional[requests.Session] = None


def _get_session() -> requests.Session:
    global _shared_session
    if _shared_session is None:
        _shared_session = _session()
    return _shared_session


def _get_json(url: str, params: Optional[dict] = None) -> Optional[dict]:
    session = _get_session()
    for attempt in range(3):
        try:
            resp = session.get(url, params=params, timeout=15)
            if resp.status_code == 429:
                time.sleep(_RETRY_WAIT * (attempt + 1))
                continue
            resp.raise_for_status()
            return resp.json()
        except requests.RequestException:
            if attempt == 2:
                return None
            time.sleep(1)
    return None


def fetch_meal_ids_by_area(area: str) -> list[str]:
    """
    Devuelve los idMeal de todas las recetas de una área geográfica.
    Ejemplo: area='Spanish', 'Italian', 'Mexican', 'Mediterranean'.
    """
    data = _get_json(f"{_BASE_URL}/filter.php", params={"a": area})
    if not data or not data.get("meals"):
        return []
    return [m["idMeal"] for m in data["meals"] if m.get("idMeal")]


def lookup_meal(meal_id: str) -> Optional[dict]:
    """
    Devuelve el dict completo de una receta por su idMeal.
    Incluye strIngredient1..20, strMeasure1..20, strInstructions, strMealThumb, etc.
    Devuelve None si no se encuentra o hay error de red.
    """
    data = _get_json(f"{_BASE_URL}/lookup.php", params={"i": meal_id})
    if not data or not data.get("meals"):
        return None
    return data["meals"][0]


def iter_meals_by_areas(areas: list[str], limit: int) -> Iterator[dict]:
    """
    Genera hasta `limit` dicts de recetas completos distribuidos entre las áreas.
    Deduplica por idMeal. Respeta un delay entre peticiones.
    """
    # Recoger todos los IDs por área
    area_ids: dict[str, list[str]] = {}
    for area in areas:
        ids = fetch_meal_ids_by_area(area)
        if ids:
            area_ids[area] = ids
        time.sleep(0.3)

    if not area_ids:
        return

    # Distribuir el límite proporcionalmente
    total_available = sum(len(ids) for ids in area_ids.values())
    quota: dict[str, int] = {}
    remaining = limit
    areas_with_ids = list(area_ids.keys())

    for i, area in enumerate(areas_with_ids):
        if i == len(areas_with_ids) - 1:
            quota[area] = remaining
        else:
            n = max(1, round(len(area_ids[area]) / total_available * limit))
            quota[area] = min(n, len(area_ids[area]), remaining)
            remaining -= quota[area]
            if remaining <= 0:
                break

    seen_ids: set[str] = set()
    total_yielded = 0

    for area in areas_with_ids:
        area_limit = quota.get(area, 0)
        yielded_for_area = 0

        for meal_id in area_ids[area]:
            if total_yielded >= limit:
                return
            if yielded_for_area >= area_limit:
                break
            if meal_id in seen_ids:
                continue

            seen_ids.add(meal_id)
            meal = lookup_meal(meal_id)
            time.sleep(0.4)

            if meal:
                meal["_area"] = area  # inyectamos el área para heurísticas
                yield meal
                yielded_for_area += 1
                total_yielded += 1
