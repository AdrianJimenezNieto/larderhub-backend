INSERT INTO products (barcode, name, category, standard_unit, image_url) VALUES
('8410000000001', 'Leche Entera 1L',       'Lácteos',   'litros',     'https://img.com/leche.jpg'),
('8410000000002', 'Yogur Natural',          'Lácteos',   'unidades',   NULL),
('8410000000003', 'Queso Rallado',          'Lácteos',   'gramos',     NULL),
('8410000000014', 'Huevos XL (12)',         'Lácteos',   'unidades',   NULL),
('8410000000004', 'Arroz Redondo 1kg',      'Despensa',  'kilogramos', NULL),
('8410000000005', 'Pasta Penne 500g',       'Despensa',  'gramos',     NULL),
('8410000000006', 'Tomate Frito',           'Despensa',  'unidades',   NULL),
('8410000000007', 'Aceite de Oliva',        'Despensa',  'litros',     NULL),
('8410000000008', 'Atún en conserva',       'Despensa',  'unidades',   NULL),
('8410000000009', 'Lentejas Pardinas',      'Despensa',  'gramos',     NULL),
('8410000000010', 'Café Molido',            'Despensa',  'gramos',     NULL),
('8410000000020', 'Cereales Avena',         'Despensa',  'gramos',     NULL),
('8410000000011', 'Manzana Golden',         'Frescos',   'kilogramos', NULL),
('8410000000012', 'Plátanos',               'Frescos',   'kilogramos', NULL),
('8410000000013', 'Pechuga de Pollo',       'Frescos',   'gramos',     NULL),
('8410000000015', 'Agua Mineral 1.5L',      'Bebidas',   'litros',     NULL),
('8410000000016', 'Vino Tinto',             'Bebidas',   'unidades',   NULL),
('8410000000017', 'Detergente Ropa',        'Limpieza',  'litros',     NULL),
('8410000000018', 'Lavavajillas',           'Limpieza',  'unidades',   NULL),
('8410000000019', 'Papel Higiénico',        'Limpieza',  'unidades',   NULL)
ON CONFLICT (barcode) DO NOTHING;

-- ============================================================
-- Seed user (author of sample recipes)
-- ============================================================
INSERT INTO users (username, email, password, role)
SELECT 'seed_chef', 'chef@larderhub.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'seed_chef');

-- ============================================================
-- Sample recipes (use product barcode lookups for ingredient IDs)
-- ============================================================

-- 1. Tortilla de Patata
INSERT INTO recipes (title, description, image_url, author_id, cooking_time_minutes, difficulty, servings, created_at)
SELECT 'Tortilla de Patata',
       'Clásica tortilla española con huevos y aceite de oliva. Jugosa por dentro y dorada por fuera.',
       NULL,
       (SELECT id FROM users WHERE username = 'seed_chef'),
       30, 'Fácil', 4, NOW()
WHERE NOT EXISTS (SELECT 1 FROM recipes WHERE title = 'Tortilla de Patata');

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Tortilla de Patata'),
       (SELECT id FROM products WHERE barcode = '8410000000014'),
       6, 'unidades'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Tortilla de Patata')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000014')
);

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Tortilla de Patata'),
       (SELECT id FROM products WHERE barcode = '8410000000007'),
       0.1, 'litros'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Tortilla de Patata')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000007')
);

INSERT INTO recipe_steps (recipe_id, step_number, step_title, description, timer_minutes)
SELECT (SELECT id FROM recipes WHERE title = 'Tortilla de Patata'), 1,
       'Batir los huevos', 'Bate los huevos con una pizca de sal hasta obtener una mezcla homogénea.', NULL
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_steps
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Tortilla de Patata') AND step_number = 1
);

INSERT INTO recipe_steps (recipe_id, step_number, step_title, description, timer_minutes)
SELECT (SELECT id FROM recipes WHERE title = 'Tortilla de Patata'), 2,
       'Cuajar la tortilla', 'Calienta el aceite en una sartén antiadherente. Vierte los huevos y cocina a fuego medio hasta que cuaje.', 5
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_steps
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Tortilla de Patata') AND step_number = 2
);

