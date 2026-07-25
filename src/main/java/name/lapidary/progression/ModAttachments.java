package name.lapidary.progression;

import com.mojang.serialization.Codec;
import name.lapidary.Lapidary;
import name.lapidary.magic.PlayerMagicData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class ModAttachments {

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
     * Legacy bitmask from the first Tome prototype.
     *
     * Keep this registered so existing saves can be read and refunded.
     * It can be removed after the save format has been stable for a
     * sufficiently long time.
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
    /**
     * Persistent player magic knowledge and prepared loadout.
     */
    public static final AttachmentType<PlayerMagicData> PLAYER_MAGIC =
            AttachmentRegistry
                    .<PlayerMagicData>builder()
                    .initializer(PlayerMagicData::empty)
                    .persistent(PlayerMagicData.CODEC)
                    .copyOnDeath()
                    .buildAndRegister(
                            Lapidary.id("player_magic")
                    );

    /**
     * Current Tome purchase storage.
     *
     * Each entry is a stable node ID such as:
     *     schools/nature
     *     nature/verdancy
     */
    public static final AttachmentType<List<String>>
            TOME_PURCHASED_NODES =
            AttachmentRegistry
                    .<List<String>>builder()
                    .initializer(List::of)
                    .persistent(
                            Codec.STRING.listOf()
                    )
                    .copyOnDeath()
                    .buildAndRegister(
                            ResourceLocation
                                    .fromNamespaceAndPath(
                                            Lapidary.MOD_ID,
                                            "tome_purchased_nodes"
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