"""
Cliente para la API de búsqueda de Open Food Facts.
Filtra por país España y categoría; devuelve productos con imagen y nombre.
"""

import time
import requests
from typing import Iterator

_BASE_URL = "https://es.openfoodfacts.org/cgi/search.pl"
_USER_AGENT = "LarderHub-Seed/1.0 (TFG DAW; github.com/larderhub)"
_RETRY_WAIT = 5  # segundos tras un 429


def _session() -> requests.Session:
    s = requests.Session()
    s.headers.update({"User-Agent": _USER_AGENT})
    return s


def _is_valid_ean(code: str) -> bool:
    return code.isdigit() and len(code) in (8, 12, 13, 14)


def fetch_by_category(off_tag: str, page_size: int = 30) -> Iterator[dict]:
    """
    Genera productos de Open Food Facts para la categoría `off_tag`
    vendidos en España que tengan nombre e imagen.

    off_tag puede ser el slug sin prefijo 'en:', p. ej. 'dairies', 'fruits'.
    """
    session = _session()
    params = {
        "action": "process",
        "json": 1,
        "page_size": page_size,
        "page": 1,
        "tagtype_0": "countries",
        "tag_contains_0": "contains",
        "tag_0": "spain",
        "tagtype_1": "categories",
        "tag_contains_1": "contains",
        "tag_1": off_tag,
        "fields": (
            "code,product_name_es,product_name,brands,"
            "image_front_url,categories_tags,quantity"
        ),
    }

    for attempt in range(3):
        try:
            resp = session.get(_BASE_URL, params=params, timeout=15)
            if resp.status_code == 429:
                time.sleep(_RETRY_WAIT * (attempt + 1))
                continue
            if resp.status_code >= 500:
                wait = _RETRY_WAIT * (2 ** attempt)
                time.sleep(wait)
                continue
            resp.raise_for_status()
            data = resp.json()
            break
        except requests.RequestException as exc:
            if attempt == 2:
                raise RuntimeError(
                    f"Error al consultar OFF para '{off_tag}': {exc}"
                ) from exc
            time.sleep(2)
    else:
        return

    for product in data.get("products", []):
        name = (product.get("product_name_es") or product.get("product_name") or "").strip()
        barcode = (product.get("code") or "").strip()
        image_url = product.get("image_front_url", "")

        if not name or len(name) < 3 or len(name) > 80:
            continue
        if not image_url:
            continue
        if barcode and not _is_valid_ean(barcode):
            barcode = ""

        yield {
            "name": name,
            "barcode": barcode or None,
            "image_url": image_url,
            "categories_tags": product.get("categories_tags", []),
            "quantity": product.get("quantity", ""),
        }

    # Cortesía con la API pública
    time.sleep(0.5)
