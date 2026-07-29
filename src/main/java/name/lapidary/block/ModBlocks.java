package name.lapidary.block;

import name.lapidary.Lapidary;
import name.lapidary.fluid.ModFluids;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

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
    public static final Block BISMUTH_BLOCK = register(
            "bismuth_block",
            new BismuthBlock(
                    BlockBehaviour.Properties
                            .ofFullCopy(Blocks.IRON_ORE)
                            .sound(SoundType.COPPER)
            )
    );
    public static final Block GEM_CUTTER = register(
            "gem_cutter",
            new GemCutterBlock(
                    BlockBehaviour.Properties
                            .ofFullCopy(Blocks.STONECUTTER)
                            .noOcclusion()
            )
    );
    public static final Block JEWELERS_TABLE = register(
            "jewelers_table",
            new JewelersTableBlock(
                    BlockBehaviour.Properties
                            .ofFullCopy(Blocks.CRAFTING_TABLE)
                            .noOcclusion()
            )
    );
    public static final LiquidBlock MANA = registerWithoutItem(
            "mana",
            new ManaLiquidBlock(
                    ModFluids.MANA,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
            )
    );
    public static final Block SABLE_CACHE = registerBlockOnly(
            "sable_cache",
            new SableCacheBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.6F)
                            .sound(SoundType.PACKED_MUD)
                            .noOcclusion()
            )
    );
    public static final Block MOLTEN_BISMUTH = registerBlockOnly(
            "molten_bismuth",
            new MoltenBismuthLiquidBlock(
                    ModFluids.MOLTEN_BISMUTH,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.LAVA)
                            .lightLevel(state -> 3)
            )
    );
    public static final Block TOME_TABLE = register(
            "tome_table",
            new TomeTableBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
            )
    );
    public static final Block CANISTER = register(
            "canister",
            new CanisterBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
            ),
            new Item.Properties().stacksTo(1)
    );

    public static final Block MANA_PERCOLATOR = register(
            "mana_percolator",
            new ManaPercolatorBlock(
                    BlockBehaviour.Properties
                            .ofFullCopy(Blocks.CAULDRON)
                            .strength(2.0F)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .pushReaction(PushReaction.BLOCK)
            )
    );


    public static final Block STAINED_GLASS_FABRICATOR = register(
            "stained_glass_fabricator",
            new StainedGlassFabricatorBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
            )
    );
    public static final Block CUSTOM_WINDOW_CONTROLLER = registerBlockOnly(
            "custom_window_controller",
            new CustomWindowControllerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.3F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .pushReaction(PushReaction.BLOCK)
            )
    );
    public static final Block CUSTOM_WINDOW_SEGMENT = registerBlockOnly(
            "custom_window_segment",
            new CustomWindowSegmentBlock(
                    BlockBehaviour.Properties.of()
                            .strength(0.3F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .pushReaction(PushReaction.BLOCK)
            )
    );

    public static final Block REINFORCED_GLASS = register(
            "reinforced_glass",
            new ReinforcedGlassBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                            .strength(0.5F, 1200.0F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
            )
    );

    public static final Block HARD_LIGHT_BLOCK = registerBlockOnly(
            "hard_light_block",
            new HardLightBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                            .strength(0.25F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 10)
                            .noOcclusion()
            )
    );

    public static final Block FROSTED_OBSIDIAN = registerBlockOnly(
            "frosted_obsidian",
            new FrostedObsidianBlock(
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN)
                            .strength(50.0F, 1200.0F)
                            .sound(SoundType.STONE)
            )
    );

    private static Block registerBlockOnly(String name, Block block) {
        return Registry.register(
                BuiltInRegistries.BLOCK,
                Lapidary.id(name),
                block
        );
    }

    private static <T extends Block> T registerWithoutItem(
            String name,
            T block
    ) {
        return Registry.register(
                BuiltInRegistries.BLOCK,
                Lapidary.id(name),
                block
        );
    }

    private ModBlocks() {
    }

    private static Block register(String name, Block block) {
        return register(name, block, new Item.Properties());
    }

    private static Block register(
            String name,
            Block block,
            Item.Properties itemProperties
    ) {
        ResourceLocation id = Lapidary.id(name);
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new BlockItem(block, itemProperties)
        );
        return block;
    }

    public static void initialize() {
    }
}
