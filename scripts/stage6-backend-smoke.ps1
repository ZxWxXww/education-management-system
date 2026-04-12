param(
  [int]$Port = 8080
)

$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $repoRoot 'backend'
$healthUrl = "http://127.0.0.1:$Port/api/health"
$summaryFile = Join-Path $backendDir 'stage6-smoke-summary.json'
$outFile = Join-Path $backendDir 'stage6-smoke.out'
$errFile = Join-Path $backendDir 'stage6-smoke.err'
$proc = $null
$startedBackend = $false

function Invoke-JsonPost([string]$Url, [hashtable]$Body, [hashtable]$Headers = @{}) {
  Invoke-RestMethod `
    -Uri $Url `
    -Method Post `
    -ContentType 'application/json; charset=utf-8' `
    -Headers $Headers `
    -Body ($Body | ConvertTo-Json -Depth 10)
}

try {
  foreach ($file in @($summaryFile, $outFile, $errFile)) {
    if (Test-Path $file) {
      Remove-Item $file -Force
    }
  }

  $existingListener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
    Select-Object -First 1

  if (-not $existingListener) {
    $proc = Start-Process `
      -FilePath 'mvn.cmd' `
      -ArgumentList 'spring-boot:run', "-Dspring-boot.run.arguments=--server.port=$Port" `
      -WorkingDirectory $backendDir `
      -RedirectStandardOutput $outFile `
      -RedirectStandardError $errFile `
      -PassThru
    $startedBackend = $true
  }

  $ready = $false
  for ($i = 0; $i -lt 90; $i++) {
    Start-Sleep -Seconds 1
    try {
      $health = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec 3
      if ($health.code -eq 0 -and $health.data -eq 'ok') {
        $ready = $true
        break
      }
    } catch {}

    if ($startedBackend -and $proc.HasExited) {
      throw "Spring Boot exited early with code $($proc.ExitCode)"
    }
  }

  if (-not $ready) {
    throw 'Backend did not become ready within 90 seconds'
  }

  $login = Invoke-JsonPost "http://127.0.0.1:$Port/api/auth/login" @{
    username = 'admin'
    password = 'Admin@123456'
    role = 'admin'
  }

  if ($login.code -ne 0 -or -not $login.data.token) {
    throw "Login failed: $($login.message)"
  }

  $headers = @{ Authorization = "Bearer $($login.data.token)" }
  $orders = Invoke-JsonPost "http://127.0.0.1:$Port/api/admin/orders/page" @{ pageNum = 1; pageSize = 5 } $headers
  $bills = Invoke-JsonPost "http://127.0.0.1:$Port/api/admin/bills/page" @{ pageNum = 1; pageSize = 5 } $headers
  $payments = Invoke-RestMethod -Uri "http://127.0.0.1:$Port/api/admin/bills/6/payments" -Method Get -Headers $headers

  if ($orders.code -ne 0) {
    throw 'Order page smoke failed'
  }
  if ($bills.code -ne 0) {
    throw 'Bill page smoke failed'
  }
  if ($payments.code -ne 0) {
    throw 'Bill payment smoke failed'
  }
  if (-not (@($login.data.permissions | Where-Object { $_.permCode -eq 'finance:order:manage' }).Count)) {
    throw 'Missing finance:order:manage permission'
  }
  if (-not (@($login.data.permissions | Where-Object { $_.permCode -eq 'finance:bill:manage' }).Count)) {
    throw 'Missing finance:bill:manage permission'
  }

  [pscustomobject]@{
    healthCode = $health.code
    loginCode = $login.code
    activeRole = $login.data.activeRole
    permissionCount = @($login.data.permissions).Count
    orderFirst = if (@($orders.data.records).Count -gt 0) {
      "$($orders.data.records[0].orderNo):$($orders.data.records[0].payStatus):$($orders.data.records[0].billStatus)"
    } else {
      $null
    }
    billFirst = if (@($bills.data.records).Count -gt 0) {
      "$($bills.data.records[0].billNo):$($bills.data.records[0].orderId):$($bills.data.records[0].status):$($bills.data.records[0].paidAmount)"
    } else {
      $null
    }
    bill6PaymentsCount = @($payments.data).Count
    bill6LastPayNo = if (@($payments.data).Count -gt 0) {
      $payments.data[-1].payNo
    } else {
      $null
    }
  } | ConvertTo-Json -Depth 6 | Set-Content -Path $summaryFile -Encoding UTF8

  Get-Content $summaryFile
} finally {
  if ($startedBackend -and $proc -and -not $proc.HasExited) {
    Stop-Process -Id $proc.Id -Force
  }
}
