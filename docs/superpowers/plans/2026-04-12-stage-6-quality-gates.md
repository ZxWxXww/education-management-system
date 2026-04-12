# Stage 6 Quality Gates Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the minimum reusable quality gates for EduSmart Manager so frontend build/tests, backend tests, and key finance-chain smoke checks can be run with one repeatable workflow.

**Architecture:** Keep the existing frontend and backend test layout. Add one backend smoke test script at repo level, wire frontend test scripts into `package.json`, and add a root verification script that orchestrates the full Stage 6 gate without introducing new infrastructure.

**Tech Stack:** PowerShell, Node.js test runner, Vite, Maven/Spring Boot, existing backend APIs

---

### Task 1: Wire frontend regression gates into package scripts

**Files:**
- Modify: `D:\education\frontend\package.json`

- [ ] **Step 1: Write the failing test expectation**

```text
We need reusable scripts so Stage 4/5 regression tests can be run without remembering raw node commands.
Expected new scripts:
- test:contracts
- verify
```

- [ ] **Step 2: Run the current package script surface and verify the gap**

Run: `npm run`
Expected: no `test:contracts` or `verify` script exists.

- [ ] **Step 3: Add the minimal package scripts**

```json
{
  "scripts": {
    "dev": "vite",
    "nav:check": "node scripts/check-nav-freeze.mjs",
    "test:contracts": "node --test tests/stage4-real-api-contract.test.mjs tests/stage5-finance-enum.test.mjs",
    "build": "vite build",
    "verify": "npm run test:contracts && npm run build",
    "preview": "vite preview"
  }
}
```

- [ ] **Step 4: Run the new frontend gate**

Run: `npm run verify`
Expected: node contract tests pass, then Vite build succeeds.


### Task 2: Add a reusable backend finance smoke script

**Files:**
- Create: `D:\education\scripts\stage6-backend-smoke.ps1`

- [ ] **Step 1: Write the failing smoke requirement**

```text
We need a repeatable smoke script that:
- reuses port 8080 when a backend is already running, otherwise starts one on 8080
- logs in with admin / Admin@123456 / admin
- checks finance order/bill permissions
- checks /api/admin/orders/page
- checks /api/admin/bills/page
- checks /api/admin/bills/6/payments
- exits non-zero on failure
```

- [ ] **Step 2: Verify the script does not exist yet**

Run: `Test-Path D:\education\scripts\stage6-backend-smoke.ps1`
Expected: `False`

- [ ] **Step 3: Add the minimal smoke script**

```powershell
param(
[int]$Port = 8080
)

$ErrorActionPreference = 'Stop'
$healthUrl = "http://127.0.0.1:$Port/api/health"
$summaryFile = Join-Path $PSScriptRoot '..\backend\stage6-smoke-summary.json'
$outFile = Join-Path $PSScriptRoot '..\backend\stage6-smoke.out'
$errFile = Join-Path $PSScriptRoot '..\backend\stage6-smoke.err'
$proc = $null

function Invoke-JsonPost([string]$Url, [hashtable]$Body, [hashtable]$Headers = @{}) {
  Invoke-RestMethod -Uri $Url -Method Post -ContentType 'application/json; charset=utf-8' -Headers $Headers -Body ($Body | ConvertTo-Json -Depth 10)
}

try {
  foreach ($file in @($summaryFile, $outFile, $errFile)) {
    if (Test-Path $file) {
      Remove-Item $file -Force
    }
  }

  $proc = Start-Process -FilePath 'mvn.cmd' `
    -ArgumentList 'spring-boot:run', "-Dspring-boot.run.arguments=--server.port=$Port" `
    -WorkingDirectory (Join-Path $PSScriptRoot '..\backend') `
    -RedirectStandardOutput $outFile `
    -RedirectStandardError $errFile `
    -PassThru

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
    if ($proc.HasExited) {
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

  if ($orders.code -ne 0) { throw 'Order page smoke failed' }
  if ($bills.code -ne 0) { throw 'Bill page smoke failed' }
  if ($payments.code -ne 0) { throw 'Bill payment smoke failed' }
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
    orderFirst = if (@($orders.data.records).Count -gt 0) { $orders.data.records[0].orderNo } else { $null }
    billFirst = if (@($bills.data.records).Count -gt 0) { "$($bills.data.records[0].billNo):$($bills.data.records[0].orderId)" } else { $null }
    bill6PaymentsCount = @($payments.data).Count
  } | ConvertTo-Json -Depth 6 | Set-Content -Path $summaryFile -Encoding UTF8

  Get-Content $summaryFile
} finally {
  if ($proc -and -not $proc.HasExited) {
    Stop-Process -Id $proc.Id -Force
  }
}
```

- [ ] **Step 4: Run the smoke script**

Run: `powershell -ExecutionPolicy Bypass -File D:\education\scripts\stage6-backend-smoke.ps1`
Expected: JSON summary printed with `loginCode: 0`, `activeRole: admin`, and non-empty finance results.


### Task 3: Add a root Stage 6 verification gate

**Files:**
- Create: `D:\education\scripts\stage6-verify.ps1`

- [ ] **Step 1: Write the failing gate requirement**

```text
We need one command at repo root that runs:
- frontend verify
- backend mvn test
- backend smoke script
and fails fast if any step fails.
```

- [ ] **Step 2: Verify the root script does not exist yet**

Run: `Test-Path D:\education\scripts\stage6-verify.ps1`
Expected: `False`

- [ ] **Step 3: Add the orchestration script**

```powershell
$ErrorActionPreference = 'Stop'

Push-Location 'D:\education\frontend'
try {
  npm run verify
} finally {
  Pop-Location
}

Push-Location 'D:\education\backend'
try {
  mvn test
} finally {
  Pop-Location
}

powershell -ExecutionPolicy Bypass -File 'D:\education\scripts\stage6-backend-smoke.ps1'
```

- [ ] **Step 4: Run the full Stage 6 gate**

Run: `powershell -ExecutionPolicy Bypass -File D:\education\scripts\stage6-verify.ps1`
Expected: frontend verify passes, backend tests pass, smoke summary prints success JSON.
