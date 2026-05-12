"""
Diccionario hardcoded inglés → español para términos de cocina.
Produce Spanglish para lo que no está en el dict — aceptable como
dato bootstrap; el usuario puede editar las recetas desde la UI.
"""

import re
from typing import Optional

# ---------------------------------------------------------------------------
# Diccionario principal (ordenado de más específico a más genérico)
# ---------------------------------------------------------------------------
ES_DICT: dict[str, str] = {
    # Proteínas animales
    "chicken breasts": "pechugas de pollo",
    "chicken breast": "pechuga de pollo",
    "chicken thighs": "muslos de pollo",
    "chicken thigh": "muslo de pollo",
    "chicken legs": "muslos de pollo",
    "chicken stock": "caldo de pollo",
    "chicken broth": "caldo de pollo",
    "whole chicken": "pollo entero",
    "chicken": "pollo",
    "ground beef": "carne picada de ternera",
    "beef mince": "carne picada de ternera",
    "beef stock": "caldo de carne",
    "beef broth": "caldo de carne",
    "beef": "ternera",
    "pork loin": "lomo de cerdo",
    "pork belly": "panceta de cerdo",
    "pork": "cerdo",
    "lamb": "cordero",
    "turkey": "pavo",
    "duck": "pato",
    "veal": "ternera lechal",
    "salmon": "salmón",
    "tuna": "atún",
    "cod": "bacalao",
    "shrimp": "gambas",
    "prawns": "gambas",
    "mussels": "mejillones",
    "clams": "almejas",
    "squid": "calamar",
    "octopus": "pulpo",
    "anchovies": "anchoas",
    "sardines": "sardinas",
    "sea bass": "lubina",
    "haddock": "eglefino",
    "fish": "pescado",
    "eggs": "huevos",
    "egg": "huevo",
    # Lácteos
    "whole milk": "leche entera",
    "skimmed milk": "leche desnatada",
    "milk": "leche",
    "double cream": "nata para montar",
    "single cream": "nata líquida",
    "heavy cream": "nata para montar",
    "cream": "nata",
    "butter": "mantequilla",
    "parmesan cheese": "queso parmesano",
    "mozzarella cheese": "queso mozzarella",
    "mozzarella": "mozzarella",
    "cheddar cheese": "queso cheddar",
    "cream cheese": "queso crema",
    "feta cheese": "queso feta",
    "feta": "queso feta",
    "ricotta": "ricotta",
    "cheese": "queso",
    "yogurt": "yogur",
    "yoghurt": "yogur",
    # Verduras y hortalizas
    "cherry tomatoes": "tomates cherry",
    "tinned tomatoes": "tomates en conserva",
    "canned tomatoes": "tomates en conserva",
    "plum tomatoes": "tomates pera",
    "tomato paste": "concentrado de tomate",
    "tomato puree": "puré de tomate",
    "tomato sauce": "salsa de tomate",
    "tomatoes": "tomates",
    "tomato": "tomate",
    "red onion": "cebolla roja",
    "spring onion": "cebolleta",
    "onions": "cebollas",
    "onion": "cebolla",
    "garlic cloves": "dientes de ajo",
    "garlic clove": "diente de ajo",
    "garlic": "ajo",
    "red pepper": "pimiento rojo",
    "green pepper": "pimiento verde",
    "bell pepper": "pimiento",
    "peppers": "pimientos",
    "pepper": "pimienta",
    "carrots": "zanahorias",
    "carrot": "zanahoria",
    "potatoes": "patatas",
    "potato": "patata",
    "sweet potato": "boniato",
    "courgette": "calabacín",
    "zucchini": "calabacín",
    "aubergine": "berenjena",
    "eggplant": "berenjena",
    "broccoli": "brócoli",
    "cauliflower": "coliflor",
    "spinach": "espinacas",
    "lettuce": "lechuga",
    "leek": "puerro",
    "celery": "apio",
    "mushrooms": "champiñones",
    "mushroom": "champiñón",
    "asparagus": "espárragos",
    "artichoke": "alcachofa",
    "corn": "maíz",
    "peas": "guisantes",
    "green beans": "judías verdes",
    "lentils": "lentejas",
    "chickpeas": "garbanzos",
    "beans": "alubias",
    "cucumber": "pepino",
    "cabbage": "col",
    # Cereales, pasta y pan
    "basmati rice": "arroz basmati",
    "long grain rice": "arroz de grano largo",
    "rice": "arroz",
    "spaghetti": "espaguetis",
    "pasta": "pasta",
    "penne": "penne",
    "lasagne sheets": "láminas de lasaña",
    "lasagne": "lasaña",
    "tagliatelle": "tagliatelle",
    "bread crumbs": "pan rallado",
    "breadcrumbs": "pan rallado",
    "flour": "harina",
    "plain flour": "harina de trigo",
    "bread": "pan",
    "oats": "avena",
    # Aceites, vinagres y condimentos
    "olive oil": "aceite de oliva",
    "sunflower oil": "aceite de girasol",
    "vegetable oil": "aceite vegetal",
    "oil": "aceite",
    "white wine vinegar": "vinagre de vino blanco",
    "red wine vinegar": "vinagre de vino tinto",
    "balsamic vinegar": "vinagre balsámico",
    "vinegar": "vinagre",
    "soy sauce": "salsa de soja",
    "fish sauce": "salsa de pescado",
    "worcestershire sauce": "salsa worcestershire",
    "hot sauce": "salsa picante",
    "tahini": "tahini",
    "honey": "miel",
    "sugar": "azúcar",
    "brown sugar": "azúcar moreno",
    "salt": "sal",
    # Hierbas y especias
    "black pepper": "pimienta negra",
    "white pepper": "pimienta blanca",
    "chilli flakes": "copos de chile",
    "chilli powder": "chile en polvo",
    "chilli": "chile",
    "cumin": "comino",
    "paprika": "pimentón",
    "smoked paprika": "pimentón ahumado",
    "turmeric": "cúrcuma",
    "coriander": "cilantro",
    "ground coriander": "cilantro molido",
    "oregano": "orégano",
    "basil": "albahaca",
    "parsley": "perejil",
    "thyme": "tomillo",
    "rosemary": "romero",
    "bay leaves": "hojas de laurel",
    "bay leaf": "hoja de laurel",
    "mint": "menta",
    "dill": "eneldo",
    "ginger": "jengibre",
    "cinnamon": "canela",
    "nutmeg": "nuez moscada",
    "saffron": "azafrán",
    "cayenne": "cayena",
    "cloves": "clavos",
    # Frutas
    "lemon juice": "zumo de limón",
    "lime juice": "zumo de lima",
    "lemon zest": "ralladura de limón",
    "lemon": "limón",
    "lime": "lima",
    "orange": "naranja",
    "tomatoes": "tomates",
    # Caldos y líquidos
    "vegetable stock": "caldo de verduras",
    "vegetable broth": "caldo de verduras",
    "water": "agua",
    "white wine": "vino blanco",
    "red wine": "vino tinto",
    # Varios
    "olive oil spray": "spray de aceite de oliva",
    "cooking spray": "spray antiadherente",
    "stock cube": "pastilla de caldo",
    "bouillon": "caldo",
    # Verbos y conectores (para steps)
    "preheat": "precalentar",
    "heat": "calentar",
    "cook": "cocinar",
    "boil": "hervir",
    "simmer": "cocer a fuego lento",
    "fry": "freír",
    "stir-fry": "saltear",
    "bake": "hornear",
    "roast": "asar",
    "grill": "asar a la plancha",
    "mix": "mezclar",
    "stir": "remover",
    "chop": "picar",
    "dice": "cortar en dados",
    "slice": "cortar en láminas",
    "mince": "picar fino",
    "grate": "rallar",
    "drain": "escurrir",
    "rinse": "lavar",
    "marinate": "marinar",
    "season": "sazonar",
    "add": "añadir",
    "serve": "servir",
    "combine": "combinar",
    "place": "colocar",
    "remove": "retirar",
    "cover": "cubrir",
    "pour": "verter",
    "bring": "llevar",
    "let": "dejar",
    "until": "hasta que",
    "then": "luego",
    "minutes": "minutos",
    "minute": "minuto",
    "hours": "horas",
    "hour": "hora",
    "medium heat": "fuego medio",
    "high heat": "fuego alto",
    "low heat": "fuego lento",
    "oven": "horno",
    "pan": "sartén",
    "pot": "olla",
}

