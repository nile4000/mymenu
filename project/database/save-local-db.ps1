param(
  [string]$OutputFile = "local_dev_public.sql"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$DbContainer = "supabase_db_db"
$DbName = "postgres"
$DbUser = "postgres"
$TargetPath = Join-Path $PSScriptRoot $OutputFile

Write-Host "Saving public schema to $TargetPath ..."

$dump = docker exec $DbContainer pg_dump -U $DbUser -d $DbName --schema=public --no-owner --no-privileges
$dump | Out-File -FilePath $TargetPath -Encoding utf8

Write-Host "Done."
