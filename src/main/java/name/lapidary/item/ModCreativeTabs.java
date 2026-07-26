package name.lapidary.item;

import name.lapidary.Lapidary;
import name.lapidary.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class ModCreativeTabs {

    public static final ResourceKey<CreativeModeTab> LAPIDARY_TAB_KEY =
            ResourceKey.create(
                    BuiltInRegistries.CREATIVE_MODE_TAB.key(),
                    ResourceLocation.fromNamespaceAndPath(
                            Lapidary.MOD_ID,
                            "lapidary"
                    )
            );

    public static CreativeModeTab LAPIDARY_TAB;

    private ModCreativeTabs() {
    }

    public static void initialize() {
        LAPIDARY_TAB =
                Registry.register(
                        BuiltInRegistries.CREATIVE_MODE_TAB,
                        LAPIDARY_TAB_KEY,
                        FabricItemGroup.builder()
                                .icon(() -> new ItemStack(ModItems.LAPIDARY_GUIDEBOOK))
                                .title(Component.translatable(
                                                "itemGroup.lapidary"
                                        ))
                                .displayItems((parameters, output) -> {
                                            Set<Item> addedItems = new HashSet<>();

                                            Consumer<ItemLike> add =
                                                    entry -> {
                                                        ItemStack stack =
                                                                new ItemStack(entry);

                                                        /*
                                                         * Blocks without an
                                                         * item form produce an
                                                         * empty stack.
                                                         */
                                                        if (stack.isEmpty()) {
                                                            Lapidary.LOGGER.warn(
                                                                    "Skipped empty creative-tab entry: {}",
                                                                    entry
                                                            );
                                                            return;
                                                        }

                                                        /*
                                                         * Prevent the same item
                                                         * from being accepted
                                                         * twice.
                                                         */
                                                        if (!addedItems.add(
                                                                stack.getItem()
                                                        )) {
                                                            Lapidary.LOGGER.warn(
                                                                    "Skipped duplicate creative-tab item: {}",
                                                                    stack.getItem()
                                                            );
                                                            return;
                                                        }

                                                        output.accept(stack);
                                                    };

                                            add.accept(ModItems.LAPIDARY_GUIDEBOOK);
                                            add.accept(ModItems.SEA_GLASS);
                                            add.accept(ModItems.FULGURITE);
                                            add.accept(ModItems.PURE_LAPIS);
                                            add.accept(ModItems.SIEVE);
                                            add.accept(ModItems.GOLD_PAN);
                                            add.accept(ModItems.GOLD_FLAKES);
                                            add.accept(ModItems.ELECTROSTATIC_MIX);
                                            add.accept(ModItems.MANA_BUCKET);
                                            add.accept(ModItems.NECKLACE_EMPTY);
                                            add.accept(ModItems.RING_EMPTY);
                                            add.accept(ModItems.RING_AMETHYST);
                                            add.accept(ModItems.RING_BISMUTH);
                                            add.accept(ModItems.RING_DIAMOND);
                                            add.accept(ModItems.RING_ENDER);
                                            add.accept(ModItems.RING_FULGURITE);
                                            add.accept(ModItems.RING_HEARTROOT);
                                            add.accept(ModItems.RING_LAPIS);
                                            add.accept(ModItems.RING_PEARL);
                                            add.accept(ModItems.RING_SEAGLASS);
                                            add.accept(ModItems.SABLE_FUR);
                                            add.accept(ModItems.GLOW_TROUT_BUCKET);
                                            add.accept(ModItems.BRIGHT_SALMON_BUCKET);
                                            add.accept(ModItems.AMEFYSH_BUCKET);
                                            add.accept(ModItems.MOLTEN_BISMUTH_BOTTLE);
                                            add.accept(ModBlocks.SEA_GLASS_BLOCK);
                                            add.accept(ModBlocks.FINE_SAND);
                                            add.accept(ModBlocks.LOAM);
                                            add.accept(ModBlocks.BISMUTH_BLOCK);
                                            add.accept(ModItems.SEA_GLASS_EMERALD);
                                            add.accept(ModItems.DIAMOND_EMERALD);
                                            add.accept(ModItems.FULGURITE_EMERALD);
                                            add.accept(ModItems.HEARTROOT_EMERALD);
                                            add.accept(ModItems.LAPIS_EMERALD);
                                            add.accept(ModBlocks.GEM_CUTTER);
                                            add.accept(ModItems.NECKLACE_SEA_GLASS);
                                            add.accept(ModItems.NECKLACE_AMETHYST);
                                            add.accept(ModItems.NECKLACE_BISMUTH);
                                            add.accept(ModItems.NECKLACE_DIAMOND);
                                            add.accept(ModItems.NECKLACE_ENDER);
                                            add.accept(ModItems.NECKLACE_FULGURITE);
                                            add.accept(ModItems.NECKLACE_HEARTROOT);
                                            add.accept(ModItems.NECKLACE_LAPIS);
                                            add.accept(ModItems.NECKLACE_PEARL);
                                            add.accept(ModBlocks.JEWELERS_TABLE);
                                            add.accept(ModBlocks.TOME_TABLE);
                                            add.accept(ModBlocks.CANISTER);
                                            add.accept(ModItems.MAGE_BACKPACK);
                                            add.accept(ModItems.STAFF);
                                            add.accept(ModItems.BISMUTH_SHARD);
                                            add.accept(ModItems.HEARTROOT);
                                            add.accept(ModItems.PEARL);
                                            add.accept(ModItems.FISH_STICK);
                                        }
                                )
                                .build()
                );

        Lapidary.LOGGER.info(
                "Registered Lapidary creative tab"
        );
    }
}