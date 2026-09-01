[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectRoot = Split-Path -Parent $PSScriptRoot
$releaseDirectory = Join-Path $projectRoot 'release'
$keystorePath = Join-Path $releaseDirectory 'roomdeck-production.jks'
$artifactDirectory = Join-Path $projectRoot 'artifacts\public\v1.0.1'
$keyAlias = 'roomdeck'
$expectedVersion = '1.0.1'

function ConvertFrom-RoomDeckSecureString {
    param([Parameter(Mandatory)][Security.SecureString]$SecureValue)

    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecureValue)
    try {
        [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    }
}

function Assert-RoomDeckCommand {
    param([Parameter(Mandatory)][string]$Description)

    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE."
    }
}

$androidStudioJbr = 'C:\Program Files\Android\Android Studio\jbr'
if (-not $env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $androidStudioJbr 'bin\java.exe'))) {
    $env:JAVA_HOME = $androidStudioJbr
}
if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    throw 'A JDK was not found. Install Android Studio or set JAVA_HOME to a JDK 17+ installation.'
}

$androidSdk = if ($env:ANDROID_SDK_ROOT) {
    $env:ANDROID_SDK_ROOT
} elseif ($env:ANDROID_HOME) {
    $env:ANDROID_HOME
} else {
    Join-Path $env:LOCALAPPDATA 'Android\Sdk'
}
if (-not (Test-Path -LiteralPath $androidSdk)) {
    throw 'The Android SDK was not found. Install it through Android Studio.'
}

$buildToolsDirectory = Get-ChildItem -LiteralPath (Join-Path $androidSdk 'build-tools') |
    Where-Object { $_.PSIsContainer } |
    Sort-Object { [version]$_.Name } -Descending |
    Select-Object -First 1
if (-not $buildToolsDirectory) {
    throw 'Android SDK Build Tools were not found.'
}

$apkSigner = Join-Path $buildToolsDirectory.FullName 'apksigner.bat'
$zipAlign = Join-Path $buildToolsDirectory.FullName 'zipalign.exe'
$apkAnalyzer = Get-ChildItem -LiteralPath $androidSdk -Recurse -Filter 'apkanalyzer.bat' |
    Select-Object -First 1 -ExpandProperty FullName
$keyTool = Join-Path $env:JAVA_HOME 'bin\keytool.exe'

foreach ($requiredTool in @($apkSigner, $zipAlign, $apkAnalyzer, $keyTool)) {
    if (-not (Test-Path -LiteralPath $requiredTool)) {
        throw "Required release tool was not found: $requiredTool"
    }
}

New-Item -ItemType Directory -Path $releaseDirectory -Force | Out-Null
New-Item -ItemType Directory -Path $artifactDirectory -Force | Out-Null

$plainPassword = $null
while ($true) {
    $securePassword = Read-Host 'Enter the permanent RoomDeck signing password (at least 16 characters)' -AsSecureString
    $plainPassword = ConvertFrom-RoomDeckSecureString -SecureValue $securePassword
    if ($plainPassword.Length -lt 16) {
        Write-Warning 'That password was too short. Please enter at least 16 characters.'
        $plainPassword = $null
        continue
    }

    if (-not (Test-Path -LiteralPath $keystorePath)) {
        $secureConfirmation = Read-Host 'Enter the same password again' -AsSecureString
        $plainConfirmation = ConvertFrom-RoomDeckSecureString -SecureValue $secureConfirmation
        if ($plainPassword -cne $plainConfirmation) {
            Write-Warning 'The two passwords did not match. Please try again.'
            $plainPassword = $null
            $plainConfirmation = $null
            continue
        }
        $plainConfirmation = $null
    }
    break
}

if (-not (Test-Path -LiteralPath $keystorePath)) {
    $env:ROOMDECK_STORE_PASSWORD = $plainPassword
    $env:ROOMDECK_KEY_PASSWORD = $plainPassword
    & $keyTool -genkeypair -v `
        -keystore $keystorePath `
        -storetype PKCS12 `
        -storepass:env ROOMDECK_STORE_PASSWORD `
        -keypass:env ROOMDECK_KEY_PASSWORD `
        -alias $keyAlias `
        -keyalg RSA `
        -keysize 4096 `
        -sigalg SHA256withRSA `
        -validity 10000 `
        -dname 'CN=RoomDeck, O=tatselkrik, C=PH'
    Assert-RoomDeckCommand 'Production signing-key creation'
    Write-Host 'Created the permanent RoomDeck production key.'
}

