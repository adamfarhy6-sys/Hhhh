<#
.SYNOPSIS
    Download the audio of "After Dark" (Mr. Kitty) from YouTube as MP3.

.DESCRIPTION
    Requires yt-dlp and ffmpeg to be installed and on PATH.
    Install with: winget install yt-dlp.yt-dlp ; winget install Gyan.FFmpeg

.EXAMPLE
    .\Download-AfterDark.ps1
    .\Download-AfterDark.ps1 -Url 'https://www.youtube.com/watch?v=zOdKS_ayMyM' -Output .\
#>
param(
    [string]$Url = 'https://www.youtube.com/watch?v=zOdKS_ayMyM',
    [string]$Output = '.'
)

$ErrorActionPreference = 'Stop'

if (-not (Get-Command yt-dlp -ErrorAction SilentlyContinue)) {
    Write-Error "yt-dlp not found. Install via: winget install yt-dlp.yt-dlp"
    exit 1
}
if (-not (Get-Command ffmpeg -ErrorAction SilentlyContinue)) {
    Write-Warning "ffmpeg not found; MP3 conversion will fail. Install via: winget install Gyan.FFmpeg"
}

if (-not (Test-Path -LiteralPath $Output)) {
    New-Item -ItemType Directory -Path $Output | Out-Null
}

$template = Join-Path $Output 'after_dark.%(ext)s'
yt-dlp -x --audio-format mp3 --audio-quality 0 -o $template $Url
