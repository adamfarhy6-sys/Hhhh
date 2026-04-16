<#
.SYNOPSIS
    Play "After Dark" by Mr. Kitty.

.DESCRIPTION
    If a local audio file is provided (or after_dark.mp3/.wav exists in the
    current directory), it is played with Windows Media Player in the
    background. Otherwise the song is opened on YouTube in the default
    browser.

.EXAMPLE
    .\Play-AfterDark.ps1
    .\Play-AfterDark.ps1 -Path .\after_dark.mp3
#>
param(
    [string]$Path
)

$ErrorActionPreference = 'Stop'

function Find-LocalFile {
    param([string]$Provided)

    if ($Provided -and (Test-Path -LiteralPath $Provided)) {
        return (Resolve-Path -LiteralPath $Provided).Path
    }
    foreach ($name in 'after_dark.mp3', 'after_dark.wav', 'after_dark.ogg') {
        if (Test-Path -LiteralPath $name) {
            return (Resolve-Path -LiteralPath $name).Path
        }
    }
    return $null
}

function Play-LocalFile {
    param([string]$File)

    Write-Host "Playing $File ..."
    try {
        $wmp = New-Object -ComObject WMPlayer.OCX
        $wmp.URL = $File
        $wmp.controls.play() | Out-Null
        while ($wmp.playState -ne 1) { Start-Sleep -Milliseconds 500 }
        return $true
    } catch {
        Write-Warning "Windows Media Player COM failed: $_"
    }

    try {
        Add-Type -AssemblyName PresentationCore
        $player = New-Object System.Windows.Media.MediaPlayer
        $player.Open([Uri]::new($File))
        $player.Play()
        Start-Sleep -Seconds 1
        while ($player.NaturalDuration.HasTimeSpan -eq $false) { Start-Sleep -Milliseconds 200 }
        $duration = $player.NaturalDuration.TimeSpan.TotalSeconds
        Start-Sleep -Seconds ([int][Math]::Ceiling($duration))
        return $true
    } catch {
        Write-Warning "MediaPlayer failed: $_"
        return $false
    }
}

function Open-YouTube {
    $query = [Uri]::EscapeDataString('Mr Kitty - After Dark')
    $url = "https://www.youtube.com/results?search_query=$query"
    Write-Host "Opening browser: $url"
    Start-Process $url
}

$local = Find-LocalFile -Provided $Path
if ($local) {
    if (Play-LocalFile -File $local) { return }
    Write-Warning 'No working local player; falling back to browser.'
}
Open-YouTube
