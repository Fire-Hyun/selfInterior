$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$statePath = Join-Path $root 'tmp\dev\process-state.json'

function Stop-ManagedProcess {
  param([Nullable[int]]$ProcessId, [string]$Name)

  if (-not $ProcessId) {
    return
  }

  $process = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
  if ($process) {
    Stop-Process -Id $ProcessId -Force
    Write-Host "$Name 프로세스를 종료했습니다. pid=$ProcessId" -ForegroundColor Green
  }
}

if (Test-Path $statePath) {
  $state = Get-Content -Path $statePath -Encoding UTF8 | ConvertFrom-Json
  Stop-ManagedProcess -ProcessId $state.apiPid -Name 'API'
  Stop-ManagedProcess -ProcessId $state.webPid -Name 'Web'
  Remove-Item $statePath -Force
} else {
  Write-Host '관리 중인 dev 상태 파일이 없습니다. 수동 실행 중인 프로세스가 있을 수 있습니다.' -ForegroundColor Yellow
}

docker compose -f (Join-Path $root 'infra\docker-compose.yml') down
Write-Host 'Docker 인프라를 내렸습니다.' -ForegroundColor Cyan
