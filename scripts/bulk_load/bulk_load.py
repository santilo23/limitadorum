"""Carga masiva de usuarios en la base de limitadorum.

Genera N usuarios ficticios y los inserta usando COPY, el mecanismo de carga
masiva de PostgreSQL. El trabajo pesado lo hace la base: este script solo
produce filas y las empuja por el socket, sin archivos intermedios ni
estructuras que mantengan el millon de registros en memoria.

Estrategia
----------
1. COPY de todas las filas a una tabla de staging sin indices ni claves
   foraneas, que es la operacion mas rapida que ofrece PostgreSQL.
2. INSERT ... SELECT desde staging hacia users, user_data y user_roles,
   resolviendo las claves foraneas con JOIN dentro de la base.

Uso
---
    python bulk_load.py                    # 1.000.000 de usuarios
    python bulk_load.py --count 10000      # una corrida de prueba
    python bulk_load.py --truncate         # vacia las tablas antes de cargar
    python bulk_load.py --dsn "postgresql://user:pass@host:5432/base"

Sin --dsn, la conexion se arma leyendo el archivo .env de la raiz del
proyecto, el mismo que usa boot.ps1.
"""

from __future__ import annotations

import argparse
import os
import random
import re
import sys
import time
import unicodedata
from pathlib import Path

try:
    import psycopg
except ImportError:
    sys.exit(
        "Falta psycopg. Instalar las dependencias con:\n"
        "    pip install -r requirements.txt"
    )

try:
    from faker import Faker
except ImportError:
    sys.exit(
        "Falta faker. Instalar las dependencias con:\n"
        "    pip install -r requirements.txt"
    )


PROJECT_ROOT = Path(__file__).resolve().parents[2]
ENV_FILE = PROJECT_ROOT / ".env"

# Distribucion de roles: 1 admin, 5 managers y el resto guest.
ADMIN_COUNT = 1
MANAGER_COUNT = 5
ROLES = ("admin", "manager", "guest")

STAGING_TABLE = "bulk_staging"


# --------------------------------------------------------------------------
# Conexion
# --------------------------------------------------------------------------

def read_env_file(path: Path) -> dict[str, str]:
    """Lee un archivo .env sencillo (KEY=VALUE, con # como comentario)."""
    values: dict[str, str] = {}
    if not path.is_file():
        return values
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        values[key.strip()] = value.strip()
    return values


def jdbc_to_dsn(jdbc_url: str, user: str, password: str) -> str:
    """Convierte jdbc:postgresql://host:puerto/base en un DSN de psycopg."""
    match = re.match(
        r"jdbc:postgresql://([^:/]+)(?::(\d+))?/(.+)$", jdbc_url.strip()
    )
    if not match:
        raise ValueError(f"No se pudo interpretar la URL JDBC: {jdbc_url}")
    host, port, dbname = match.group(1), match.group(2) or "5432", match.group(3)
    return psycopg.conninfo.make_conninfo(
        host=host, port=port, dbname=dbname, user=user, password=password
    )


def resolve_dsn(explicit: str | None) -> str:
    if explicit:
        return explicit
    env = read_env_file(ENV_FILE)
    jdbc_url = os.environ.get("DB_URL") or env.get("DB_URL")
    user = os.environ.get("DB_USER") or env.get("DB_USER") or "postgres"
    password = os.environ.get("DB_PASSWORD") or env.get("DB_PASSWORD") or ""
    if not jdbc_url:
        raise SystemExit(
            f"No se encontro DB_URL en {ENV_FILE}.\n"
            "Pasar la conexion con --dsn, o crear el .env a partir de .env.example."
        )
    return jdbc_to_dsn(jdbc_url, user, password)


# --------------------------------------------------------------------------
# Generacion de datos
# --------------------------------------------------------------------------

def slugify(value: str) -> str:
    """Normaliza un nombre para usarlo dentro de un username."""
    plain = unicodedata.normalize("NFKD", value)
    plain = plain.encode("ascii", "ignore").decode("ascii")
    return re.sub(r"[^a-z0-9]", "", plain.lower())


