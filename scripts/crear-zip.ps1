<#
.SYNOPSIS
  Genera el zip de entrega del proyecto sin los directorios y ficheros innecesarios.
  Ejecutar con PowerShell 7 (pwsh): Windows PowerShell 5 crea zips con rutas con barra invertida.
.PARAMETER Output
  Ruta del zip a crear. Por defecto, tech-watch.zip en el Escritorio.
#>
param(
    [string]$Output = (Join-Path ([Environment]::GetFolderPath('Desktop')) 'tech-watch.zip')
)

$root = Split-Path $PSScriptRoot -Parent
$staging = Join-Path ([IO.Path]::GetTempPath()) ("tech-watch-" + [guid]::NewGuid())

# .gradle, .idea, build, .kotlin, .claude y .git sobran; local.properties, google-services.json y secretos.txt son secretos y se entregan aparte.
robocopy $root $staging /E /XD .gradle .idea build .kotlin .claude .git /XF local.properties google-services.json finnhub.properties secretos.txt *.iml | Out-Null
if ($LASTEXITCODE -ge 8) { throw "robocopy fallo con codigo $LASTEXITCODE" }

if (Test-Path $Output) { Remove-Item $Output }
Compress-Archive -Path (Join-Path $staging '*') -DestinationPath $Output
Remove-Item $staging -Recurse -Force

"Creado: $Output ({0:N1} MB)" -f ((Get-Item $Output).Length / 1MB)
