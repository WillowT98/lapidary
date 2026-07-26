package name.lapidary.origin;

import name.lapidary.progression.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class OriginPlayerData {

    private OriginPlayerData() {
    }

    public static OriginAbilityData get(
            Player player
    ) {
        return player.getAttachedOrCreate(
                ModAttachments.ORIGIN_ABILITIES
        );
    }

    public static void set(
            ServerPlayer player,
            OriginAbilityData data
    ) {
        player.setAttached(
                ModAttachments.ORIGIN_ABILITIES,
                data
        );

        OriginManager.sync(
                player
        );
    }
}
