# Auto Fishing+ — Itamio Port

Automatically reels in the rod when a fish bites, then recasts.

- Client-side only. No commands, no GUI, no config screen.
- Rising-edge guarded: one reel + one recast per bite.
- Packages: `itamio.iamautofish` · mod id: `itamioautofish` · version `1.0.0`
- Author: Sekai0NI0itamio

## Variants

| Directory | Loader / MC | Bite detection |
|---|---|---|
| `1.12.2-forge/` | Forge 1.12.2, MCP `stable_39` | client-side bobber dip (heuristic) |
| `1.20.1-fabric/` | Fabric 1.20.1, Mojang mappings | `FishingHook.biting` via mixin accessor |
| `1.20.1-forge/` | Forge 1.20.1, Mojang mappings | `FishingHook.biting` via mixin accessor |

## License / attribution

Derived from [Auto-Fishing](https://github.com/Tr4nter/Auto-Fishing) by Tr4nter,
licensed **GPL-3.0**. See `CREDITS.md` and `LICENSE.gpl.txt` in each variant.
Source for every binary is in this repository.

> A successful Gradle build only proves the code compiles and packages. It does
> **not** prove the fishing cycle works in game. Gameplay verification is still
> required, especially for the 1.12.2 bobber-dip heuristic.
