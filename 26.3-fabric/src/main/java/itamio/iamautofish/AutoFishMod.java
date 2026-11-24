package itamio.iamautofish;

import itamio.iamautofish.mixin.FishingHookAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Auto Fishing+ — Itamio Port (1.20.1 Fabric).
 *
 * <p>Derivative work of Auto-Fishing by Tr4nter (GPL-3.0). See CREDITS.md and
 * LICENSE.gpl.txt. Modified by Sekai0NI0itamio to use Mojang official mappings,
 * a bobber-owner lookup, and a rising-edge guarded state machine.</p>
 *
 * <p>All names below were verified against the Mojang mapping file in the local
 * Gradle cache. In official mappings the bobber class is
 * {@code net.minecraft.world.entity.projectile.FishingHook} (Yarn's
 * {@code FishingBobberEntity}) and its bite flag is {@code biting} (Yarn's
 * {@code caughtFish}).</p>
 */
public class AutoFishMod implements ClientModInitializer {

    public static final String MOD_ID = "itamioautofish";
    public static final String MOD_NAME = "Auto Fishing+ — Itamio Port";
    public static final String VERSION = "1.0.0";

    private static final int ACTION_COOLDOWN = 8;

    private enum Phase { IDLE, WAIT_FOR_REEL }

    // A Fabric client has exactly one LocalPlayer, so these static fields are
    // effectively a per-player rising-edge guard / state machine.
    private static Phase phase = Phase.IDLE;
    private static int ticksSinceAction = ACTION_COOLDOWN;
    private static boolean wasBiting;
    // A rising edge seen during the cooldown is remembered, not consumed.
    private static boolean reelPending;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(AutoFishMod::onClientTick);
    }

    private static void onClientTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) {
            hardReset();
            return;
        }
        // A GUI or a temporarily empty rod hand must NOT discard a pending
        // recast, otherwise the cycle stalls permanently after a reel.
        if (mc.gui.screen() != null) {
            wasBiting = false;
            return;
        }
        if (ticksSinceAction < ACTION_COOLDOWN) {
            ticksSinceAction++;
        }

        InteractionHand hand = rodHand(player);
        if (hand == null) {
            wasBiting = false;
            return;
        }

        FishingHook hook = findOwnHook(level, player);

        if (phase == Phase.WAIT_FOR_REEL) {
            // Wait for the state change (bobber removed by the reel) before recasting.
            if (hook == null && ticksSinceAction >= ACTION_COOLDOWN) {
                useRod(mc, player, hand);
                phase = Phase.IDLE;
                ticksSinceAction = 0;
            }
            reelPending = false;
            wasBiting = false;
            return;
        }

        if (hook == null) {
            wasBiting = false;
            reelPending = false;
            return;
        }

        // Verified: FishingHook.biting is the private field mirroring Mojang's
        // DATA_BITING synched data, updated client-side in onSyncedDataUpdated.
        boolean biting = ((FishingHookAccessor) hook).iamautofish$getBiting();
        if (biting && !wasBiting) {
            reelPending = true; // rising edge, even if the cooldown is active
        }
        if (reelPending && ticksSinceAction >= ACTION_COOLDOWN) {
            useRod(mc, player, hand); // reel in, once for this rising edge
            phase = Phase.WAIT_FOR_REEL;
            ticksSinceAction = 0;
            reelPending = false;
        }
        wasBiting = biting;
    }

    private static FishingHook findOwnHook(ClientLevel level, LocalPlayer player) {
        // getPlayerOwner() is public in Mojang mappings; no reliance on a
        // guessed player.fishHook field.
        List<Entity> found = level.getEntities(
                player,
                player.getBoundingBox().inflate(128.0D),
                e -> e instanceof FishingHook && ((FishingHook) e).getPlayerOwner() == player);
        return found.isEmpty() ? null : (FishingHook) found.get(0);
    }

    private static void useRod(Minecraft mc, LocalPlayer player, InteractionHand hand) {
        // Verified signature: MultiPlayerGameMode.useItem(Player, InteractionHand)
        mc.gameMode.useItem(player, hand);
    }

    private static InteractionHand rodHand(LocalPlayer player) {
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!main.isEmpty() && main.getItem() == Items.FISHING_ROD) {
            return InteractionHand.MAIN_HAND;
        }
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!off.isEmpty() && off.getItem() == Items.FISHING_ROD) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private static void hardReset() {
        phase = Phase.IDLE;
        wasBiting = false;
        reelPending = false;
    }
}
