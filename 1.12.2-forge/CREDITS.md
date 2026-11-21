# Credits — 1.12.2 Forge variant

## Upstream

- **Auto-Fishing** by **Tr4nter** — <https://github.com/Tr4nter/Auto-Fishing> — GPL-3.0.
- Original ideas adapted: client-tick bite polling, once-per-bite rod trigger,
  reel-then-recast sequencing.

## Modifications by Sekai0NI0itamio

- Rewritten from scratch against **MCP stable_39** names (verified in the local
  ForgeGradle sources jar), package `itamio.iamautofish`, mod id `itamioautofish`.
- **No direct port of the upstream `caughtFish` accessor.** In 1.12.2
  `EntityFishHook` there is no client-side `caughtFish` boolean. `caughtEntity`
  is the hooked entity, and the real bite countdown `ticksCatchable` is only
  advanced on the server inside `catchingFish()`. Reading it on the client would
  always return the initial value.
- Safe fallback implemented as requested: a client `TickEvent.ClientTickEvent`
  uses the player's own `EntityFishHook` (`EntityPlayer.fishEntity`) and detects
  the vanilla bite dip (the server's extra downward motion is synced to the
  client). The trigger is rising-edge guarded and followed by a single recast
  once the bobber disappears.
- No commands, no GUI, no config screen.

## Verification status / risk

A green Gradle build proves compilation only. The bobber-dip heuristic is a
heuristic: threshold `0.12` blocks and a 20-tick arming delay may need tuning
for real servers (ping, TPS, lure enchantments, modded bobbers). **In-game
verification is still required.**

## License

GPL-3.0 — full text in `LICENSE.gpl.txt`. Source is in `src/main/java`.
