# Pruebas del seed de productos — checklist para verificar

Slice asociada: 13 (fase productos, vía Open Food Facts).
Rama: `feature/seed-products-openfoodfacts`.

---

## Pre-requisitos

```bash
# 1. Levantar Postgres (desde la raíz del backend)
cd larderhub-backend
docker-compose up -d

# 2. Levantar Spring Boot en otra terminal
mvn spring-boot:run
# (espera a ver "Started LarderHubApplication")

# 3. Preparar el entorno Python para el seed
cd scripts/seed
python -m venv .venv
.venv\Scripts\activate         # PowerShell / cmd
# source .venv/bin/activate    # macOS/Linux

pip install -r requirements.txt
```

---

## Prueba 1 — Dry-run (no toca la BD)

```bash
python seed_products.py --dry-run
```

**Resultado esperado:**
- Imprime `[DRY-RUN] LarderHub seed → http://localhost:8080`
- Por cada categoría española imprime una barra `tqdm` y filas tipo:
  ```
  [DRY] Leche Pascual Semidesnatada ...  barcode=8410297122016  categoría=Lácteos  unidad=litros
  ```
- Resumen final con tabla de creados/saltados/errores. Todo debería ir a la columna *creados* en dry-run.
- **No se autentica ni hace POST**. Si revisas la BD, el conteo de productos no cambia.

## Prueba 2 — Autenticación del seeder

```bash
# Antes de correr el seed, verifica que el usuario no existe:
psql -h localhost -U postgres -d larderhub \
  -c "SELECT username, email FROM users WHERE email = 'seeder@larderhub.dev'"
# (Postgres password: 'password')
```

Si **no existe** → primera ejecución real lo creará vía `POST /api/auth/register`.
Si **existe** → hará login directamente.

```bash
python seed_products.py --per-category 3   # arranque ligero
```

**Resultado esperado:**
- Imprime "Autenticando usuario seeder..." sin errores.
- Imprime "Cargando barcodes existentes..." con un número ≥ 21 (los del `data.sql`).
- Procesa las 10 categorías con barra de progreso.

Verificación:
```bash
psql -h localhost -U postgres -d larderhub \
  -c "SELECT username, email, role FROM users WHERE email = 'seeder@larderhub.dev'"
```
Debe devolver una fila con `role = USER`.

## Prueba 3 — Inserción real con volumen completo

```bash
python seed_products.py --per-category 12
```

**Resultado esperado:**
- ~80–120 productos creados (algunos serán filtrados por OFF si no tienen imagen/nombre válido).
- Tabla final con desglose por categoría.

Verificación SQL:
```sql
-- Conteo por categoría (debe incluir las nuevas: Frutas, Verduras, Carnes, Pescados, Legumbres, Cereales, Aceites)
SELECT category, COUNT(*) FROM products GROUP BY category ORDER BY 2 DESC;

-- Productos con imagen
SELECT COUNT(*) FROM products WHERE image_url IS NOT NULL AND image_url <> '';

-- Sin barcode duplicados
SELECT barcode, COUNT(*) FROM products GROUP BY barcode HAVING COUNT(*) > 1;
-- → debe devolver 0 filas
```

## Prueba 4 — Idempotencia (re-ejecución)

Sin tocar nada, vuelve a lanzar:
```bash
python seed_products.py --per-category 12
```

**Resultado esperado:**
- La tabla final debe mostrar casi todo en la columna **Saltados** y muy pocos (o ninguno) **Creados**.
- El `COUNT(*)` de `products` no debería aumentar significativamente.

## Prueba 5 — Verificación end-to-end en la UI

Con el seed ya cargado y el frontend levantado:

```bash
cd larderhub-frontend && npm run dev
```

1. Loguéate con cualquier usuario (puedes usar el seeder: `seeder@larderhub.dev` / `seedme123!`).
2. Ve a **Despensa → Añadir producto**.
3. En el buscador escribe `leche`, `pollo`, `aceite`, `tomate`. Deberían aparecer múltiples resultados con sus imágenes reales de Open Food Facts.
4. Selecciona uno con imagen y verifica que la imagen carga sin errores en la consola del navegador.
5. Comprueba que el `standardUnit` mostrado es coherente (litros para leches, gramos para pasta, etc.).

## Prueba 6 — Recetas existentes siguen funcionando

`data.sql` mete 5 recetas que referencian productos por `barcode`. Verifica que no se hayan roto:

```sql
SELECT r.title, COUNT(ri.id) AS num_ingredientes
FROM recipes r LEFT JOIN recipe_ingredients ri ON ri.recipe_id = r.id
GROUP BY r.title;
```
Deben aparecer las 5 recetas (`Tortilla de Patata`, `Arroz con Atún`, `Pollo con Arroz`, `Lentejas Estofadas`, `Pasta con Tomate`) con sus ingredientes.

En la UI: ve a **Recetas** → debe seguir mostrándolas todas con sus pasos.

---

## Troubleshooting

| Síntoma | Causa probable | Solución |
|---|---|---|
| `ConnectionError: localhost:8080` | Backend no está corriendo | `mvn spring-boot:run` |
| `401 Unauthorized` al cargar barcodes | Token expiró | Re-ejecutar el script (refresca el JWT) |
| `429 Too Many Requests` de OFF | API rate-limited | El cliente reintenta tras 5s, esperar |
| Categoría con 0 creados | OFF no encontró productos ES con esos tags | Revisar `SEARCH_TARGETS` en `category_map.py` |
| Todos saltados pero la BD está vacía | `--api-url` apunta al backend equivocado | Verificar puerto |

---

## Bugs detectados durante el diseño de las pruebas

> Documentar aquí si aparece algo nuevo durante la verificación real de mañana.
> (Sección útil para la memoria del TFG, apartado 7.3.)