INSERT INTO recipe_steps (recipe_id, step_number, step_title, description, timer_minutes)
SELECT (SELECT id FROM recipes WHERE title = 'Tortilla de Patata'), 3,
       'Dar la vuelta', 'Cubre la sartén con un plato y da la vuelta con un movimiento rápido. Desliza de nuevo a la sartén.', 3
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_steps
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Tortilla de Patata') AND step_number = 3
);

-- 2. Arroz con Atún
INSERT INTO recipes (title, description, image_url, author_id, cooking_time_minutes, difficulty, servings, created_at)
SELECT 'Arroz con Atún',
       'Plato rápido y nutritivo con arroz, atún en conserva y salsa de tomate. Perfecto para el día a día.',
       NULL,
       (SELECT id FROM users WHERE username = 'seed_chef'),
       25, 'Fácil', 2, NOW()
WHERE NOT EXISTS (SELECT 1 FROM recipes WHERE title = 'Arroz con Atún');

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Arroz con Atún'),
       (SELECT id FROM products WHERE barcode = '8410000000004'),
       0.2, 'kilogramos'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Arroz con Atún')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000004')
);

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Arroz con Atún'),
       (SELECT id FROM products WHERE barcode = '8410000000008'),
       2, 'unidades'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Arroz con Atún')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000008')
);

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Arroz con Atún'),
       (SELECT id FROM products WHERE barcode = '8410000000006'),
       1, 'unidades'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Arroz con Atún')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000006')
);

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Arroz con Atún'),
       (SELECT id FROM products WHERE barcode = '8410000000007'),
       0.05, 'litros'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Arroz con Atún')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000007')
);

INSERT INTO recipe_steps (recipe_id, step_number, step_title, description, timer_minutes)
SELECT (SELECT id FROM recipes WHERE title = 'Arroz con Atún'), 1,
       'Cocer el arroz', 'Cuece el arroz en agua con sal según las instrucciones del paquete.', 18
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_steps
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Arroz con Atún') AND step_number = 1
);

INSERT INTO recipe_steps (recipe_id, step_number, step_title, description, timer_minutes)
SELECT (SELECT id FROM recipes WHERE title = 'Arroz con Atún'), 2,
       'Mezclar y servir', 'Escurre el arroz. Mezcla con el atún escurrido y el tomate frito. Saltea 2 minutos.', 2
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_steps
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Arroz con Atún') AND step_number = 2
);

-- 3. Pollo con Arroz
INSERT INTO recipes (title, description, image_url, author_id, cooking_time_minutes, difficulty, servings, created_at)
SELECT 'Pollo con Arroz',
       'Pechuga de pollo jugosa acompañada de arroz. Un plato equilibrado, alto en proteínas y fácil de preparar.',
       NULL,
       (SELECT id FROM users WHERE username = 'seed_chef'),
       40, 'Media', 2, NOW()
WHERE NOT EXISTS (SELECT 1 FROM recipes WHERE title = 'Pollo con Arroz');

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Pollo con Arroz'),
       (SELECT id FROM products WHERE barcode = '8410000000013'),
       400, 'gramos'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Pollo con Arroz')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000013')
);

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Pollo con Arroz'),
       (SELECT id FROM products WHERE barcode = '8410000000004'),
       0.2, 'kilogramos'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Pollo con Arroz')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000004')
);

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Pollo con Arroz'),
       (SELECT id FROM products WHERE barcode = '8410000000007'),
       0.05, 'litros'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Pollo con Arroz')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000007')
);

INSERT INTO recipe_steps (recipe_id, step_number, step_title, description, timer_minutes)
SELECT (SELECT id FROM recipes WHERE title = 'Pollo con Arroz'), 1,
       'Sellar el pollo', 'Corta la pechuga en filetes. Calienta aceite en una sartén y sella el pollo 3 min por cada lado.', 6
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_steps
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Pollo con Arroz') AND step_number = 1
);

INSERT INTO recipe_steps (recipe_id, step_number, step_title, description, timer_minutes)
SELECT (SELECT id FROM recipes WHERE title = 'Pollo con Arroz'), 2,
       'Cocer el arroz', 'En otra olla, cuece el arroz en agua con sal durante 18 minutos.', 18
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_steps
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Pollo con Arroz') AND step_number = 2
);

