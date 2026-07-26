package name.lapidary.window;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;

public enum WindowBackground {

    STONE_BRICKS(
            "stone_bricks",
            Blocks.STONE_BRICKS,
            ResourceLocation.withDefaultNamespace(
                    "block/stone_bricks"
            ),
            0x7A7A7A
    ),
    BRICKS(
            "bricks",
            Blocks.BRICKS,
            ResourceLocation.withDefaultNamespace(
                    "block/bricks"
            ),
            0x965A4A
    ),
    COBBLESTONE(
            "cobblestone",
            Blocks.COBBLESTONE,
            ResourceLocation.withDefaultNamespace(
                    "block/cobblestone"
            ),
            0x777777
    ),
    OAK_PLANKS(
            "oak_planks",
            Blocks.OAK_PLANKS,
            ResourceLocation.withDefaultNamespace(
                    "block/oak_planks"
            ),
            0xB58A55
    ),
    SPRUCE_PLANKS(
            "spruce_planks",
            Blocks.SPRUCE_PLANKS,
            ResourceLocation.withDefaultNamespace(
                    "block/spruce_planks"
            ),
            0x775538
    ),
    DARK_OAK_PLANKS(
            "dark_oak_planks",
            Blocks.DARK_OAK_PLANKS,
            ResourceLocation.withDefaultNamespace(
                    "block/dark_oak_planks"
            ),
            0x4A321D
    ),
    DEEPSLATE_BRICKS(
            "deepslate_bricks",
            Blocks.DEEPSLATE_BRICKS,
            ResourceLocation.withDefaultNamespace(
                    "block/deepslate_bricks"
            ),
            0x3B3B40
    ),
    POLISHED_BLACKSTONE_BRICKS(
            "polished_blackstone_bricks",
            Blocks.POLISHED_BLACKSTONE_BRICKS,
            ResourceLocation.withDefaultNamespace(
                    "block/polished_blackstone_bricks"
            ),
            0x2D262F
    ),
    SANDSTONE(
            "sandstone",
            Blocks.SANDSTONE,
            ResourceLocation.withDefaultNamespace(
                    "block/sandstone"
            ),
            0xD8C78F
    ),
    RED_SANDSTONE(
            "red_sandstone",
            Blocks.RED_SANDSTONE,
            ResourceLocation.withDefaultNamespace(
                    "block/red_sandstone"
            ),
            0xA95725
    ),
    QUARTZ_BRICKS(
            "quartz_bricks",
            Blocks.QUARTZ_BRICKS,
            ResourceLocation.withDefaultNamespace(
                    "block/quartz_bricks"
            ),
            0xE5E0DB
    ),
    PRISMARINE_BRICKS(
            "prismarine_bricks",
            Blocks.PRISMARINE_BRICKS,
            ResourceLocation.withDefaultNamespace(
                    "block/prismarine_bricks"
            ),
            0x63A99D
    ),
    MUD_BRICKS(
            "mud_bricks",
            Blocks.MUD_BRICKS,
            ResourceLocation.withDefaultNamespace(
                    "block/mud_bricks"
            ),
            0x876B58
    );

    private static final WindowBackground[] VALUES =
            values();

    private final String id;
    private final Block materialBlock;
    private final ResourceLocation texture;
    private final int previewColor;

    WindowBackground(
            String id,
            Block materialBlock,
            ResourceLocation texture,
            int previewColor
    ) {
        this.id =
                id;

        this.materialBlock =
                materialBlock;

        this.texture =
                texture;

        this.previewColor =
                previewColor;
    }

    public String id() {
        return id;
    }

    public Block materialBlock() {
        return materialBlock;
    }

    public Item materialItem() {
        return materialBlock.asItem();
    }

    public ResourceLocation texture() {
        return texture;
    }

    public int previewColor() {
        return previewColor;
    }

    public Component displayName() {
        return materialBlock.getName();
    }

    public int index() {
        return ordinal();
    }

    public static WindowBackground byId(
            String id
    ) {
        return Arrays.stream(VALUES)
                .filter(
                        background ->
                                background.id.equals(id)
                )
                .findFirst()
                .orElse(STONE_BRICKS);
    }

    public static WindowBackground byIndex(
            int index
    ) {
        return VALUES[
                Math.floorMod(
                        index,
                        VALUES.length
                )
        ];
    }

    public static int count() {
        return VALUES.length;
    }
}
