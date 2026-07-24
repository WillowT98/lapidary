package name.lapidary.progression;

import com.mojang.serialization.Codec;
import name.lapidary.Lapidary;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;

public final class ModAttachments {

    /**
     * Persistent Insight total.
     */
    public static final AttachmentType<Integer>
            LAPIDARY_INSIGHT =
            AttachmentRegistry
                    .<Integer>builder()
                    .initializer(() -> 0)
                    .persistent(Codec.INT)
                    .copyOnDeath()
                    .buildAndRegister(
                            ResourceLocation
                                    .fromNamespaceAndPath(
                                            Lapidary.MOD_ID,
                                            "lapidary_insight"
                                    )
                    );

    /**
     * Persistent Tome purchases.
     *
     * Each purchased node occupies one bit in this long.
     */
    public static final AttachmentType<Long>
            TOME_PURCHASES =
            AttachmentRegistry
                    .<Long>builder()
                    .initializer(() -> 0L)
                    .persistent(Codec.LONG)
                    .copyOnDeath()
                    .buildAndRegister(
                            ResourceLocation
                                    .fromNamespaceAndPath(
                                            Lapidary.MOD_ID,
                                            "tome_purchases"
                                    )
                    );

    private ModAttachments() {
    }

    public static void initialize() {
        Lapidary.LOGGER.info(
                "Registering Lapidary data attachments"
        );
    }
}