INSERT INTO recipe_steps (recipe_id, step_number, step_title, description, timer_minutes)
SELECT (SELECT id FROM recipes WHERE title = 'Pollo con Arroz'), 3,
       'Servir', 'Sirve el pollo sobre el arroz. Añade una pizca de sal y pimienta al gusto.', NULL
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_steps
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Pollo con Arroz') AND step_number = 3
);

-- 4. Lentejas Estofadas
INSERT INTO recipes (title, description, image_url, author_id, cooking_time_minutes, difficulty, servings, created_at)
SELECT 'Lentejas Estofadas',
       'Lentejas pardinas cocinadas a fuego lento con aceite de oliva. Reconfortantes y muy nutritivas.',
       NULL,
       (SELECT id FROM users WHERE username = 'seed_chef'),
       45, 'Fácil', 4, NOW()
WHERE NOT EXISTS (SELECT 1 FROM recipes WHERE title = 'Lentejas Estofadas');

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Lentejas Estofadas'),
       (SELECT id FROM products WHERE barcode = '8410000000009'),
       400, 'gramos'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Lentejas Estofadas')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000009')
);

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Lentejas Estofadas'),
       (SELECT id FROM products WHERE barcode = '8410000000007'),
       0.05, 'litros'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Lentejas Estofadas')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000007')
);

INSERT INTO recipe_steps (recipe_id, step_number, step_title, description, timer_minutes)
SELECT (SELECT id FROM recipes WHERE title = 'Lentejas Estofadas'), 1,
       'Remojar las lentejas', 'Lava las lentejas y déjalas en remojo 30 minutos. Escurre antes de cocinar.', 30
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_steps
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Lentejas Estofadas') AND step_number = 1
);

INSERT INTO recipe_steps (recipe_id, step_number, step_title, description, timer_minutes)
SELECT (SELECT id FROM recipes WHERE title = 'Lentejas Estofadas'), 2,
       'Cocinar a fuego lento', 'Cubre las lentejas con agua, añade aceite y sal. Cocina a fuego medio-bajo 35 minutos.', 35
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_steps
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Lentejas Estofadas') AND step_number = 2
);

-- 5. Pasta con Tomate
INSERT INTO recipes (title, description, image_url, author_id, cooking_time_minutes, difficulty, servings, created_at)
SELECT 'Pasta con Tomate',
       'Pasta penne con salsa de tomate frito y un chorrito de aceite de oliva. El clásico de siempre.',
       NULL,
       (SELECT id FROM users WHERE username = 'seed_chef'),
       20, 'Fácil', 2, NOW()
WHERE NOT EXISTS (SELECT 1 FROM recipes WHERE title = 'Pasta con Tomate');

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Pasta con Tomate'),
       (SELECT id FROM products WHERE barcode = '8410000000005'),
       200, 'gramos'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Pasta con Tomate')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000005')
);

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Pasta con Tomate'),
       (SELECT id FROM products WHERE barcode = '8410000000006'),
       1, 'unidades'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Pasta con Tomate')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000006')
);

INSERT INTO recipe_ingredients (recipe_id, product_id, quantity, unit)
SELECT (SELECT id FROM recipes WHERE title = 'Pasta con Tomate'),
       (SELECT id FROM products WHERE barcode = '8410000000007'),
       0.05, 'litros'
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_ingredients
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Pasta con Tomate')
    AND product_id = (SELECT id FROM products WHERE barcode = '8410000000007')
);

INSERT INTO recipe_steps (recipe_id, step_number, step_title, description, timer_minutes)
SELECT (SELECT id FROM recipes WHERE title = 'Pasta con Tomate'), 1,
       'Cocer la pasta', 'Cuece la pasta en agua con sal siguiendo el tiempo indicado en el envase.', 10
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_steps
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Pasta con Tomate') AND step_number = 1
);

INSERT INTO recipe_steps (recipe_id, step_number, step_title, description, timer_minutes)
SELECT (SELECT id FROM recipes WHERE title = 'Pasta con Tomate'), 2,
       'Añadir la salsa', 'Escurre la pasta. En la misma olla, calienta el tomate frito con un chorrito de aceite. Mezcla con la pasta.', 3
WHERE NOT EXISTS (
  SELECT 1 FROM recipe_steps
  WHERE recipe_id = (SELECT id FROM recipes WHERE title = 'Pasta con Tomate') AND step_number = 2
);