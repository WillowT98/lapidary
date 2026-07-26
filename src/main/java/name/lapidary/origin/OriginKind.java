package name.lapidary.origin;

import net.minecraft.world.entity.player.Player;

public enum OriginKind {

    NONE(""),
    FELINE("lapidary_origin_feline"),
    MOTH("lapidary_origin_moth"),
    FAIRY("lapidary_origin_fairy");

    private final String entityTag;

    OriginKind(
            String entityTag
    ) {
        this.entityTag =
                entityTag;
    }

    public String entityTag() {
        return entityTag;
    }

    public boolean matches(
            Player player
    ) {
        return this != NONE
                && player.getTags()
                .contains(entityTag);
    }

    public static OriginKind of(
            Player player
    ) {
        for (OriginKind kind : values()) {
            if (kind.matches(player)) {
                return kind;
            }
        }

        return NONE;
    }
}
