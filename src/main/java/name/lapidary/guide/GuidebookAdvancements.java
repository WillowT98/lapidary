package name.lapidary.guide;

import name.lapidary.Lapidary;
import name.lapidary.item.ModItems;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Awards the invisible, per-player advancements used as Patchouli entry locks.
 * Nothing here is team- or world-global: the supplied ServerPlayer is always
 * the sole recipient.
 */
public final class GuidebookAdvancements {
    public static final ResourceLocation ROOT = id("root");

    private GuidebookAdvancements() {
    }

    public static ResourceLocation id(String path) {
        return Lapidary.id("guide/" + path);
    }

    /**
     * @return true only when this call newly completed the advancement.
     */
    public static boolean grant(
            ServerPlayer player,
            String path,
            String entryTitle,
            boolean notify
    ) {
        return grant(player, id(path), entryTitle, notify);
    }

    public static boolean grant(
            ServerPlayer player,
            ResourceLocation advancementId,
            String entryTitle,
            boolean notify
    ) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }

        AdvancementHolder advancement =
                server.getAdvancements().get(advancementId);
        if (advancement == null) {
            Lapidary.LOGGER.warn(
                    "Missing guidebook advancement {}",
                    advancementId
            );
            return false;
        }

        boolean newlyGranted = player.getAdvancements().award(
                advancement,
                "granted"
        );
        if (newlyGranted && notify) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.lapidary.guidebook.new_entry",
                            Component.literal(entryTitle)
                    ),
                    true
            );
        }
        return newlyGranted;
    }

    /** Gives one personal journal the first time the root note is awarded. */
    public static void giveInitialJournal(ServerPlayer player) {
        ItemStack journal = new ItemStack(ModItems.LAPIDARY_GUIDEBOOK);
        if (!player.getInventory().add(journal)) {
            player.drop(journal, false);
        }
        player.displayClientMessage(
                Component.translatable(
                        "message.lapidary.guidebook.received"
                ),
                true
        );
    }
}
