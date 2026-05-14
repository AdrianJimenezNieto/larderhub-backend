"""
Entrypoint del seed de recetas desde TheMealDB.

Uso:
  python seed_recipes.py [--dry-run] [--api-url URL] [--limit N] [--areas A,B,C] [--seeder-password P]

Requiere que el seed de productos ya se haya ejecutado (necesita el catálogo para el matching).
Lee SEEDER_PASSWORD del entorno o de un fichero .env si python-dotenv está disponible.
"""

import argparse
import os
import sys
from collections import defaultdict
from pathlib import Path

from tqdm import tqdm

from api_client import BackendClient
from ingredient_matcher import IngredientMatcher
from recipe_transformer import to_create_recipe_request
from themealdb_client import iter_meals_by_areas

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

_DEFAULT_AREAS = "Spanish,Italian,Mediterranean,Mexican"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Seed de recetas LarderHub desde TheMealDB")
    parser.add_argument("--api-url", default="http://localhost:8080", help="URL base del backend")
    parser.add_argument("--limit", type=int, default=25, help="Número máximo de recetas a importar")
    parser.add_argument("--areas", default=_DEFAULT_AREAS, help="Áreas geográficas de TheMealDB separadas por comas")
    parser.add_argument("--seeder-password", default=os.getenv("SEEDER_PASSWORD", "seedme123!"), help="Contraseña del usuario seeder")
    parser.add_argument("--dry-run", action="store_true", help="Muestra qué se crearía sin escribir en la BD")
    return parser.parse_args()


def _save_unmatched(unmatched: list[str]) -> None:
    if not unmatched:
        return
    out_path = Path(__file__).parent / "unmatched_ingredients.txt"
    # Acumular sin duplicados
    existing: set[str] = set()
    if out_path.exists():
        existing = set(out_path.read_text(encoding="utf-8").splitlines())
    new_terms = sorted(existing | set(unmatched))
    out_path.write_text("\n".join(new_terms), encoding="utf-8")
    print(f"\n  → {len(set(unmatched) - existing)} nuevos términos sin match guardados en {out_path.name}")


def main() -> None:
    args = parse_args()
    areas = [a.strip() for a in args.areas.split(",") if a.strip()]

    print(f"{'[DRY-RUN] ' if args.dry_run else ''}LarderHub seed recetas → {args.api_url}")
    print(f"Áreas: {areas}  ·  límite: {args.limit} recetas\n")

    client = BackendClient(args.api_url, args.seeder_password)

    print("Autenticando usuario seeder...")
    client.ensure_auth()

    print("Cargando catálogo de productos para matching...")
    product_index = client.load_product_index()
    print(f"  → {len(product_index)} productos en catálogo")

    print("Cargando recetas existentes (idempotencia)...")
    existing_titles = client.load_existing_recipe_titles()
    print(f"  → {len(existing_titles)} recetas ya importadas\n")

    if args.dry_run:
        print("  [DRY-RUN] No se escribirá ningún dato en la BD\n")

    matcher = IngredientMatcher(product_index)

    # Contadores globales
    counters: dict[str, int] = defaultdict(int)
    by_area: dict[str, dict[str, int]] = {area: defaultdict(int) for area in areas}

    print(f"Consultando TheMealDB para {args.limit} recetas...\n")

    with tqdm(total=args.limit, unit="receta", desc="Importando") as bar:
        for meal in iter_meals_by_areas(areas, args.limit):
            area = meal.get("_area", "?")
            area_c = by_area.get(area, defaultdict(int))

            # Idempotencia por título (pre-transformación)
            raw_title = (meal.get("strMeal") or "").strip()

            # Transformar
            matcher.clear_unmatched()
            payload = to_create_recipe_request(meal, matcher)

            if payload is None:
                reason = "título no traducible" if raw_title else "sin título"
                # Si el transformer rechaza por <3 ingredientes matcheados, el motivo es ese
                tqdm.write(f"  [DESC] {raw_title[:50]} — descartada")
                area_c["descartadas"] += 1
                counters["descartadas"] += 1
                by_area[area] = area_c
                bar.update(1)
                continue

            title = payload["title"]

            # Idempotencia: skip si ya existe (normalizado)
            if title.lower().strip() in existing_titles:
                tqdm.write(f"  [SKIP] {title[:60]} — ya existe")
                area_c["saltadas"] += 1
                counters["saltadas"] += 1
                by_area[area] = area_c
                bar.update(1)
                continue

            if args.dry_run:
                n_ing = len(payload["ingredients"])
                n_steps = len(payload["steps"])
                tqdm.write(
                    f"  [DRY] {title[:55]:<55} "
                    f"area={area:<14} "
                    f"ing={n_ing}  steps={n_steps}  "
                    f"dificultad={payload['difficulty']}"
                )
                area_c["importadas"] += 1
                counters["importadas"] += 1
            else:
                result = client.create_recipe(payload)
                if result["status"] == "created":
                    existing_titles.add(title.lower().strip())
                    area_c["importadas"] += 1
                    counters["importadas"] += 1
                else:
                    tqdm.write(f"  [ERR] {title[:50]}: {result.get('reason', '')[:80]}")
                    area_c["errores"] += 1
                    counters["errores"] += 1

            by_area[area] = area_c
            bar.update(1)

    # Guardar ingredientes sin match para revisión
    _save_unmatched(matcher.unmatched())

    # Resumen final
    print("\n" + "─" * 70)
    print("RESUMEN")
    print("─" * 70)
    print(f"{'Área':<16} {'Importadas':>11} {'Saltadas':>9} {'Descartadas':>12} {'Errores':>8}")
    print("─" * 70)
    for area in areas:
        c = by_area.get(area, {})
        print(
            f"{area:<16} {c.get('importadas', 0):>11} {c.get('saltadas', 0):>9} "
            f"{c.get('descartadas', 0):>12} {c.get('errores', 0):>8}"
        )
    print("─" * 70)
    print(
        f"{'TOTAL':<16} {counters.get('importadas', 0):>11} {counters.get('saltadas', 0):>9} "
        f"{counters.get('descartadas', 0):>12} {counters.get('errores', 0):>8}"
    )
    print()

    if counters.get("errores", 0) > 0:
        sys.exit(1)


if __name__ == "__main__":
    main()
