# Carga masiva de usuarios

Script en Python que genera **1.000.000 de usuarios ficticios** y los inserta en
PostgreSQL usando `COPY`, el mecanismo de carga masiva de la base.

Es una herramienta auxiliar: no forma parte del build de Maven ni de los tests.
Trabaja directamente contra el esquema que genera Hibernate.

---

## Requisitos

- Python 3.10 o superior
- La base creada y con las tablas ya generadas (levantar la aplicación al menos
  una vez, para que Hibernate cree el esquema)

```bash
python -m venv .venv
.venv\Scripts\python.exe -m pip install -r requirements.txt
```

---

## Uso

```bash
.venv\Scripts\python.exe bulk_load.py --fast
```

Sin `--dsn`, la conexión se arma leyendo el `.env` de la raíz del proyecto, el
mismo que usa `boot.ps1`.

| Opción | Default | Qué hace |
|---|---|---|
| `--count N` | `1000000` | Cantidad de usuarios a generar |
| `--fast` | apagado | Quita las claves foráneas durante la carga y las recrea al final |
| `--truncate` | apagado | Vacía `users`, `user_data` y `user_roles` antes de cargar |
| `--pool-size N` | `5000` | Tamaño del pool de nombres reutilizables |
| `--seed N` | — | Semilla, para que la generación sea reproducible |
| `--dsn ...` | del `.env` | Conexión explícita, por ejemplo al contenedor Docker |
| `--progress-every N` | `100000` | Cada cuántas filas informar avance (`0` para no informar) |

Ejemplos:

```bash
# Prueba rápida contra el contenedor Docker
.venv\Scripts\python.exe bulk_load.py --count 10000 --dsn "postgresql://postgres:postgres@localhost:5433/USERS_AUTH_DEV"

# El millón completo, vaciando primero
.venv\Scripts\python.exe bulk_load.py --truncate --fast
```

---

## Cómo funciona

Las tres tablas están encadenadas por claves foráneas, así que no se puede hacer
`COPY` directo sobre ellas. La solución es una **tabla de staging**:

1. **`COPY` a `bulk_staging`** — una tabla `UNLOGGED`, sin índices ni claves
   foráneas. Es la escritura más rápida que ofrece PostgreSQL. Las filas se
   generan y se envían por el socket con un generador, sin archivo CSV
   intermedio y sin mantener el millón de registros en memoria.
2. **`INSERT ... SELECT` a `users`** — PostgreSQL genera los `id`.
3. **`INSERT ... SELECT` a `user_data`** — `JOIN` contra `users` por `username`
   para resolver el `user_id`.
4. **`INSERT ... SELECT` a `user_roles`** — `JOIN` contra `role` por
   `description`.
5. **`ANALYZE`** de las tres tablas, para que el planificador tenga estadísticas
   al día.

Todo el trabajo pesado ocurre dentro de la base. Python solo genera filas.

### El pool de nombres

Faker genera del orden de decenas de miles de registros por segundo, así que
llamarlo un millón de veces dominaría el tiempo total. En vez de eso se genera
un pool acotado (5.000 nombres, apellidos, direcciones y teléfonos) una sola
vez, y después se combinan sus elementos al azar. Los datos siguen pareciendo
reales y la generación deja de ser el cuello de botella.

La unicidad de `username` está garantizada por el índice de fila que se le
concatena, no por el pool.

### Distribución de roles

Exactamente **1 `admin`**, **5 `manager`** y el resto **`guest`**, con las
posiciones privilegiadas elegidas al azar a lo largo de todo el archivo. Los
tres roles se crean si no existen.

---

## Rendimiento medido

Un millón de usuarios contra PostgreSQL 18 en Docker:

| Etapa | Sin `--fast` | Con `--fast` |
|---|---|---|
| `COPY` a staging | 6,8 s | 6,8 s |
| `INSERT` a `users` | 32,0 s | 29,0 s |
| `INSERT` a `user_data` | 32,5 s | 6,4 s |
| `INSERT` a `user_roles` | 68,2 s | 23,3 s |
| Recrear claves foráneas | — | 1,4 s |
| **Total** | **140,7 s** | **68,0 s** |

El `COPY` mueve 1.000.000 de filas en menos de 7 segundos: unas 154.000 filas
por segundo. El costo real está en los `INSERT ... SELECT`, por el mantenimiento
de índices y la verificación de claves foráneas.

### Por qué `--fast` cambia tanto

Con las claves foráneas activas, PostgreSQL dispara **un trigger de verificación
por cada fila insertada**. `user_roles` tiene dos claves foráneas, así que un
millón de filas implica dos millones de verificaciones individuales.

Al quitarlas antes de la carga y recrearlas después, la validación se hace **una
sola vez sobre toda la tabla**, con un plan de conjunto en lugar de fila por
fila. Recrear las tres claves foráneas tarda 1,4 segundos y ahorra más de 70.

Es la técnica estándar de cualquier carga masiva. La contrapartida es que
durante la carga la base queda temporalmente sin esas garantías de integridad,
así que **no debe usarse sobre una base en producción con tráfico**.

### Por qué no se usa JPA

El camino obvio, `userRepository.saveAll(...)`, tardaría horas. La razón está en
la propia entidad:

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

`IDENTITY` **deshabilita el batching de Hibernate por completo**: necesita el id
generado inmediatamente después de cada `INSERT`, así que no puede agrupar
sentencias y hace un viaje a la base por cada fila. Para batchear con JPA habría
que pasar a `SEQUENCE` con `allocationSize`.

---

## Verificar el resultado

```sql
SELECT 'users' AS tabla, count(*) FROM users
UNION ALL SELECT 'user_data', count(*) FROM user_data
UNION ALL SELECT 'user_roles', count(*) FROM user_roles;

SELECT r.description, count(*)
FROM user_roles ur JOIN role r ON r.id = ur.role_id
GROUP BY r.description ORDER BY 2 DESC;
```

Un millón de usuarios ocupa alrededor de **440 MB** entre datos e índices.

Para vaciar todo:

```sql
TRUNCATE user_roles, user_data, users RESTART IDENTITY CASCADE;
```
