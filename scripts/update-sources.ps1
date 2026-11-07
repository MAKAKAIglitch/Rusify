#requires -Version 5.1
[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$ProjectRoot = [IO.Path]::GetFullPath((Split-Path -Parent $PSScriptRoot))
$Repository = "MAKAKAIglitch/Rusify"
$Branch = "main"
$ArchiveUrl = "https://codeload.github.com/$Repository/zip/refs/heads/$Branch"
$ManifestName = ".rusify-update-manifest.txt"
$ManifestPath = Join-Path $ProjectRoot $ManifestName
$BackupRoot = Join-Path $ProjectRoot ".rusify-backups"
$TempRoot = Join-Path ([IO.Path]::GetTempPath()) ("RusifyUpdater-" + [guid]::NewGuid().ToString("N"))
$StageRoot = Join-Path $TempRoot "stage"
$ZipPath = Join-Path $TempRoot "rusify-main.zip"
$Utf8NoBom = New-Object System.Text.UTF8Encoding($false)

# These folders/files belong to the game, build output, or the user.
# They are deliberately never touched by the source updater.
$ProtectedTopLevel = @(
    ".git", ".tools", ".gradle", ".idea", ".vscode",
    ".rusify-backups", "build", "run", "mods", "config",
    "saves", "logs", "crash-reports", "screenshots",
    "resourcepacks", "shaderpacks"
)
$ProtectedFiles = @("options.txt", "servers.dat")

function Test-ProtectedPath {
    param([Parameter(Mandatory = $true)][string] $RelativePath)

    $normalized = $RelativePath.Replace("\", "/").TrimStart("/")
    $first = ($normalized -split "/", 2)[0]
    if ($ProtectedTopLevel -contains $first) { return $true }
    if ($ProtectedFiles -contains $normalized) { return $true }
    if ($normalized -eq $ManifestName) { return $true }
    return $false
}

function Get-Sha256 {
    param([Parameter(Mandatory = $true)][string] $Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash
}

function Get-SafeDestination {
    param(
        [Parameter(Mandatory = $true)][string] $Root,
        [Parameter(Mandatory = $true)][string] $RelativePath
    )

    $relative = $RelativePath.Replace("/", "\")
    if ([IO.Path]::IsPathRooted($relative) -or $relative -match '(^|\\)\.\.(\\|$)' -or $relative.Contains(":")) {
        throw "Unsafe path received from the GitHub archive: $RelativePath"
    }

    $rootFull = [IO.Path]::GetFullPath($Root).TrimEnd("\") + "\"
    $destination = [IO.Path]::GetFullPath((Join-Path $Root $relative))
    if (!$destination.StartsWith($rootFull, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Archive path escapes the destination folder: $RelativePath"
    }
    return $destination
}

function Expand-VerifiedArchive {
    param(
        [Parameter(Mandatory = $true)][string] $Archive,
        [Parameter(Mandatory = $true)][string] $Destination
    )

    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem

    [IO.Directory]::CreateDirectory($Destination) | Out-Null
    $zip = [IO.Compression.ZipFile]::OpenRead($Archive)
    try {
        foreach ($entry in $zip.Entries) {
            $name = $entry.FullName.Replace("\", "/")
            if ([string]::IsNullOrWhiteSpace($name) -or $name.EndsWith("/")) { continue }

            $parts = $name.Split("/")
            if ($parts.Count -lt 2) { continue }
            $relative = ($parts | Select-Object -Skip 1) -join "/"
            if ([string]::IsNullOrWhiteSpace($relative) -or (Test-ProtectedPath -RelativePath $relative)) { continue }

            $target = Get-SafeDestination -Root $Destination -RelativePath $relative
            $parent = Split-Path -Parent $target
            [IO.Directory]::CreateDirectory($parent) | Out-Null

            $inputStream = $entry.Open()
            try {
                $outputStream = [IO.File]::Create($target)
                try { $inputStream.CopyTo($outputStream) }
                finally { $outputStream.Dispose() }
            }
            finally { $inputStream.Dispose() }
        }
    }
    finally { $zip.Dispose() }
}

try {
    Write-Host ""
    Write-Host " Rusify source updater" -ForegroundColor White
    Write-Host " Repository: $Repository | Branch: $Branch" -ForegroundColor DarkGray
    Write-Host " Mode: direct download from GitHub (Git is not required)" -ForegroundColor Cyan
    Write-Host ""

    if (!(Test-Path -LiteralPath (Join-Path $ProjectRoot "build.bat"))) {
        throw "This does not look like the Rusify project folder: build.bat was not found. Keep update-sources.bat in the Rusify root folder."
    }

    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    [IO.Directory]::CreateDirectory($TempRoot) | Out-Null

    Write-Host "[1/4] Downloading the latest main branch from GitHub..." -ForegroundColor Cyan
    Invoke-WebRequest -Uri $ArchiveUrl -OutFile $ZipPath -UseBasicParsing -Headers @{
        "User-Agent" = "Rusify-Source-Updater"
        "Accept" = "application/vnd.github+json"
    }

    if (!(Test-Path -LiteralPath $ZipPath) -or (Get-Item -LiteralPath $ZipPath).Length -lt 1024) {
        throw "GitHub returned an empty or unexpectedly small archive."
    }

    Write-Host "[2/4] Checking and extracting the archive..." -ForegroundColor Cyan
    Expand-VerifiedArchive -Archive $ZipPath -Destination $StageRoot

    $required = @("build.bat", "gradlew", "settings.gradle")
    foreach ($file in $required) {
        if (!(Test-Path -LiteralPath (Join-Path $StageRoot $file) -PathType Leaf)) {
            throw "The downloaded archive is incomplete: required file '$file' is missing. Existing sources were not changed."
        }
    }

    $newFiles = @(Get-ChildItem -LiteralPath $StageRoot -File -Recurse | ForEach-Object {
        $_.FullName.Substring($StageRoot.Length).TrimStart("\").Replace("\", "/")
    } | Sort-Object -Unique)

    if ($newFiles.Count -lt 5) {
        throw "The downloaded source archive contains too few files. Existing sources were not changed."
    }

    $oldFiles = @()
    if (Test-Path -LiteralPath $ManifestPath -PathType Leaf) {
        $oldFiles = @(Get-Content -LiteralPath $ManifestPath | Where-Object {
            $_ -and $_ -notmatch '(^/|(^|/)\.\.(/|$)|:)' -and !(Test-ProtectedPath -RelativePath $_)
        })
    }

    $stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $backupDirectory = Join-Path $BackupRoot $stamp
    $changed = 0
    $unchanged = 0
    $removed = 0
    $backedUp = 0

    Write-Host "[3/4] Updating source files..." -ForegroundColor Cyan
    foreach ($relative in $newFiles) {
        $source = Get-SafeDestination -Root $StageRoot -RelativePath $relative
        $target = Get-SafeDestination -Root $ProjectRoot -RelativePath $relative
        $targetParent = Split-Path -Parent $target
        [IO.Directory]::CreateDirectory($targetParent) | Out-Null

        if (Test-Path -LiteralPath $target -PathType Leaf) {
            if ((Get-Sha256 -Path $source) -eq (Get-Sha256 -Path $target)) {
                $unchanged++
                continue
            }

            $backup = Get-SafeDestination -Root $backupDirectory -RelativePath $relative
            [IO.Directory]::CreateDirectory((Split-Path -Parent $backup)) | Out-Null
            Copy-Item -LiteralPath $target -Destination $backup -Force
            $backedUp++
        }

        $temporaryTarget = "$target.rusify-update-tmp"
        Copy-Item -LiteralPath $source -Destination $temporaryTarget -Force
        Move-Item -LiteralPath $temporaryTarget -Destination $target -Force
        $changed++
    }

    # Remove only files listed by a previous successful Rusify update.
    # This cleans up obsolete project files without scanning/deleting user data.
    $newSet = @{}
    foreach ($relative in $newFiles) { $newSet[$relative.ToLowerInvariant()] = $true }

    foreach ($relative in $oldFiles) {
        if ($newSet.ContainsKey($relative.ToLowerInvariant())) { continue }
        $stale = Get-SafeDestination -Root $ProjectRoot -RelativePath $relative
        if (Test-Path -LiteralPath $stale -PathType Leaf) {
            $backup = Get-SafeDestination -Root $backupDirectory -RelativePath $relative
            [IO.Directory]::CreateDirectory((Split-Path -Parent $backup)) | Out-Null
            Copy-Item -LiteralPath $stale -Destination $backup -Force
            Remove-Item -LiteralPath $stale -Force
            $removed++
            $backedUp++
        }
    }

    $manifestTemp = "$ManifestPath.tmp"
    [IO.File]::WriteAllLines($manifestTemp, $newFiles, $Utf8NoBom)
    Move-Item -LiteralPath $manifestTemp -Destination $ManifestPath -Force

    Write-Host "[4/4] Finished." -ForegroundColor Cyan
    Write-Host ""
    Write-Host " ==========================================" -ForegroundColor Green
    Write-Host " RUSIFY SOURCES UPDATED SUCCESSFULLY" -ForegroundColor Green
    Write-Host " ==========================================" -ForegroundColor Green
    Write-Host "Updated files: $changed" -ForegroundColor Green
    Write-Host "Already current: $unchanged" -ForegroundColor Gray
    Write-Host "Obsolete files removed: $removed" -ForegroundColor Gray
    Write-Host "Backup copies: $backedUp" -ForegroundColor Gray
    if ($backedUp -gt 0) {
        Write-Host "Backup folder: .rusify-backups\$stamp" -ForegroundColor Yellow
    }
    Write-Host ""
    Write-Host "Git is not needed. You can now run build.bat to compile the updated mod." -ForegroundColor White
    Write-Host ""
    exit 0
}
catch {
    Write-Host ""
    Write-Host "[UPDATE ERROR] $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "The updater keeps user folders such as mods, config, saves, and build untouched." -ForegroundColor DarkGray
    Write-Host "Any source files already replaced are backed up in .rusify-backups when possible." -ForegroundColor DarkGray
    Write-Host ""
    exit 1
}
finally {
    if (Test-Path -LiteralPath $TempRoot) {
        Remove-Item -LiteralPath $TempRoot -Recurse -Force -ErrorAction SilentlyContinue
    }
}
