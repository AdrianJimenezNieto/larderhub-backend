# Pruebas del seed de recetas — checklist para verificar

Slice asociada: 13b (recetas vía TheMealDB).
Rama: `feature/seed-recipes-themealdb`.

**Pre-requisito obligatorio:** el seed de productos (`seed_products.py`) debe haberse ejecutado
correctamente antes — el matching de ingredientes depende de que el catálogo tenga productos.

---

## Pre-requisitos

```bash
# 1. Levantar Postgres y Spring Boot
cd larderhub-backend
docker-compose up -d
mvn spring-boot:run

# 2. Ejecutar primero el seed de productos (si no se ha hecho aún)
cd scripts/seed
.venv\Scripts\activate
python seed_products.py

# 3. Instalar nueva dependencia thefuzz
pip install -r requirements.txt
```

---

## Prueba 1 — Dry-run

```bash
python seed_recipes.py --dry-run
```

**Resultado esperado:**
- Imprime `[DRY-RUN] LarderHub seed recetas → http://localhost:8080`
- Por cada receta muestra línea tipo:
  ```
  [DRY] Pollo A La Portuguesa       area=Spanish        ing=6  steps=3  dificultad=Media
  ```
- Líneas `[DESC]` para recetas descartadas (sin título traducible o <3 ingredientes).
- Tabla final con desglose por área.
- **La BD no se modifica** (no llama al backend).

## Prueba 2 — Ejecución real (límite reducido para primer test)

```bash
python seed_recipes.py --limit 5 --areas Spanish
```

**Resultado esperado:**
- Autentica al seeder, carga productos y recetas existentes.
- Importa hasta 5 recetas del área Spanish.
- Tabla muestra recetas importadas > 0.

Verificación SQL:
```sql
SELECT title, difficulty, cooking_time_minutes, servings
FROM recipes
WHERE author_id = (SELECT id FROM users WHERE email = 'seeder@larderhub.dev');
```

## Prueba 3 — Ejecución completa

```bash
python seed_recipes.py --limit 25
```

**Resultado esperado:**
- ~15-25 recetas importadas (variará según matching de ingredientes).
- Tabla con desglose por área.
- Fichero `unmatched_ingredients.txt` creado con términos sin match.

Verificación SQL:
```sql
-- Conteo total de recetas (5 data.sql + N seedeadas)
SELECT COUNT(*) FROM recipes;

-- Todas las recetas seedeadas tienen ≥3 ingredientes
SELECT r.title, COUNT(ri.id) AS n
FROM recipes r
JOIN recipe_ingredients ri ON ri.recipe_id = r.id
WHERE r.author_id = (SELECT id FROM users WHERE email = 'seeder@larderhub.dev')
GROUP BY r.title
HAVING COUNT(ri.id) < 3;
-- → debe devolver 0 filas

-- Todas tienen ≥1 step
SELECT r.title, COUNT(rs.id) AS n
FROM recipes r
JOIN recipe_steps rs ON rs.recipe_id = r.id
WHERE r.author_id = (SELECT id FROM users WHERE email = 'seeder@larderhub.dev')
GROUP BY r.title
HAVING COUNT(rs.id) = 0;
-- → debe devolver 0 filas
```

## Prueba 4 — Idempotencia

```bash
python seed_recipes.py --limit 25
```

Segunda ejecución inmediatamente después.

**Resultado esperado:**
- Toda la columna "Saltadas" con el mismo número que "Importadas" en la primera ejecución.
- La columna "Importadas" muestra 0.
- El COUNT(*) de recetas no aumenta.

## Prueba 5 — Verificación UI end-to-end

```bash
cd larderhub-frontend && npm run dev
```

1. Login con `seeder@larderhub.dev` / `seedme123!`
2. Pestaña **Recetas**: aparecen las nuevas recetas con imagen (strMealThumb de TheMealDB).
3. Abrir una receta (ej. Paella, Gazpacho). Verificar:
   - Título en español o coherente.
   - Lista de ingredientes con nombres de productos del catálogo.
   - Steps visibles con texto en español (puede ser Spanglish — es una limitación conocida).
4. **Sugerencias**: añadir manualmente `Pollo` o `Arroz` al inventario del hogar.
   Ir a Recetas → Sugerencias. Deben aparecer recetas que usen esos ingredientes.
5. **Valorar una receta**: dar 4 estrellas → el promedio debe actualizarse.

## Prueba 6 — Recetas existentes de data.sql sin afectar

```sql
SELECT r.title, COUNT(ri.id) AS n
FROM recipes r
JOIN recipe_ingredients ri ON ri.recipe_id = r.id
WHERE r.title IN ('Tortilla de Patata', 'Arroz con Atún', 'Pollo con Arroz', 'Lentejas Estofadas', 'Pasta con Tomate')
GROUP BY r.title;
```
Las 5 recetas originales deben seguir teniendo sus ingredientes intactos.

---

## Troubleshooting

| Síntoma | Causa probable | Solución |
|---|---|---|
| 0 recetas importadas en área Spanish | TheMealDB devuelve pocos resultados para esa área | Probar `--areas Italian,Mexican` |
| Todas las recetas descartadas | Catálogo de productos vacío (seed de productos no ejecutado) | Ejecutar `seed_products.py` primero |
| `ModuleNotFoundError: thefuzz` | `pip install -r requirements.txt` no ejecutado | Instalar dependencias en el venv |
| HTTP 400 al crear receta | `productId` inválido (producto borrado entre load y create) | Relanzar el script (recarga el índice) |
| Títulos en inglés en la UI | Receta de área no Spanish sin suficientes términos en el dict | Normal, limitación conocida; se puede editar desde la UI |

---

## Limitaciones conocidas (para TFG sección 7.3)

1. **Traducción Spanglish**: el diccionario substitutivo no traduce frases complejas correctamente.
   Las recetas son editables desde la UI (slice 10 ✅).
2. **Cobertura del matching**: TheMealDB usa nombres anglosajones específicos ("Scotch Bonnet Chilli")
   que el fuzzy no resolverá. Revisar `unmatched_ingredients.txt` para ampliar el diccionario.
3. **TheMealDB sin campos numéricos**: cookingTimeMinutes y servings son heurísticos.
4. **Dependencia del orden**: el seed de recetas DEBE ejecutarse después del seed de productos.
