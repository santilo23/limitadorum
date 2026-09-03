# boot.ps1 - Levanta la aplicacion pasandole la configuracion por variables de
# entorno. Las variables SPRING_* tienen mas prioridad que application.yaml, asi
# que lo que se define aca sobreescribe al perfil activo.
#
# La contrasena NO se guarda en este archivo. Se toma, en este orden:
#   1. El parametro -DbPassword
#   2. La variable DB_PASSWORD del archivo .env (que esta en .gitignore)
#   3. Un prompt interactivo que no muestra lo que se escribe
#
# Uso:
#   .\boot.ps1
#   .\boot.ps1 -DbUrl "jdbc:postgresql://localhost:5433/USERS_AUTH_DEV"
#   .\boot.ps1 -HibernateDdlAuto "update"

param(
    [string]$EnvFile = ".env",
    [string]$DbUrl,
    [string]$DbUsername,
    [string]$DbPassword,
    [string]$DbDriver = "org.postgresql.Driver",
    # create-drop recrea el esquema en cada arranque y lo ELIMINA al apagar la
    # aplicacion. Para que las tablas queden visibles en pgAdmin despues de
    # cerrar, usar "update".
    [string]$HibernateDdlAuto = "create-drop",
    [string]$ShowSql = "true"
)

# --- Cargar el .env local, si existe ---
if (Test-Path $EnvFile) {
    Get-Content $EnvFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and (-not $line.StartsWith("#")) -and $line.Contains("=")) {
            $pair = $line.Split("=", 2)
            Set-Item -Path ("Env:" + $pair[0].Trim()) -Value $pair[1].Trim()
        }
    }
    Write-Host "Configuracion cargada desde $EnvFile"
}

# --- Resolver valores: parametro > .env > default ---
if (-not $DbUrl) {
    if ($env:DB_URL) { $DbUrl = $env:DB_URL }
    else { $DbUrl = "jdbc:postgresql://localhost:5432/USERS_AUTH_DEV" }
}
if (-not $DbUsername) {
    if ($env:DB_USER) { $DbUsername = $env:DB_USER }
    else { $DbUsername = "postgres" }
}
if (-not $DbPassword -and $env:DB_PASSWORD) {
    $DbPassword = $env:DB_PASSWORD
}
if (-not $DbPassword) {
    $secure = Read-Host "Contrasena de PostgreSQL para el usuario '$DbUsername'" -AsSecureString
    $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    $DbPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
    [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
}

Write-Host "Conectando a $DbUrl como '$DbUsername' (ddl-auto: $HibernateDdlAuto)"

$env:SPRING_DATASOURCE_URL = $DbUrl
$env:SPRING_DATASOURCE_USERNAME = $DbUsername
$env:SPRING_DATASOURCE_PASSWORD = $DbPassword
$env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = $DbDriver
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = $HibernateDdlAuto
$env:SPRING_JPA_SHOW_SQL = $ShowSql

.\mvnw.cmd spring-boot:run
