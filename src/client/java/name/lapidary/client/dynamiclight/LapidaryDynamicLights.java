package name.lapidary.client.dynamiclight;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import name.lapidary.Lapidary;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

/**
 * Registers Lapidary's client-side dynamic light sources.
 */
public final class LapidaryDynamicLights
        implements DynamicLightsInitializer {

    /*
     * LambDynamicLights accepts values from 0 through 15.
     *
     * 12 is bright enough to make the Fairy clearly luminous without
     * making her equivalent to a full-strength light block.
     */
    private static final int FAIRY_LIGHT_LEVEL = 12;

    private static final FairyPlayerLuminance FAIRY_PLAYER_LUMINANCE =
            new FairyPlayerLuminance();

    /*
     * Every EntityLuminance implementation needs a registered type,
     * even though this implementation is registered directly through
     * Java rather than loaded from a JSON file.
     */
    public static final EntityLuminance.Type FAIRY_PLAYER_LUMINANCE_TYPE =
            EntityLuminance.Type.registerSimple(
                    Lapidary.id("fairy_player"),
                    FAIRY_PLAYER_LUMINANCE
            );

    @Override
    public void onInitializeDynamicLights(
            DynamicLightsContext context
    ) {
        context.entityLightSourceManager()
                .onRegisterEvent()
                .register(registrationContext ->
                        registrationContext.register(
                                EntityType.PLAYER,
                                FAIRY_PLAYER_LUMINANCE
                        )
                );
    }

    /*
     * LambDynamicLights 4.8.10 still retains this older abstract
     * initializer method for compatibility. The context-based method
     * above is the one we actually need.
     */
    @Override
    @SuppressWarnings({"deprecation", "removal"})
    public void onInitializeDynamicLights(
            ItemLightSourceManager itemLightSourceManager
    ) {
        // Nothing to register for held items.
    }

    private static final class FairyPlayerLuminance
            implements EntityLuminance {

        private FairyPlayerLuminance() {
        }

        @Override
        public Type type() {
            return FAIRY_PLAYER_LUMINANCE_TYPE;
        }

        @Override
        public int getLuminance(
                ItemLightSourceManager itemLightSourceManager,
                Entity entity
        ) {
            /*
             * fairy_self_glow already causes Fairy players to satisfy
             * Minecraft's client-side glowing check. Reusing that state
             * avoids duplicating the origin-detection system or adding
             * another network packet.
             */
            if (entity instanceof Player
                    && entity.isCurrentlyGlowing()) {

                return FAIRY_LIGHT_LEVEL;
            }

            return 0;
        }
    }
}