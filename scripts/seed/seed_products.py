"""
Entrypoint del seed de productos desde Open Food Facts.

Uso:
  python seed_products.py [--dry-run] [--api-url URL] [--per-category N] [--seeder-password P]

Lee SEEDER_PASSWORD del entorno o de un fichero .env si python-dotenv está disponible.
"""

import argparse
import os
import sys
from collections import defaultdict

from tqdm import tqdm

from api_client import BackendClient
from category_map import SEARCH_TARGETS, resolve_category
from off_client import fetch_by_category
from unit_parser import standard_unit

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Seed de productos LarderHub desde Open Food Facts")
    parser.add_argument("--api-url", default="http://localhost:8080", help="URL base del backend")
    parser.add_argument("--per-category", type=int, default=12, help="Productos a importar por categoría")
    parser.add_argument("--seeder-password", default=os.getenv("SEEDER_PASSWORD", "seedme123!"), help="Contraseña del usuario seeder")
    parser.add_argument("--dry-run", action="store_true", help="Muestra qué se crearía sin escribir en la BD")
    return parser.parse_args()


def main() -> None:
    args = parse_args()

    print(f"{'[DRY-RUN] ' if args.dry_run else ''}LarderHub seed → {args.api_url}")
    print(f"Categorías: {len(SEARCH_TARGETS)}  ·  {args.per_category} productos por categoría\n")

    client = BackendClient(args.api_url, args.seeder_password)

    if not args.dry_run:
        print("Autenticando usuario seeder...")
        client.ensure_auth()
        print("Cargando barcodes existentes...")
        existing_barcodes = client.load_existing_barcodes()
        print(f"  → {len(existing_barcodes)} productos ya en catálogo\n")
    else:
        existing_barcodes = set()

    # Contadores globales
    counters: dict[str, int] = defaultdict(int)  # created / skipped / filtered / error
    by_category: dict[str, dict[str, int]] = {}

    for off_tag, category_es in SEARCH_TARGETS:
        cat_counters: dict[str, int] = defaultdict(int)
        by_category[category_es] = cat_counters

        # OFF devuelve más de lo pedido; acotamos aquí
        fetch_limit = args.per_category * 2  # margen para filtrados
        collected = 0

        with tqdm(
            desc=f"  {category_es:<12}",
            total=args.per_category,
            unit="prod",
            leave=True,
        ) as bar:
            try:
                for product in fetch_by_category(off_tag, page_size=fetch_limit):
                    if collected >= args.per_category:
                        break

                    barcode = product["barcode"]

                    # Idempotencia local (antes de llamar al backend)
                    if barcode and barcode in existing_barcodes:
                        cat_counters["skipped"] += 1
                        counters["skipped"] += 1
                        continue

                    # Resolver la categoría española a partir de los tags del producto
                    # (puede diferir de la categoría buscada si la API devuelve supersets)
                    resolved = resolve_category(product["categories_tags"]) or category_es

                    unit = standard_unit(product.get("quantity"))

                    if args.dry_run:
                        tqdm.write(
                            f"    [DRY] {product['name'][:55]:<55} "
                            f"barcode={barcode or 'N/A':<14} "
                            f"categoría={resolved}  unidad={unit}"
                        )
                        cat_counters["created"] += 1
                        counters["created"] += 1
                    else:
                        result = client.create_product(
                            name=product["name"],
                            category=resolved,
                            barcode=barcode,
                            image_url=product.get("image_url"),
                            standard_unit=unit,
                        )
                        status = result["status"]
                        cat_counters[status] += 1
                        counters[status] += 1

                        if status == "error":
                            tqdm.write(f"    ERROR {product['name'][:40]}: {result.get('reason')}")
                        elif barcode:
                            existing_barcodes.add(barcode)

                    collected += 1
                    bar.update(1)
            except RuntimeError as exc:
                tqdm.write(f"  SKIP {category_es}: {exc}")
                counters["error"] += 1

    # Resumen final
    print("\n" + "─" * 60)
    print("RESUMEN")
    print("─" * 60)
    print(f"{'Categoría':<14} {'Creados':>8} {'Saltados':>9} {'Errores':>8}")
    print("─" * 60)
    for category_es, cat_c in by_category.items():
        print(
            f"{category_es:<14} {cat_c.get('created', 0):>8} "
            f"{cat_c.get('skipped', 0):>9} {cat_c.get('error', 0):>8}"
        )
    print("─" * 60)
    print(
        f"{'TOTAL':<14} {counters.get('created', 0):>8} "
        f"{counters.get('skipped', 0):>9} {counters.get('error', 0):>8}"
    )
    print()

    if counters.get("error", 0) > 0:
        sys.exit(1)


if __name__ == "__main__":
    main()
