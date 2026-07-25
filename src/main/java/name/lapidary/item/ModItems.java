package name.lapidary.item;

import name.lapidary.Lapidary;
import name.lapidary.entity.ModEntities;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;

import name.lapidary.fluid.ModFluids;
import net.minecraft.world.level.material.Fluids;

public final class ModItems {
    public static final Item SEA_GLASS = register(
            "sea_glass",
            new Item(new Item.Properties())
    );
    public static final Item SEA_GLASS_EMERALD = register(
            "sea_glass_emerald",
            new Item(new Item.Properties())
    );
    public static final Item FULGURITE = register(
            "fulgurite",
            new Item(new Item.Properties())
    );
    public static final Item PURE_LAPIS = register(
            "pure_lapis",
            new Item(new Item.Properties())
    );
    public static final Item GOLD_FLAKES = register(
            "gold_flakes",
            new Item(new Item.Properties())
    );
    public static final Item ELECTROSTATIC_MIX = register(
            "electrostatic_mix",
            new Item(new Item.Properties())
    );
    public static final Item NECKLACE_EMPTY = register(
            "necklace_empty",
            new Item(new Item.Properties())
    );
    public static final Item NECKLACE_SEA_GLASS = register(
            "necklace_sea_glass",
            new Item(new Item.Properties())
    );
    public static final Item RING_EMPTY = register(
            "ring_empty",
            new Item(new Item.Properties().stacksTo(1))
    );
    public static final Item SIEVE = register(
            "sieve",
            new SieveItem(
                    new Item.Properties()
                            .durability(128)
            )
    );
    public static final Item GOLD_PAN = register(
            "gold_pan",
            new GoldPanItem(
                    new Item.Properties()
                            .durability(128)
            )
    );
    public static final Item MANA_BUCKET = register(
            "mana_bucket",
            new BucketItem(
                    ModFluids.MANA,
                    new Item.Properties()
                            .craftRemainder(Items.BUCKET)
                            .stacksTo(1)
            )
    );
    public static final Item SABLE_FUR = register(
            "sable_fur",
            new Item(new Item.Properties())
    );

    public static final Item GLOW_TROUT_BUCKET =
            register(
                    "glow_trout_bucket",
                    new MobBucketItem(
                            ModEntities.GLOW_TROUT,
                            Fluids.WATER,
                            SoundEvents.BUCKET_EMPTY_FISH,
                            new Item.Properties()
                                    .stacksTo(1)
                    )
            );

    public static final Item BRIGHT_SALMON_BUCKET =
            register(
                    "bright_salmon_bucket",
                    new MobBucketItem(
                            ModEntities.BRIGHT_SALMON,
                            Fluids.WATER,
                            SoundEvents.BUCKET_EMPTY_FISH,
                            new Item.Properties()
                                    .stacksTo(1)
                    )
            );
    public static final Item AMEFYSH_BUCKET =
            register(
                    "amefysh_bucket",
                    new MobBucketItem(
                            ModEntities.AMEFYSH,
                            Fluids.WATER,
                            SoundEvents.BUCKET_EMPTY_FISH,
                            new Item.Properties()
                                    .stacksTo(1)
                    )
            );
    public static final Item MOLTEN_BISMUTH_BOTTLE =
            register(
                    "molten_bismuth_bottle",
                    new MoltenBismuthBottleItem(
                            new Item.Properties()
                                    .stacksTo(16)
                    )
            );
    public static final Item MAGE_BACKPACK =
            register(
                    "mage_backpack",
                    new MageBackpackItem(
                            new Item.Properties()
                                    .stacksTo(1)
                    )
            );
    public static final Item LAPIDARY_GUIDEBOOK = register(
            "lapidary_guidebook",
            new LapidaryGuidebookItem(
                    new Item.Properties()
                            .stacksTo(1)
            )
    );
    public static final Item STAFF =
            register(
                    "staff",
                    new StaffItem(
                            new Item.Properties()
                                    .stacksTo(1)
                    )
            );

    private ModItems() {
        // Prevent this utility class from being instantiated.
    }

    private static Item register(String name, Item item) {
        ResourceLocation id =
                ResourceLocation.fromNamespaceAndPath(Lapidary.MOD_ID, name);

        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    public static void initialize() {
        /*ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> {
                    entries.accept(SEA_GLASS);
                    entries.accept(FULGURITE);
                    entries.accept(PURE_LAPIS);
                    entries.accept(SIEVE);
                    entries.accept(GOLD_PAN);
                    entries.accept(GOLD_FLAKES);
                    entries.accept(ELECTROSTATIC_MIX);
                    entries.accept(MANA_BUCKET);
                    entries.accept(NECKLACE_EMPTY);
                    entries.accept(RING_EMPTY);
                    entries.accept(SABLE_FUR);
                    entries.accept(GLOW_TROUT_BUCKET);
                    entries.accept(BRIGHT_SALMON_BUCKET);
                    entries.accept(AMEFYSH_BUCKET);
                    entries.accept(MOLTEN_BISMUTH_BOTTLE);
                });*/
    }
}