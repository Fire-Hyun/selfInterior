$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$statePath = Join-Path $root 'tmp\dev\process-state.json'

function Show-PortStatus {
  param([int]$Port, [string]$Label)

  $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
    Select-Object -First 1

  if ($connection) {
    Write-Host "$Label : LISTEN ($Port)" -ForegroundColor Green
  } else {
    Write-Host "$Label : STOPPED ($Port)" -ForegroundColor Yellow
  }
}

Write-Host 'selfInterior 개발 상태' -ForegroundColor Cyan
Write-Host "root: $root"

if (Test-Path $statePath) {
  Write-Host ''
  Write-Host '관리 중인 프로세스 상태 파일이 있습니다.' -ForegroundColor Green
  Get-Content -Path $statePath -Encoding UTF8
} else {
  Write-Host ''
  Write-Host '관리 중인 프로세스 상태 파일이 없습니다.' -ForegroundColor Yellow
}

Write-Host ''
Show-PortStatus -Port 3000 -Label 'Web'
Show-PortStatus -Port 8080 -Label 'API'
Show-PortStatus -Port 5432 -Label 'Postgres'
Show-PortStatus -Port 6379 -Label 'Redis'

Write-Host ''
try {
  docker compose -f (Join-Path $root 'infra\docker-compose.yml') ps
} catch {
  Write-Host 'Docker 상태를 읽지 못했습니다. Docker Desktop 실행 여부를 확인하세요.' -ForegroundColor Yellow
}
