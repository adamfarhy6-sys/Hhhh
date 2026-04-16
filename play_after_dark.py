#!/usr/bin/env python3
"""Play "After Dark" by Mr. Kitty.

Tries, in order:
  1. A local audio file passed as an argument or named ``after_dark.mp3`` /
     ``after_dark.wav`` in the current directory, played with an available
     command-line player (ffplay, mpv, mpg123, afplay, aplay).
  2. Falls back to opening a YouTube search for the song in the default
     web browser.
"""
from __future__ import annotations

import os
import shutil
import subprocess
import sys
import webbrowser
from urllib.parse import quote_plus

SONG_QUERY = "Mr Kitty - After Dark"
LOCAL_CANDIDATES = ("after_dark.mp3", "after_dark.wav", "after_dark.ogg")
PLAYERS = (
    ("ffplay", ["-nodisp", "-autoexit", "-loglevel", "quiet"]),
    ("mpv", ["--no-video"]),
    ("mpg123", ["-q"]),
    ("afplay", []),
    ("aplay", ["-q"]),
)


def find_local_file(argv: list[str]) -> str | None:
    if len(argv) > 1 and os.path.isfile(argv[1]):
        return argv[1]
    for name in LOCAL_CANDIDATES:
        if os.path.isfile(name):
            return name
    return None


def play_local(path: str) -> bool:
    for binary, flags in PLAYERS:
        if shutil.which(binary):
            print(f"Playing {path!r} with {binary}...")
            try:
                subprocess.run([binary, *flags, path], check=True)
                return True
            except subprocess.CalledProcessError as exc:
                print(f"{binary} exited with status {exc.returncode}")
                return False
    return False


def play_via_browser() -> None:
    url = f"https://www.youtube.com/results?search_query={quote_plus(SONG_QUERY)}"
    print(f"Opening browser search for {SONG_QUERY!r}...")
    webbrowser.open(url)


def main(argv: list[str]) -> int:
    local = find_local_file(argv)
    if local and play_local(local):
        return 0
    if local:
        print("No working CLI audio player found for the local file.")
    play_via_browser()
    return 0


if __name__ == "__main__":
    raise SystemExit(main(sys.argv))
