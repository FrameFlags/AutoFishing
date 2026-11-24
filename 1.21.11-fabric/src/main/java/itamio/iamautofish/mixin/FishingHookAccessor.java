package itamio.iamautofish.mixin;

import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mojang-mapped counterpart of upstream's Yarn {@code FishingBobberEntityMixin}.
 * The official field name is {@code biting}, not {@code caughtFish}.
 */
@Mixin(FishingHook.class)
public interface FishingHookAccessor {

    @Accessor("biting")
    boolean iamautofish$getBiting();
}
