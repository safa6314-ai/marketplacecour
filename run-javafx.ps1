$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

Write-Host "[ARTEVIA DEBUG] Project root: $projectRoot"
Write-Host "[STRIPE DEBUG] STRIPE_SECRET_KEY is null/empty =" ([string]::IsNullOrWhiteSpace($env:STRIPE_SECRET_KEY))
Write-Host "[STRIPE DEBUG] STRIPE_SECRET_KEY starts with sk_test_ =" ($env:STRIPE_SECRET_KEY -like "sk_test_*")
Write-Host "[STRIPE DEBUG] STRIPE_SECRET_KEY starts with pk_test_ =" ($env:STRIPE_SECRET_KEY -like "pk_test_*")
Write-Host "[STRIPE DEBUG] STRIPE_CURRENCY =" $(if ([string]::IsNullOrWhiteSpace($env:STRIPE_CURRENCY)) { "not set, default usd" } else { $env:STRIPE_CURRENCY })

$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if ($mvn -eq $null) {
    $intellijMaven = Get-ChildItem -Path "C:\Program Files\JetBrains" -Recurse -Filter mvn.cmd -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -like "*\plugins\maven\lib\maven3\bin\mvn.cmd" } |
        Select-Object -First 1

    if ($intellijMaven -eq $null) {
        throw "Maven n'est pas disponible dans PATH. Lancez depuis IntelliJ Maven ou installez Maven."
    }

    Write-Host "[MAVEN DEBUG] Using IntelliJ Maven:" $intellijMaven.FullName
    & $intellijMaven.FullName javafx:run
    exit $LASTEXITCODE
}

mvn javafx:run
