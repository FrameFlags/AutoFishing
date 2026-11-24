# Credits — 1.20.1 Fabric variant

## Upstream

- **Auto-Fishing** by **Tr4nter** — <https://github.com/Tr4nter/Auto-Fishing> — GPL-3.0.
- Adapted files: the client tick bite check, the bobber accessor mixin, and the
  rod interaction queue (replaced with an explicit state machine).

## Modifications by Sekai0NI0itamio

- **Mojang official mappings only** (`loom.officialMojangMappings()`). Names were
  verified against the Mojang `client_mappings.txt` in the local Gradle cache:
  - Yarn `FishingBobberEntity` → Mojang `net.minecraft.world.entity.projectile.FishingHook`
  - Yarn `caughtFish` → Mojang private field `biting`
  - Yarn `MinecraftClient` → Mojang `net.minecraft.client.Minecraft`
  - Yarn `ClientPlayerInteractionManager.interactItem` → Mojang
    `MultiPlayerGameMode.useItem(Player, InteractionHand)`
- Bobber lookup iterates client level entities and checks
  `FishingHook.getPlayerOwner() == player`. No guessed `player.fishHook` field.
- Rising-edge guard: the rod is used once when `biting` goes false→true; the
  recast happens only after the bobber is gone (state change), with a cooldown.
- Client-only: `ClientModInitializer` + `ClientTickEvents.START_CLIENT_TICK`,
  `"environment": "client"` in `fabric.mod.json`.
- No commands, no GUI, no config screen.

## Verification status / risk

A green Gradle build proves compilation and packaging only. It does not prove
the in-game fishing cycle. Risks: `getPlayerOwner()` on the client depends on the
owner being synced; if a server/mod delays that, the hook may not be found.
Server latency between the synced bite flag and the reel request can also
affect timing. **In-game verification is still required.**

## License

GPL-3.0 — full text in `LICENSE.gpl.txt`. Source is in `src/main/java`.
