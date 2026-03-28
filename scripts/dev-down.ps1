$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$statePath = Join-Path $root 'tmp\dev\process-state.json'

function Stop-ManagedProcess {
  param([Nullable[int]]$Pid, [string]$Name)

  if (-not $Pid) {
    return
  }

  $process = Get-Process -Id $Pid -ErrorAction SilentlyContinue
  if ($process) {
    Stop-Process -Id $Pid -Force
    Write-Host "$Name 프로세스를 종료했습니다. pid=$Pid" -ForegroundColor Green
  }
}

if (Test-Path $statePath) {
  $state = Get-Content -Path $statePath -Encoding UTF8 | ConvertFrom-Json
  Stop-ManagedProcess -Pid $state.apiPid -Name 'API'
  Stop-ManagedProcess -Pid $state.webPid -Name 'Web'
  Remove-Item $statePath -Force
} else {
  Write-Host '저장된 dev 상태 파일이 없습니다. 포트가 살아 있으면 수동 실행분일 수 있습니다.' -ForegroundColor Yellow
}

docker compose -f (Join-Path $root 'infra\docker-compose.yml') down
Write-Host 'Docker 인프라를 내렸습니다.' -ForegroundColor Cyan
