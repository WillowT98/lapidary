package name.lapidary.particle;

import name.lapidary.Lapidary;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Particle types used by Lapidary.
 *
 * The percolator bubble is registered on both logical sides, while its
 * provider and rendering implementation are registered only by the client.
 */
public final class ModParticles {

    public static final SimpleParticleType PERCOLATOR_BUBBLE =
            Registry.register(
                    BuiltInRegistries.PARTICLE_TYPE,
                    Lapidary.id("percolator_bubble"),
                    FabricParticleTypes.simple(true)
            );

    private ModParticles() {
    }

    /**
     * Forces static registration to occur during normal mod initialization.
     */
    public static void initialize() {
        Lapidary.LOGGER.info("Registered Lapidary particles");
    }
}
