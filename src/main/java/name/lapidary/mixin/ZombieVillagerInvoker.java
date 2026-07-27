package name.lapidary.mixin;

import net.minecraft.world.entity.monster.ZombieVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.UUID;

/** Exposes vanilla's private curing starter without replacing curing logic. */
@Mixin(ZombieVillager.class)
public interface ZombieVillagerInvoker {
    @Invoker("startConverting")
    void lapidary$startConverting(UUID starter, int conversionTime);
}