class NamePool:
    """Pool de datos ficticios reutilizables.

    Faker genera del orden de decenas de miles de registros por segundo, asi
    que llamarlo un millon de veces domina el tiempo total de la carga. En vez
    de eso se genera un pool acotado una sola vez y despues se combinan sus
    elementos al azar: los datos siguen pareciendo reales y la generacion pasa
    a ser practicamente gratis.
    """

    def __init__(self, size: int, locale: str = "es_AR", seed: int | None = None):
        faker = Faker(locale)
        if seed is not None:
            Faker.seed(seed)
            random.seed(seed)

        self.first_names = [faker.first_name() for _ in range(size)]
        self.last_names = [faker.last_name() for _ in range(size)]
        self.addresses = [
            faker.address().replace("\n", ", ") for _ in range(size)
        ]
        self.phones = [faker.phone_number() for _ in range(size)]
        self.domains = [
            "example.com", "mail.com", "um.edu.ar", "test.edu.ar", "gmail.com",
        ]

    def row(self, index: int, role: str) -> tuple:
        first = random.choice(self.first_names)
        last = random.choice(self.last_names)
        username = f"{slugify(first)}{slugify(last)}{index:07d}"
        domain = random.choice(self.domains)
        return (
            username,
            f"{username}@{domain}",
            first,
            last,
            random.choice(self.addresses),
            random.choice(self.phones),
            random.random() < 0.7,
            role,
        )


def role_assignment(total: int) -> dict[int, str]:
    """Elige al azar que posiciones reciben los roles privilegiados."""
    privileged = random.sample(range(total), min(ADMIN_COUNT + MANAGER_COUNT, total))
    assignment = {pos: "manager" for pos in privileged[ADMIN_COUNT:]}
    for pos in privileged[:ADMIN_COUNT]:
        assignment[pos] = "admin"
    return assignment


# --------------------------------------------------------------------------
# Carga
# --------------------------------------------------------------------------

def create_staging(cur) -> None:
    cur.execute(f"DROP TABLE IF EXISTS {STAGING_TABLE}")
    # UNLOGGED evita escribir en el WAL: la tabla no sobrevive a una caida del
    # servidor, que es irrelevante para datos temporales y acelera la carga.
    cur.execute(
        f"""
        CREATE UNLOGGED TABLE {STAGING_TABLE} (
            username     text,
            email        text,
            first_name   text,
            last_name    text,
            address      text,
            phone_number text,
            active       boolean,
            role         text
        )
        """
    )


def copy_rows(cur, pool: NamePool, total: int, progress_every: int) -> None:
    assignment = role_assignment(total)
    statement = f"""
        COPY {STAGING_TABLE}
            (username, email, first_name, last_name, address, phone_number, active, role)
        FROM STDIN
    """
    started = time.perf_counter()
    with cur.copy(statement) as copy:
        for i in range(total):
            copy.write_row(pool.row(i, assignment.get(i, "guest")))
            processed = i + 1
            if progress_every and processed % progress_every == 0:
                rate = processed / (time.perf_counter() - started)
                print(
                    f"  {processed:,} / {total:,} filas  ({rate:,.0f} filas/s)",
                    flush=True,
                )


def analyze_staging(cur) -> None:
    """Recolecta estadisticas de la tabla de staging.

    Es imprescindible: una tabla recien creada no tiene estadisticas, asi que
    el planificador la estima en unas pocas filas y elige Nested Loop con un
    Index Scan por fila contra users. Con las estadisticas al dia cambia a
    Hash Join y los INSERT ... SELECT pasan de minutos a segundos.
    """
    cur.execute(f"ANALYZE {STAGING_TABLE}")


def ensure_roles(cur) -> None:
    cur.executemany(
        """
        INSERT INTO role (description) VALUES (%s)
        ON CONFLICT (description) DO NOTHING
        """,
        [(role,) for role in ROLES],
    )


def promote(cur) -> tuple[dict[str, int], dict[str, float]]:
    """Mueve los datos de staging a las tablas reales resolviendo las FKs."""
    counts: dict[str, int] = {}
    timings: dict[str, float] = {}

    def run(name: str, sql: str) -> None:
        started = time.perf_counter()
        cur.execute(sql)
        counts[name] = cur.rowcount
        timings[name] = time.perf_counter() - started
        print(f"  {name}: {counts[name]:,} filas en {timings[name]:.1f} s", flush=True)

    # Los usuarios ya existentes se saltean: username tiene restriccion unique.
    run("users", f"""
        INSERT INTO users (username, email, active)
        SELECT s.username, s.email, s.active
        FROM {STAGING_TABLE} s
        ON CONFLICT (username) DO NOTHING
        """)

    run("user_data", f"""
        INSERT INTO user_data (first_name, last_name, address, phone_number, user_id)
        SELECT s.first_name, s.last_name, s.address, s.phone_number, u.id
        FROM {STAGING_TABLE} s
        JOIN users u ON u.username = s.username
        LEFT JOIN user_data d ON d.user_id = u.id
        WHERE d.id IS NULL
        """)

    run("user_roles", f"""
        INSERT INTO user_roles (user_id, role_id)
        SELECT u.id, r.id
        FROM {STAGING_TABLE} s
        JOIN users u ON u.username = s.username
        JOIN role  r ON r.description = s.role
        ON CONFLICT DO NOTHING
        """)

    return counts, timings


