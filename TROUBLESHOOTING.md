# Troubleshooting

## Claude Code VM cache fix (macOS)

If Claude Code fails to start or behaves unexpectedly due to a corrupted VM
cache, clear the cached VM state and restart the app.

### Steps

1. Quit Claude.
2. Delete the following folders:
   - `~/Library/Application Support/Claude/claude-code-vm`
   - `~/Library/Application Support/Claude/vm_bundles`
3. Restart Claude.

### Commands

```sh
rm -rf ~/Library/Application\ Support/Claude/claude-code-vm
rm -rf ~/Library/Application\ Support/Claude/vm_bundles
```

After restarting, Claude will rebuild these caches on first launch.
