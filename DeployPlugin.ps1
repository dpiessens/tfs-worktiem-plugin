$ServiceName = "TeamCity"
$PluginFileName = "tfs-workitems.zip"
$TeamCityInstallRoot = "D:\TeamCity"

Write-Host
Write-Host "Deploying TeamCity Plugin"

Write-Host "Stopping Service: $ServiceName"
Stop-Service -Name $ServiceName -Force

$logDir = Join-Path $TeamCityInstallRoot "logs"
Write-Host "Removing Log Directory: $logDir"
Remove-Item -Path $logDir -Recurse -Force -ErrorAction SilentlyContinue

$pluginPath = Join-Path $PSScriptRoot "target\$PluginFileName"
$teamcityPluginDir = Join-Path $env:TEAMCITY_DATA_PATH "plugins"

$unpackedDir = Join-Path $teamcityPluginDir ".unpacked"
$deployedFile = Join-Path $teamcityPluginDir $PluginFileName
Write-Host "Removing old copy of plugin"
Remove-Item -Path $unpackedDir -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path $deployedFile -Force -ErrorAction SilentlyContinue

Write-Host "Copying Plugin: $PluginFileName"
Write-Host "Destination: $teamcityPluginDir"
Copy-Item -Path $pluginPath -Destination $teamcityPluginDir -Force

Write-Host "Starting Service: $ServiceName"
Start-Service -Name $ServiceName