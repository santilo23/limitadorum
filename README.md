# limitadorum

Sistema de gestión de usuarios y roles desarrollado como trabajo práctico de la
carrera de Ingeniería en Informática de la **Universidad de Mendoza**.

La aplicación modela usuarios que pueden tener asignados uno o más roles, con el
objetivo de servir como base para un sistema de control de acceso y permisos.

---

## Stack

| Componente | Versión |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.1 |
| Spring Data JPA | (gestionada por Spring Boot) |
| PostgreSQL | driver `org.postgresql` |
| Maven | 3.9.16 (vía wrapper) |
| Lombok | (gestionada por Spring Boot) |
| H2 | solo en tests |

---

## Requisitos previos

- **JDK 25**. Verificalo con `java -version`; tiene que decir `25`, y tiene que
  ser un **JDK**, no un JRE (un JRE no incluye compilador y Maven falla con
  *"No compiler is provided in this environment"*).

  ```bash
  winget install EclipseAdoptium.Temurin.25.JDK
  ```

- **PostgreSQL** corriendo, con una base creada para la aplicación. No hace
  falta para correr los tests: usan H2 embebida.

  ```sql
  CREATE DATABASE limitadorum;
  ```

No hace falta instalar Maven: el repositorio incluye el *Maven Wrapper*
(`mvnw` / `mvnw.cmd`), que descarga la versión correcta la primera vez.

---

## Configuración

La conexión a la base se define en
[`src/main/resources/application.yaml`](src/main/resources/application.yaml) y
puede sobreescribirse por variables de entorno, sin tocar el repositorio:

| Variable | Default | Descripción |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/limitadorum` | URL JDBC de la base |
| `DB_USER` | `postgres` | Usuario de la base |
| `DB_PASSWORD` | `postgres` | Contraseña del usuario |

El esquema se genera automáticamente con `spring.jpa.hibernate.ddl-auto: update`.
Es cómodo para desarrollo, pero **no es apto para producción**: en un entorno
real corresponde usar migraciones versionadas (Flyway o Liquibase).

---

## Cómo levantar el proyecto

Desde Git Bash o WSL:

```bash
./mvnw spring-boot:run
```

Desde PowerShell o CMD:

```bash
.\mvnw.cmd spring-boot:run
```

La aplicación queda escuchando en `http://localhost:8080`. Todavía no expone
endpoints REST: la capa de controllers está pendiente.

### Tests

```bash
./mvnw test
```

Los tests de repositorio usan `@DataJpaTest` con una base **H2 embebida**, así
que corren sin necesidad de tener PostgreSQL levantado.

### Empaquetado

```bash
./mvnw package
```

Genera el JAR ejecutable en `target/limitadorum-0.0.1-SNAPSHOT.jar`.

---

## Modelo de datos

```mermaid
classDiagram
    class User {
        Long id
        String username
        String email
        Boolean active
    }
    class Role {
        Long id
        String description
    }
    User "n" -- "m" Role
```

- **`User`** → tabla `users`. `username` es único y obligatorio; `email` es
  obligatorio.
- **`Role`** → tabla `role`. `description` es única y obligatoria.
- La relación **muchos a muchos** se materializa en la tabla intermedia
  `user_roles` (`user_id`, `role_id`). `User` es el lado propietario de la
  relación; `Role` es el lado inverso (`mappedBy = "roles"`).

---

## Estructura del proyecto

Arquitectura clásica de tres capas de Spring Boot:

```
src/main/java/ar/edu/um/limitadorum/
├── LimitadorumApplication.java   # punto de entrada
├── controllers/                  # capa de presentación (pendiente)
├── services/                     # capa de negocio (pendiente)
├── domain/                       # entidades JPA
│   ├── User.java
│   └── Role.java
└── repository/                   # capa de persistencia
    ├── UserRepository.java
    └── RoleRepository.java
```

---

## Estado actual

- [x] Entidades `User` y `Role` con relación muchos a muchos
- [x] Repositorios Spring Data JPA
- [x] Configuración de conexión a PostgreSQL
- [ ] Tests de repositorio
- [ ] Entidad `UserData` (datos personales del usuario)
- [ ] Capa de servicios
- [ ] Endpoints REST
- [ ] Integración continua

---

## Convención de commits

Se sigue [Conventional Commits](https://www.conventionalcommits.org/) con
referencia al issue correspondiente:

```
#<nro-issue> <tipo>(<scope>): <descripción>
```

Ejemplo: `#2 feat(domain): agregar entidades User y Role`

Tipos usados: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`.