try {
    $env:ROOMDECK_STORE_FILE = $keystorePath
    $env:ROOMDECK_STORE_PASSWORD = $plainPassword
    $env:ROOMDECK_KEY_ALIAS = $keyAlias
    $env:ROOMDECK_KEY_PASSWORD = $plainPassword
    $env:GRADLE_USER_HOME = Join-Path $projectRoot '.gradle-home'
    $env:ANDROID_PREFS_ROOT = Join-Path $projectRoot '.android'
    New-Item -ItemType Directory -Path $env:ANDROID_PREFS_ROOT -Force | Out-Null

    Push-Location $projectRoot
    try {
        & .\gradlew.bat testDebugUnitTest lintDebug lintRelease assembleRelease `
            --no-daemon '-Pkotlin.compiler.execution.strategy=in-process'
        Assert-RoomDeckCommand 'Production release build'
    } finally {
        Pop-Location
    }

    $packages = @(
        @{
            Source = Join-Path $projectRoot 'mobile\build\outputs\apk\release\mobile-release.apk'
            Destination = Join-Path $artifactDirectory 'RoomDeck-Controller-v1.0.1.apk'
            ApplicationId = 'io.github.tatselkrik.roomdeck'
            VersionCode = '20'
        },
        @{
            Source = Join-Path $projectRoot 'receiver\build\outputs\apk\release\receiver-release.apk'
            Destination = Join-Path $artifactDirectory 'RoomDeck-Receiver-v1.0.1.apk'
            ApplicationId = 'io.github.tatselkrik.roomdeck.receiver'
            VersionCode = '12'
        }
    )

    $certificateDigests = @()
    foreach ($package in $packages) {
        if (-not (Test-Path -LiteralPath $package.Source)) {
            throw "Expected signed APK was not created: $($package.Source)"
        }
        Copy-Item -LiteralPath $package.Source -Destination $package.Destination -Force

        & $apkSigner verify --verbose --print-certs $package.Destination
        Assert-RoomDeckCommand "Signature verification for $($package.Destination)"
        $certificateOutput = & $apkSigner verify --print-certs $package.Destination
        Assert-RoomDeckCommand "Certificate inspection for $($package.Destination)"
        $digestLine = $certificateOutput | Where-Object { $_ -match 'certificate SHA-256 digest:' } | Select-Object -First 1
        if (-not $digestLine) {
            throw "No signing-certificate digest was reported for $($package.Destination)."
        }
        $certificateDigests += ($digestLine -replace '^.*digest:\s*', '').Trim()

        & $zipAlign -c -P 16 -v 4 $package.Destination | Out-Host
        Assert-RoomDeckCommand "Zip alignment verification for $($package.Destination)"

        $applicationId = (& $apkAnalyzer manifest application-id $package.Destination).Trim()
        $versionName = (& $apkAnalyzer manifest version-name $package.Destination).Trim()
        $versionCode = (& $apkAnalyzer manifest version-code $package.Destination).Trim()
        $debuggable = (& $apkAnalyzer manifest debuggable $package.Destination).Trim()
        if ($applicationId -cne $package.ApplicationId -or
            $versionName -cne $expectedVersion -or
            $versionCode -cne $package.VersionCode -or
            $debuggable -cne 'false') {
            throw "APK metadata verification failed for $($package.Destination)."
        }
    }

    if (@($certificateDigests | Sort-Object -Unique).Count -ne 1) {
        throw 'Controller and Receiver were not signed with the same production certificate.'
    }

    $checksumPath = Join-Path $artifactDirectory 'SHA256SUMS-v1.0.1.txt'
    $checksumLines = $packages | ForEach-Object {
        $hash = Get-FileHash -LiteralPath $_.Destination -Algorithm SHA256
        '{0}  {1}' -f $hash.Hash.ToLowerInvariant(), (Split-Path -Leaf $_.Destination)
    }
    [IO.File]::WriteAllLines($checksumPath, $checksumLines, [Text.UTF8Encoding]::new($false))

    Write-Host ''
    Write-Host 'Production release artifacts are ready:'
    Get-ChildItem -LiteralPath $artifactDirectory | Select-Object Name, Length, LastWriteTime | Format-Table -AutoSize
    Write-Host "Signing certificate SHA-256: $($certificateDigests[0])"
} finally {
    Remove-Item Env:ROOMDECK_STORE_FILE -ErrorAction SilentlyContinue
    Remove-Item Env:ROOMDECK_STORE_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:ROOMDECK_KEY_ALIAS -ErrorAction SilentlyContinue
    Remove-Item Env:ROOMDECK_KEY_PASSWORD -ErrorAction SilentlyContinue
    $plainPassword = $null
}
