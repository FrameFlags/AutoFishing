# Credits — Auto Fishing+ — Itamio Port

## Upstream project

- **Auto-Fishing** by **Tr4nter** — <https://github.com/Tr4nter/Auto-Fishing>
- Upstream license: **GNU General Public License v3.0 (GPL-3.0)**.
- Upstream source files adapted for this port:
  - `TickEvent.java` — client-tick bite polling and rod reeling
  - `FishingBobberEntityMixin.java` — accessor for the bobber's bite flag
  - `ClientPlayerInteractionManagerInjector.java` — rod interaction coordination

## What was changed by Sekai0NI0itamio

- Retargeted the mechanic to three variants: `1.12.2-forge`, `1.20.1-fabric`, `1.20.1-forge`.
- Moved everything into package `itamio.iamautofish` with mod id `itamioautofish`.
- **1.20.1:** replaced every Yarn name from upstream with **Mojang official mappings**
  (verified against the Mojang mapping file in the local Gradle cache):
  - `FishingBobberEntity` → `net.minecraft.world.entity.projectile.FishingHook`
  - `caughtFish` → `biting` (private field, read through `@Accessor("biting")`)
  - `MinecraftClient` → `net.minecraft.client.Minecraft`
  - `interactItem` → `MultiPlayerGameMode.useItem(Player, InteractionHand)`
  - Found the bobber by iterating level entities and checking `getPlayerOwner()`,
    rather than relying on a `player.fishHook` field.
- Rewrote upstream's scheduler into an explicit rising-edge state machine so the
  rod is triggered **once per bite**, then recast only after the bobber disappears.
- Removed upstream's config file, config screen and commands. There is no GUI and
  no config screen in this port.
- **1.12.2:** fully rewritten. MCP `stable_39` `EntityFishHook` has no client-visible
  bite flag (`caughtEntity` is the hooked entity; `ticksCatchable` is server-only),
  so the client detects the vanilla bobber dip instead. See each variant's
  `CREDITS.md` and the source comments for details.

## License

This is a derivative work of a GPL-3.0 project and is therefore licensed under
**GPL-3.0**, not MIT. The complete license text is in `LICENSE.gpl.txt`.
The complete source for each binary build is available in this repository
(`SimpleMechanicMods/mods/auto-fishing/<variant>/src/main/java`).
