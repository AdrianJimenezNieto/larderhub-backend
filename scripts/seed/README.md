# Seed de productos — LarderHub

Pobla el catálogo de productos llamando a la API pública de
[Open Food Facts](https://world.openfoodfacts.org) (licencia ODbL)
con filtro `countries=spain` y a la API REST del backend.

## Requisitos

- Python 3.10+
- Backend corriendo en `http://localhost:8080` (y la BD levantada)

## Instalación

```bash
cd scripts/seed
python -m venv .venv
# Windows
.venv\Scripts\activate
# macOS/Linux
source .venv/bin/activate

pip install -r requirements.txt
```

## Uso

```bash
# Dry-run — muestra qué se crearía sin tocar la BD
python seed_products.py --dry-run

# Ejecución real (12 productos por categoría ≈ 100-120 totales)
python seed_products.py

# Con opciones explícitas
python seed_products.py \
  --api-url http://localhost:8080 \
  --per-category 12 \
  --seeder-password seedme123!
```

### Opciones

| Flag | Default | Descripción |
|---|---|---|
| `--api-url` | `http://localhost:8080` | URL base del backend |
| `--per-category` | `12` | Productos a importar por categoría |
| `--seeder-password` | `seedme123!` | Contraseña del usuario seeder |
| `--dry-run` | `false` | Solo muestra, no escribe en la BD |

También se puede usar `SEEDER_PASSWORD` como variable de entorno (o en un `.env`).

## Idempotencia

El script carga todos los códigos de barras existentes antes de empezar.
Los productos cuyo barcode ya esté en la BD se omiten sin error.

## Datos de origen

Los datos provienen de **Open Food Facts**, proyecto colaborativo bajo licencia
[Open Database License (ODbL)](https://opendatacommons.org/licenses/odbl/).
Atribución requerida según los términos de la licencia.
