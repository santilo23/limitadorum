# boot.ps1 - Levanta la aplicacion pasandole la configuracion por variables de
# entorno. Las variables SPRING_* tienen mas prioridad que application.yaml, asi
# que lo que se define aca sobreescribe al perfil activo.
#
# Uso:
#   .\boot.ps1                                  # USERS_AUTH_DEV en pgAdmin (5432)
#   .\boot.ps1 -DbUrl "jdbc:postgresql://localhost:5433/USERS_AUTH_DEV"   # docker
#   .\boot.ps1 -HibernateDdlAuto "update"       # conserva las tablas al salir

param(
    [string]$DbUrl = "jdbc:postgresql://localhost:5432/USERS_AUTH_DEV",
    [string]$DbUsername = "postgres",
    [string]$DbPassword = "postgres",
    [string]$DbDriver = "org.postgresql.Driver",
    # create-drop recrea el esquema en cada arranque y lo ELIMINA al apagar la
    # aplicacion. Para que las tablas queden visibles en pgAdmin despues de
    # cerrar, usar "update".
    [string]$HibernateDdlAuto = "create-drop",
    [string]$ShowSql = "true"
)

$env:SPRING_DATASOURCE_URL = $DbUrl
$env:SPRING_DATASOURCE_USERNAME = $DbUsername
$env:SPRING_DATASOURCE_PASSWORD = $DbPassword
$env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = $DbDriver
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = $HibernateDdlAuto
$env:SPRING_JPA_SHOW_SQL = $ShowSql

.\mvnw.cmd spring-boot:run
