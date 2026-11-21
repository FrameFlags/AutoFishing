# Credits — 1.20.1 Forge variant

## Upstream

- **Auto-Fishing** by **Tr4nter** — <https://github.com/Tr4nter/Auto-Fishing> — GPL-3.0.
- Adapted: client-tick bite polling, bite flag read, reel-then-recast idea.

## Modifications by Sekai0NI0itamio

- Ported to Forge 1.20.1 / ForgeGradle 6 with **Mojang official mappings**
  (`mapping_channel=official`). All names verified in the local Mojang mapping
  file and via `javap` on the mapped Forge jar:
  - `FishingBobberEntity` → `net.minecraft.world.entity.projectile.FishingHook`
  - Yarn `caughtFish` → Mojang private field `biting`
  - `MinecraftClient` → `net.minecraft.client.Minecraft`
  - rod use → `MultiPlayerGameMode.useItem(Player, InteractionHand)`
- Instead of upstream's Yarn `@Accessor("caughtFish")` mixin (the Mixin 0.8.5
  annotation processor cannot resolve Mojang field names in ForgeGradle's dev
  environment), this variant uses a **Forge Access Transformer**:
  `META-INF/accesstransformer.cfg` makes `FishingHook.biting` public using the
  verified SRG name `f_37099_`. No mixin config is shipped.
- Bobber found by iterating client level entities via
  `Level.getEntities(...)` and checking `FishingHook.getPlayerOwner()`.
- Rising-edge guarded: one reel when `biting` flips false→true, one recast only
  after the bobber disappears; cooldown prevents repeat triggers.
- Client side only: the FORGE-bus `@EventBusSubscriber(..., value = Dist.CLIENT)`
  listens to `TickEvent.ClientTickEvent`. No server-side logic.
- No commands, no GUI, no config screen.

## Verification status / risk

A green Gradle build proves compilation and packaging only, not in-game
behaviour. Risks: access-transformer application in production,
`getPlayerOwner()` resolution on the client, and latency between the synced
bite flag and the rod use. **In-game verification is still required.**

## License

GPL-3.0 — full text in `LICENSE.gpl.txt`. Source is in `src/main/java`.
