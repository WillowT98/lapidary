package name.lapidary.progression;

import com.mojang.serialization.Codec;
import name.lapidary.Lapidary;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;

public final class ModAttachments {

    /**
     * A persistent integer stored separately on each player.
     *
     * initializer(() -> 0):
     * New players begin with 0 Insight.
     *
     * persistent(Codec.INT):
     * The value is written to the player's save data.
     *
     * copyOnDeath():
     * The value is copied to the player's new entity after respawning.
     */
    public static final AttachmentType<Integer> LAPIDARY_INSIGHT =
            AttachmentRegistry.<Integer>builder()
                    .initializer(() -> 0)
                    .persistent(Codec.INT)
                    .copyOnDeath()
                    .buildAndRegister(
                            ResourceLocation.fromNamespaceAndPath(
                                    Lapidary.MOD_ID,
                                    "lapidary_insight"
                            )
                    );

    private ModAttachments() {
    }

    public static void initialize() {
        Lapidary.LOGGER.info("Registering Lapidary data attachments");
    }
}