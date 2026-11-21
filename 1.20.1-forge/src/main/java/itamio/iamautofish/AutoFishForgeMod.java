package itamio.iamautofish;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Auto Fishing+ — Itamio Port (1.20.1 Forge, client side only).
 *
 * <p>Derivative work of Auto-Fishing by Tr4nter (GPL-3.0). See CREDITS.md and
 * LICENSE.gpl.txt. Modified by Sekai0NI0itamio to use Mojang official mappings
 * and a rising-edge guarded state machine.</p>
 */
@Mod(AutoFishForgeMod.MOD_ID)
public class AutoFishForgeMod {

    public static final String MOD_ID = "itamioautofish";
    public static final String MOD_NAME = "Auto Fishing+ — Itamio Port";
    public static final String VERSION = "1.0.0";

    private static final int ACTION_COOLDOWN = 8;

    private enum Phase { IDLE, WAIT_FOR_REEL }

    // The Forge client has a single LocalPlayer; these fields are the
    // per-player rising-edge guard and reel/recast state machine.
    private static Phase phase = Phase.IDLE;
    private static int ticksSinceAction = ACTION_COOLDOWN;
    private static boolean wasBiting;
    // A rising edge seen during the cooldown is remembered, not consumed.
    private static boolean reelPending;

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ClientEvents {

        private ClientEvents() {}

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            ClientLevel level = mc.level;
            if (player == null || level == null) {
                hardReset();
                return;
            }
            // A GUI or a temporarily empty rod hand must NOT discard a pending
            // recast, otherwise the cycle stalls permanently after a reel.
            if (mc.screen != null) {
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
                // Wait for the state change (bobber removed) before recasting.
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

            // Verified Mojang name: FishingHook.biting (Yarn called it caughtFish).
            // Made public by META-INF/accesstransformer.cfg (SRG f_37099_).
            boolean biting = hook.biting;
            if (biting && !wasBiting) {
                reelPending = true; // rising edge, even if the cooldown is active
            }
            if (reelPending && ticksSinceAction >= ACTION_COOLDOWN) {
                useRod(mc, player, hand); // reel in once per bite
                phase = Phase.WAIT_FOR_REEL;
                ticksSinceAction = 0;
                reelPending = false;
            }
            wasBiting = biting;
        }
    }

    private static FishingHook findOwnHook(ClientLevel level, LocalPlayer player) {
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
