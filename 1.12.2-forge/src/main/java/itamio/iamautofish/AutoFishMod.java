package itamio.iamautofish;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Auto Fishing+ — Itamio Port (1.12.2 Forge).
 *
 * <p>Derivative work of Auto-Fishing by Tr4nter (GPL-3.0). See CREDITS.md and
 * LICENSE.gpl.txt. Modified by Sekai0NI0itamio for MCP stable_39 names and a
 * rising-edge guarded state machine.</p>
 *
 * <p>1.12.2 note: MCP stable_39 EntityFishHook has no client-visible boolean
 * bite flag. {@code caughtEntity} is the hooked entity, and the real bite
 * countdown ({@code ticksCatchable}) is only advanced inside
 * {@code catchingFish()}, which the vanilla code calls server-side only.
 * Therefore the client detects the vanilla bite dip: while a fish bites the
 * server applies extra downward motion to the bobber each tick, which is
 * synced to the client. This is a heuristic and needs in-game verification.</p>
 */
@Mod(modid = AutoFishMod.MOD_ID, name = AutoFishMod.MOD_NAME, version = AutoFishMod.VERSION)
public class AutoFishMod {

    public static final String MOD_ID = "itamioautofish";
    public static final String MOD_NAME = "Auto Fishing+ — Itamio Port";
    public static final String VERSION = "1.0.0";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new ClientAutoFish());
        }
    }

    /** Client-only automation. Registered only on the client side. */
    public static final class ClientAutoFish {

        private static final int ACTION_COOLDOWN = 8;
        private static final int ARM_TICKS = 20;
        private static final double DIP_THRESHOLD = 0.12D;

        private enum Phase { IDLE, WAIT_FOR_REEL }

        private Phase phase = Phase.IDLE;
        private int ticksSinceAction = ACTION_COOLDOWN;
        private int ticksInWater;
        private double restY = Double.NaN;
        private boolean wasBiting;
        // Rising edge seen during the cooldown is remembered, not consumed.
        private boolean reelPending;

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.player;
            World world = mc.world;
            if (player == null || world == null) {
                reset();
                return;
            }
            // A GUI or a temporarily empty rod hand must NOT discard a pending
            // recast, otherwise the cycle stalls permanently after a reel.
            if (mc.currentScreen != null) {
                resetDetection();
                return;
            }
            if (ticksSinceAction < ACTION_COOLDOWN) {
                ticksSinceAction++;
            }

            EnumHand hand = rodHand(player);
            if (hand == null) {
                resetDetection();
                return;
            }

            // Verified from MCP stable_39 sources: EntityPlayer.fishEntity is a
            // public field set by EntityFishHook.init() to the player's own hook.
            EntityFishHook hook = player.fishEntity;
            if (hook == null || hook.isDead) {
                if (phase == Phase.WAIT_FOR_REEL && ticksSinceAction >= ACTION_COOLDOWN) {
                    // The hook disappeared after our reel: recast exactly once.
                    useRod(mc, player, hand);
                    phase = Phase.IDLE;
                    ticksSinceAction = 0;
                } else if (phase == Phase.IDLE) {
                    resetDetection();
                }
                reelPending = false;
                return;
            }

            if (!hook.isInWater()) {
                resetDetection();
                return;
            }
            ticksInWater++;

            double y = hook.posY;
            if (Double.isNaN(restY) || y > restY) {
                restY = y; // highest observed position ~= resting water level
            }
            boolean bitingNow = ticksInWater >= ARM_TICKS
                    && (restY - y) > DIP_THRESHOLD
                    && hook.motionY < 0.0D;

            if (bitingNow && !wasBiting) {
                reelPending = true; // rising edge, even if the cooldown is active
            }
            if (phase == Phase.IDLE && reelPending && ticksSinceAction >= ACTION_COOLDOWN) {
                useRod(mc, player, hand); // reel in, once for this rising edge
                phase = Phase.WAIT_FOR_REEL;
                ticksSinceAction = 0;
                reelPending = false;
            }
            wasBiting = bitingNow;
        }

        private void resetDetection() {
            ticksInWater = 0;
            restY = Double.NaN;
            wasBiting = false;
        }

        private void reset() {
            resetDetection();
            phase = Phase.IDLE;
            reelPending = false;
        }

        private static void useRod(Minecraft mc, EntityPlayer player, EnumHand hand) {
            // Verified method: PlayerControllerMP.processRightClick(EntityPlayer, World, EnumHand)
            mc.playerController.processRightClick(player, mc.world, hand);
        }

        private static EnumHand rodHand(EntityPlayer player) {
            ItemStack main = player.getHeldItemMainhand();
            if (main != null && main.getItem() instanceof ItemFishingRod) {
                return EnumHand.MAIN_HAND;
            }
            ItemStack off = player.getHeldItemOffhand();
            if (off != null && off.getItem() instanceof ItemFishingRod) {
                return EnumHand.OFF_HAND;
            }
            return null;
        }
    }
}
