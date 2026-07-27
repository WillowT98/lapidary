package name.lapidary.client.dynamiclight;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import name.lapidary.Lapidary;
import name.lapidary.client.origin.ClientOriginState;
import name.lapidary.origin.OriginKind;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

/**
 * Registers Lapidary's custom dynamic light sources.
 */
@SuppressWarnings({
        "deprecation",
        "removal"
})
public final class LapidaryDynamicLights
        implements DynamicLightsInitializer {

    /**
     * Dynamic-light values range from 0 through 15.
     */
    private static final int FAIRY_LIGHT_LEVEL =
            15;

    private static final FairyPlayerLuminance FAIRY_LUMINANCE =
            new FairyPlayerLuminance();

    public static final EntityLuminance.Type FAIRY_LUMINANCE_TYPE =
            EntityLuminance.Type.registerSimple(
                    Lapidary.id(
                            "fairy_player"
                    ),
                    FAIRY_LUMINANCE
            );

    @Override
    public void onInitializeDynamicLights(
            DynamicLightsContext context
    ) {
        context.entityLightSourceManager()
                .onRegisterEvent()
                .register(
                        registrationContext ->
                                registrationContext.register(
                                        EntityType.PLAYER,
                                        FAIRY_LUMINANCE
                                )
                );
    }

    /**
     * LambDynamicLights 4.8.10 for Minecraft 1.21.1 still requires
     * this legacy method, despite marking it for future removal.
     *
     * Dynamic-light registration is performed through the newer
     * context-based method above.
     */
    @Override
    @Deprecated(forRemoval = true)
    @SuppressWarnings({
            "deprecation",
            "removal"
    })
    public void onInitializeDynamicLights(
            ItemLightSourceManager ignored
    ) {
        // Required compatibility method for the 1.21.1 API.
    }

    private static final class FairyPlayerLuminance
            implements EntityLuminance {

        private FairyPlayerLuminance() {
        }

        @Override
        public Type type() {
            return FAIRY_LUMINANCE_TYPE;
        }

        @Override
        public int getLuminance(
                ItemLightSourceManager itemLightSourceManager,
                Entity entity
        ) {
            if (!(entity instanceof Player player)) {
                return 0;
            }

            Minecraft client =
                    Minecraft.getInstance();

            /*
             * ClientOriginState is explicitly synchronized by
             * OriginStatePayload and reliably identifies the local
             * player's origin.
             */
            boolean isLocalFairy =
                    player == client.player
                            && ClientOriginState.originKind()
                            == OriginKind.FAIRY.ordinal();

            /*
             * This may also identify remote Fairy players when their
             * origin marker tag is available on the client.
             */
            boolean hasFairyMarker =
                    OriginKind.FAIRY.matches(
                            player
                    );

            return isLocalFairy
                    || hasFairyMarker
                    ? FAIRY_LIGHT_LEVEL
                    : 0;
        }
    }
}