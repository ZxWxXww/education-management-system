param(
    [string]$DbHost = "localhost",
    [int]$Port = 3306,
    [string]$Database = "edu",
    [string]$User = "",
    [string]$Password = "",
    [string]$LoginPath = "",
    [string]$MysqlPath = "mysql"
)

$hasLoginPath = -not [string]::IsNullOrWhiteSpace($LoginPath)
$effectiveUser = if (-not [string]::IsNullOrWhiteSpace($User)) { $User } else { $env:EDU_DB_USER }
$effectivePassword = if (-not [string]::IsNullOrWhiteSpace($Password)) { $Password } else { $env:EDU_DB_PASSWORD }
$hasUser = -not [string]::IsNullOrWhiteSpace($effectiveUser)
$hasPassword = -not [string]::IsNullOrWhiteSpace($effectivePassword)

if (-not $hasLoginPath -and (-not $hasUser -or -not $hasPassword)) {
    Write-Host "Database check failed: missing credentials." -ForegroundColor Red
    Write-Host "Use one of the following methods:" -ForegroundColor Yellow
    Write-Host "1) Username and password:" -ForegroundColor Yellow
    Write-Host "   .\check-db-connection.ps1 -DbHost localhost -Port 3306 -Database edu -User YOUR_DB_USER_HERE -Password YOUR_DB_PASSWORD_HERE"
    Write-Host "   or set env: EDU_DB_USER, EDU_DB_PASSWORD"
    Write-Host "2) mysql_config_editor login-path:" -ForegroundColor Yellow
    Write-Host "   .\check-db-connection.ps1 -Database edu -LoginPath YOUR_LOGIN_PATH_HERE"
    exit 1
}

$mysqlArgs = @("--host=$DbHost", "--port=$Port", "--database=$Database", "--batch", "--raw")

if ($hasLoginPath) {
    $mysqlArgs += "--login-path=$LoginPath"
} else {
    $mysqlArgs += "--user=$effectiveUser"
    $env:MYSQL_PWD = $effectivePassword
}

$output = & $MysqlPath @mysqlArgs -e "SHOW TABLES;" 2>&1
$exitCode = $LASTEXITCODE

if (-not $hasLoginPath) {
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}

if ($exitCode -ne 0) {
    Write-Host "Database connection failed." -ForegroundColor Red
    Write-Host "$output"
    exit $exitCode
}

Write-Host "Database connection successful: $Database" -ForegroundColor Green
Write-Host "Current tables:"
$output
exit 0
