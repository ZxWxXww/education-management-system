param(
    [string]$DbHost = "localhost",
    [int]$Port = 3306,
    [string]$Database = "edu",
    [string]$User = "",
    [string]$Password = "",
    [string]$LoginPath = "",
    [string]$MysqlPath = "mysql",
    [string]$SqlFile = ".\edu-db.session.sql"
)

$hasLoginPath = -not [string]::IsNullOrWhiteSpace($LoginPath)
$effectiveUser = if (-not [string]::IsNullOrWhiteSpace($User)) { $User } else { $env:EDU_DB_USER }
$effectivePassword = if (-not [string]::IsNullOrWhiteSpace($Password)) { $Password } else { $env:EDU_DB_PASSWORD }
$hasUser = -not [string]::IsNullOrWhiteSpace($effectiveUser)
$hasPassword = -not [string]::IsNullOrWhiteSpace($effectivePassword)

if (-not (Test-Path -Path $SqlFile -PathType Leaf)) {
    Write-Host "DDL execute failed: SQL file not found: $SqlFile" -ForegroundColor Red
    Write-Host "Please provide a valid SQL file path, for example:" -ForegroundColor Yellow
    Write-Host ".\run-edu-ddl.ps1 -SqlFile .\edu-db.session.sql"
    exit 1
}

$sqlContent = Get-Content -Path $SqlFile -Raw
if ([string]::IsNullOrWhiteSpace($sqlContent)) {
    Write-Host "DDL execute failed: SQL file is empty: $SqlFile" -ForegroundColor Red
    Write-Host "Please paste CREATE TABLE statements into the SQL file first."
    exit 1
}

if (-not $hasLoginPath -and (-not $hasUser -or -not $hasPassword)) {
    Write-Host "DDL execute failed: missing credentials." -ForegroundColor Red
    Write-Host "Use one of the following methods:" -ForegroundColor Yellow
    Write-Host "1) Username and password:"
    Write-Host "   .\run-edu-ddl.ps1 -DbHost localhost -Port 3306 -Database edu -User YOUR_DB_USER_HERE -Password YOUR_DB_PASSWORD_HERE -SqlFile .\edu-db.session.sql"
    Write-Host "   or set env: EDU_DB_USER, EDU_DB_PASSWORD"
    Write-Host "2) mysql_config_editor login-path:"
    Write-Host "   .\run-edu-ddl.ps1 -Database edu -LoginPath YOUR_LOGIN_PATH_HERE -SqlFile .\edu-db.session.sql"
    exit 1
}

$mysqlArgs = @("--host=$DbHost", "--port=$Port", "--default-character-set=utf8mb4", "--database=$Database")

if ($hasLoginPath) {
    $mysqlArgs += "--login-path=$LoginPath"
} else {
    $mysqlArgs += "--user=$effectiveUser"
    $env:MYSQL_PWD = $effectivePassword
}

Write-Host "Running DDL script..." -ForegroundColor Cyan
Write-Host "Target: $DbHost`:$Port / $Database"
Write-Host "SQL: $SqlFile"

$output = & $MysqlPath @mysqlArgs --execute="$sqlContent" 2>&1
$exitCode = $LASTEXITCODE

if (-not $hasLoginPath) {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}

if ($exitCode -ne 0) {
    Write-Host "DDL execute failed." -ForegroundColor Red
    Write-Host "$output"
    exit $exitCode
}

Write-Host "DDL execute success." -ForegroundColor Green
Write-Host "$output"
exit 0
