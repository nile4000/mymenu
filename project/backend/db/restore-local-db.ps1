param(
  [string]$DumpFile = "local_dev_public.sql"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$DbContainer = "supabase_db_db"
$DbName = "postgres"
$DbUser = "postgres"
$DumpPath = Join-Path $PSScriptRoot $DumpFile

if (-not (Test-Path $DumpPath)) {
  throw "Dump file not found: $DumpPath"
}

Write-Host "Restarting local Supabase stack ..."
npx supabase stop --no-backup
npx supabase start

Write-Host "Restoring $DumpPath ..."
Get-Content $DumpPath -Raw | docker exec -i $DbContainer psql -U $DbUser -d $DbName

Write-Host "Verifying tables ..."
docker exec $DbContainer psql -U $DbUser -d $DbName -c "select count(*) as article_count from public.article; select count(*) as receipt_count from public.receipt;"

Write-Host "Done."
