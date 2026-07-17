package name.lapidary.block;

import name.lapidary.Lapidary;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import name.lapidary.fluid.ModFluids;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;

public final class ModBlocks {
    public static final Block SEA_GLASS_BLOCK = register(
            "sea_glass_block",
            new Block(
                    BlockBehaviour.Properties.of()
                            .strength(0.3F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
            )
    );
    public static final Block FINE_SAND = register(
            "fine_sand",
            new Block(
                    BlockBehaviour.Properties.of()
                            .strength(0.3F)
                            .sound(SoundType.SAND)
            )
    );
    public static final Block LOAM = register(
            "loam",
            new Block(
                    BlockBehaviour.Properties.of()
                            .sound(SoundType.MUD)
            )
    );
    public static final LiquidBlock MANA = registerWithoutItem(
            "mana",
            new ManaLiquidBlock(
                    ModFluids.MANA,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
            )
    );
    private static <T extends Block> T registerWithoutItem(
            String name,
            T block
    ) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                Lapidary.MOD_ID,
                name
        );

        return Registry.register(
                BuiltInRegistries.BLOCK,
                id,
                block
        );
    }
    private ModBlocks() {
    }

    private static Block register(String name, Block block) {
        ResourceLocation id =
                ResourceLocation.fromNamespaceAndPath(Lapidary.MOD_ID, name);

        Registry.register(BuiltInRegistries.BLOCK, id, block);

        Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new BlockItem(block, new Item.Properties())
        );

        return block;
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS)
                .register(entries -> {
                    entries.accept(SEA_GLASS_BLOCK);
                    entries.accept(FINE_SAND);
                    entries.accept(LOAM);
                });
    }
}