package name.lapidary.tag;

import name.lapidary.Lapidary;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModBlockTags {
    public static final TagKey<Block> SIFTABLE_BY_SIEVE =
            create("siftable_by_sieve");

    /** Blocks which the first-pass Gravity spell may safely animate. */
    public static final TagKey<Block> GRAVITY_AFFECTED =
            create("gravity_affected");

    /** Ores revealed by the first-pass ore-sight spell. */
    public static final TagKey<Block> ORES =
            create("ores");

    private static TagKey<Block> create(String path) {
        return TagKey.create(
                Registries.BLOCK,
                Lapidary.id(path)
        );
    }

    private ModBlockTags() {
    }
}
