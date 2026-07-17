package name.lapidary.tag;

import name.lapidary.Lapidary;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModBlockTags {
    public static final TagKey<Block> SIFTABLE_BY_SIEVE = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(
                    Lapidary.MOD_ID,
                    "siftable_by_sieve"
            )
    );

    private ModBlockTags() {
    }
}