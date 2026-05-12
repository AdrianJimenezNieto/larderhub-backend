"""
Cliente autenticado para la API REST del backend de LarderHub.

Gestiona el ciclo de vida del usuario seeder (registro/login automático)
y expone operaciones de lectura/escritura sobre el catálogo de productos.
"""

import sys
from typing import Optional
import requests

_SEEDER_EMAIL = "seeder@larderhub.dev"
_SEEDER_USERNAME = "seeder"


class BackendClient:
    def __init__(self, base_url: str, seeder_password: str):
        self._base = base_url.rstrip("/")
        self._password = seeder_password
        self._token: Optional[str] = None
        self._session = requests.Session()

    # ------------------------------------------------------------------
    # Auth
    # ------------------------------------------------------------------

    def ensure_auth(self) -> None:
        """Obtiene un JWT válido, registrando al usuario seeder si no existe."""
        token = self._login()
        if token:
            self._token = token
            return

        # Usuario no existe todavía: registrar
        self._register()
        token = self._login()
        if not token:
            print("ERROR: no se pudo autenticar tras el registro. Revisa que el backend esté corriendo.", file=sys.stderr)
            sys.exit(1)
        self._token = token

    def _login(self) -> Optional[str]:
        try:
            resp = self._session.post(
                f"{self._base}/api/auth/login",
                json={"email": _SEEDER_EMAIL, "password": self._password},
                timeout=10,
            )
            if resp.status_code == 200:
                return resp.json().get("token")
            return None
        except requests.RequestException:
            return None

    def _register(self) -> None:
        resp = self._session.post(
            f"{self._base}/api/auth/register",
            json={
                "username": _SEEDER_USERNAME,
                "email": _SEEDER_EMAIL,
                "password": self._password,
            },
            timeout=10,
        )
        if resp.status_code not in (200, 201, 409):
            print(
                f"ERROR al registrar seeder: {resp.status_code} {resp.text}",
                file=sys.stderr,
            )
            sys.exit(1)

    def _auth_headers(self) -> dict:
        return {"Authorization": f"Bearer {self._token}"}

    # ------------------------------------------------------------------
    # Productos
    # ------------------------------------------------------------------

    def load_existing_barcodes(self) -> set[str]:
        """
        Carga todos los códigos de barras ya existentes en el catálogo.
        Usado para idempotencia: no intentamos crear productos duplicados.
        """
        resp = self._session.get(
            f"{self._base}/api/v1/products",
            headers=self._auth_headers(),
            timeout=15,
        )
        resp.raise_for_status()
        return {
            p["barcode"]
            for p in resp.json()
            if p.get("barcode")
        }

    def create_product(
        self,
        name: str,
        category: str,
        barcode: Optional[str],
        image_url: Optional[str],
        standard_unit: str,
    ) -> dict:
        """
        Crea un producto. Devuelve un dict con 'status':
        - 'created'  → producto insertado correctamente
        - 'skipped'  → barcode duplicado (idempotencia)
        - 'error'    → fallo inesperado
        """
        payload = {
            "name": name,
            "category": category,
            "standardUnit": standard_unit,
        }
        if barcode:
            payload["barcode"] = barcode
        if image_url:
            payload["imageUrl"] = image_url

        try:
            resp = self._session.post(
                f"{self._base}/api/v1/products",
                json=payload,
                headers=self._auth_headers(),
                timeout=10,
            )
        except requests.RequestException as exc:
            return {"status": "error", "reason": str(exc)}

        if resp.status_code in (200, 201):
            return {"status": "created", "id": resp.json().get("id")}

        # El backend lanza 400/500 con mensaje cuando el barcode ya existe
        if resp.status_code in (400, 409, 500):
            body = resp.text.lower()
            if "barcode" in body or "already exists" in body:
                return {"status": "skipped", "reason": "barcode duplicado"}

        return {"status": "error", "reason": f"HTTP {resp.status_code}: {resp.text[:120]}"}
