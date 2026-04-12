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