def drop_foreign_keys(cur) -> list[tuple[str, str, str]]:
    """Elimina las FKs de las tablas destino y devuelve sus definiciones.

    Con las FKs activas, PostgreSQL dispara un trigger de verificacion por cada
    fila insertada. Al recrearlas despues de la carga, la validacion se hace una
    sola vez sobre toda la tabla, que es mucho mas barato.
    """
    cur.execute(
        """
        SELECT conrelid::regclass::text, conname, pg_get_constraintdef(oid)
        FROM pg_constraint
        WHERE contype = 'f'
          AND conrelid::regclass::text = ANY(%s)
        """,
        (["user_data", "user_roles"],),
    )
    saved = cur.fetchall()
    for table, name, _ in saved:
        cur.execute(f'ALTER TABLE {table} DROP CONSTRAINT "{name}"')
    return saved


def restore_foreign_keys(cur, saved: list[tuple[str, str, str]]) -> None:
    for table, name, definition in saved:
        cur.execute(f'ALTER TABLE {table} ADD CONSTRAINT "{name}" {definition}')


def truncate_all(cur) -> None:
    cur.execute("TRUNCATE user_roles, user_data, users RESTART IDENTITY CASCADE")


# --------------------------------------------------------------------------

def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Carga masiva de usuarios ficticios en PostgreSQL usando COPY."
    )
    parser.add_argument("--count", type=int, default=1_000_000,
                        help="cantidad de usuarios a generar (default: 1000000)")
    parser.add_argument("--pool-size", type=int, default=5000,
                        help="tamano del pool de nombres reutilizables (default: 5000)")
    parser.add_argument("--dsn", default=None,
                        help="DSN de PostgreSQL; por defecto se arma desde el .env")
    parser.add_argument("--truncate", action="store_true",
                        help="vaciar users, user_data y user_roles antes de cargar")
    parser.add_argument("--seed", type=int, default=None,
                        help="semilla para hacer la generacion reproducible")
    parser.add_argument("--progress-every", type=int, default=100_000,
                        help="cada cuantas filas informar avance (0 para no informar)")
    parser.add_argument("--fast", action="store_true",
                        help="quitar las claves foraneas durante la carga y "
                             "recrearlas al final (mucho mas rapido)")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    dsn = resolve_dsn(args.dsn)
    safe = re.sub(r"password=\S+", "password=***", dsn)
    print(f"Conectando a: {safe}")

    total_started = time.perf_counter()

    print(f"Generando pool de {args.pool_size:,} nombres...")
    started = time.perf_counter()
    pool = NamePool(args.pool_size, seed=args.seed)
    print(f"  pool listo en {time.perf_counter() - started:.1f} s")

    with psycopg.connect(dsn) as conn:
        with conn.cursor() as cur:
            if args.truncate:
                print("Vaciando tablas...")
                truncate_all(cur)

            ensure_roles(cur)

            print(f"Cargando {args.count:,} filas en staging via COPY...")
            started = time.perf_counter()
            create_staging(cur)
            copy_rows(cur, pool, args.count, args.progress_every)
            analyze_staging(cur)
            copy_elapsed = time.perf_counter() - started
            print(f"  COPY completo en {copy_elapsed:.1f} s")

            print("Insertando en users, user_data y user_roles...")
            started = time.perf_counter()
            saved_fks: list[tuple[str, str, str]] = []
            if args.fast:
                saved_fks = drop_foreign_keys(cur)
                print(f"  claves foraneas desactivadas: {len(saved_fks)}")
            try:
                counts, _ = promote(cur)
            finally:
                if saved_fks:
                    fk_started = time.perf_counter()
                    restore_foreign_keys(cur, saved_fks)
                    print(f"  claves foraneas recreadas en "
                          f"{time.perf_counter() - fk_started:.1f} s")
            promote_elapsed = time.perf_counter() - started
            print(f"  INSERT ... SELECT completo en {promote_elapsed:.1f} s")

            cur.execute(f"DROP TABLE IF EXISTS {STAGING_TABLE}")
            cur.execute("ANALYZE users, user_data, user_roles")
        conn.commit()

    total_elapsed = time.perf_counter() - total_started
    print("\nResumen")
    print(f"  users insertados      : {counts['users']:,}")
    print(f"  user_data insertados  : {counts['user_data']:,}")
    print(f"  user_roles insertados : {counts['user_roles']:,}")
    print(f"  COPY                  : {copy_elapsed:.1f} s")
    print(f"  INSERT ... SELECT     : {promote_elapsed:.1f} s")
    print(f"  total                 : {total_elapsed:.1f} s")
    if total_elapsed > 0:
        print(f"  promedio              : {args.count / total_elapsed:,.0f} filas/s")


if __name__ == "__main__":
    main()
