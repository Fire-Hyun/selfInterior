$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$tmpDir = Join-Path $root 'tmp\dev'
$statePath = Join-Path $tmpDir 'process-state.json'
$apiLauncherPath = Join-Path $tmpDir 'api-launcher.ps1'
$webLauncherPath = Join-Path $tmpDir 'web-launcher.ps1'

function Ensure-Directory {
  param([string]$Path)

  if (-not (Test-Path $Path)) {
    New-Item -ItemType Directory -Path $Path | Out-Null
  }
}

function Get-JdkHome {
  if ($env:JAVA_HOME -and (Test-Path $env:JAVA_HOME)) {
    return $env:JAVA_HOME
  }

  $bundled = Get-ChildItem -Path (Join-Path $root '.tools') -Directory -Filter 'jdk-*' -ErrorAction SilentlyContinue |
    Sort-Object Name -Descending |
    Select-Object -First 1

  if ($bundled) {
    return $bundled.FullName
  }

  throw "portable JDK를 찾지 못했습니다. '.tools\\jdk-*' 경로를 확인하세요."
}

function Test-DockerEngine {
  try {
    docker version | Out-Null
    return $true
  } catch {
    return $false
  }
}

function Test-LocalPort {
  param([int]$Port)

  $connection = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
    Select-Object -First 1

  return $null -ne $connection
}

function Write-LauncherScript {
  param(
    [string]$Path,
    [string]$Title,
    [string]$Workdir,
    [string]$JdkHome,
    [string]$Command
  )

  $script = @"
\$ErrorActionPreference = 'Stop'
[Console]::Title = '$Title'
\$env:JAVA_HOME = '$JdkHome'
\$env:Path = '$JdkHome\bin;' + \$env:Path
Set-Location '$Workdir'
Write-Host ''
Write-Host '[$Title] 시작' -ForegroundColor Green
Write-Host "workdir: $Workdir"
Write-Host "JAVA_HOME: $JdkHome"
Write-Host ''
$Command
"@

  Set-Content -Path $Path -Value $script -Encoding UTF8
}

function Start-ManagedWindow {
  param([string]$LauncherPath)

  $process = Start-Process powershell `
    -ArgumentList @('-NoExit', '-ExecutionPolicy', 'Bypass', '-File', $LauncherPath) `
    -WorkingDirectory $root `
    -PassThru

  return $process.Id
}

Ensure-Directory $tmpDir

if (-not (Test-DockerEngine)) {
  throw "Docker Desktop 엔진이 실행 중이 아닙니다. Docker Desktop을 켠 뒤 다시 시도하세요."
}

$jdkHome = Get-JdkHome

Write-Host 'Docker 인프라를 올립니다...' -ForegroundColor Cyan
docker compose -f (Join-Path $root 'infra\docker-compose.yml') up -d postgres redis

$state = [ordered]@{
  root = $root
  startedAt = (Get-Date).ToString('s')
  apiPid = $null
  webPid = $null
}

if (-not (Test-LocalPort -Port 8080)) {
  Write-LauncherScript `
    -Path $apiLauncherPath `
    -Title 'selfInterior API' `
    -Workdir (Join-Path $root 'apps\api') `
    -JdkHome $jdkHome `
    -Command '.\gradlew.bat bootRun'

  $state.apiPid = Start-ManagedWindow -LauncherPath $apiLauncherPath
  Write-Host "API 창을 시작했습니다. pid=$($state.apiPid)" -ForegroundColor Green
} else {
  Write-Host 'API(8080)는 이미 실행 중입니다. 새 창을 띄우지 않습니다.' -ForegroundColor Yellow
}

if (-not (Test-LocalPort -Port 3000)) {
  Write-LauncherScript `
    -Path $webLauncherPath `
    -Title 'selfInterior Web' `
    -Workdir (Join-Path $root 'apps\web') `
    -JdkHome $jdkHome `
    -Command 'npm run dev'

  $state.webPid = Start-ManagedWindow -LauncherPath $webLauncherPath
  Write-Host "Web 창을 시작했습니다. pid=$($state.webPid)" -ForegroundColor Green
} else {
  Write-Host 'Web(3000)는 이미 실행 중입니다. 새 창을 띄우지 않습니다.' -ForegroundColor Yellow
}

$state | ConvertTo-Json | Set-Content -Path $statePath -Encoding UTF8

Write-Host ''
Write-Host '실행 준비가 끝났습니다.' -ForegroundColor Cyan
Write-Host '- Web: http://localhost:3000'
Write-Host '- API Health: http://localhost:8080/actuator/health'
Write-Host '- 종료: npm run dev:down'