# Construir lookup case-insensitive de frases (ordenado por longitud desc para priorizar frases largas)
_PHRASE_KEYS = sorted(ES_DICT.keys(), key=len, reverse=True)


def translate_term(text: str) -> str:
    """Traduce un término único (ingrediente). Devuelve el texto en español o el original."""
    key = text.lower().strip()
    return ES_DICT.get(key, text)


def translate_text(text: str) -> str:
    """
    Sustituye términos conocidos en un bloque de texto.
    Respeta mayúsculas iniciales. El resultado puede ser Spanglish.
    """
    if not text:
        return text

    result = text
    for en_phrase in _PHRASE_KEYS:
        es_phrase = ES_DICT[en_phrase]
        # Sustituir de forma case-insensitive pero preservar si empieza en mayúscula
        pattern = re.compile(re.escape(en_phrase), re.IGNORECASE)
        def _replace(m: re.Match) -> str:
            original = m.group(0)
            if original[0].isupper():
                return es_phrase.capitalize()
            return es_phrase
        result = pattern.sub(_replace, result)

    return result


def generate_title(meal: dict) -> Optional[str]:
    """
    Intenta generar un título en español para la receta.
    Devuelve None si no es posible traducir suficientes palabras.
    """
    area = meal.get("_area", "")
    raw_title = (meal.get("strMeal") or "").strip()

    if not raw_title:
        return None

    # Recetas españolas: el título suele estar ya en español o ser un nombre propio
    if area == "Spanish":
        return raw_title

    translated = translate_text(raw_title)

    # Contar palabras "sin traducir" (igual que el original, ignorando artículos)
    stopwords = {"with", "and", "the", "a", "of", "in", "on", "for"}
    original_words = [w for w in raw_title.lower().split() if w not in stopwords]
    translated_lower = translated.lower()
    untranslated = sum(
        1 for w in original_words if w in translated_lower
    )

    # Si más de la mitad de las palabras sustantivas quedan en inglés, descartar
    content_words = [w for w in original_words if len(w) > 3]
    if content_words and untranslated / len(content_words) > 0.6:
        return None

    return translated.title()
