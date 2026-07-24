package name.lapidary.fluid;

import name.lapidary.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Locale;

public enum CanisterLiquid {

    WATER(
            ResourceLocation.withDefaultNamespace(
                    "water"
            )
    ),

    LAVA(
            ResourceLocation.withDefaultNamespace(
                    "lava"
            )
    ),

    MANA(
            ResourceLocation.fromNamespaceAndPath(
                    "lapidary",
                    "mana"
            )
    ),

    /*
     * Milk is not a placed vanilla Fluid, so it needs its own
     * logical canister identity. It uses water's texture when
     * rendered, but is otherwise treated as a distinct liquid.
     */
    MILK(
            ResourceLocation.withDefaultNamespace(
                    "milk"
            )
    );

    private final ResourceLocation id;

    CanisterLiquid(
            ResourceLocation id
    ) {
        this.id = id;
    }

    public ResourceLocation id() {
        return id;
    }

    public Component displayName() {
        return Component.translatable(
                "liquid.lapidary."
                        + name()
                        .toLowerCase(Locale.ROOT)
        );
    }

    /**
     * Bucket item returned when this liquid is withdrawn.
     */
    public Item filledBucketItem() {
        return switch (this) {
            case WATER ->
                    Items.WATER_BUCKET;

            case LAVA ->
                    Items.LAVA_BUCKET;

            case MANA ->
                    ModItems.MANA_BUCKET;

            case MILK ->
                    Items.MILK_BUCKET;
        };
    }

    /**
     * Fluid whose animated sprite should be used by the renderer.
     *
     * Milk borrows water's animated sprite and is tinted white.
     */
    public Fluid renderFluid() {
        return switch (this) {
            case WATER ->
                    Fluids.WATER;

            case LAVA ->
                    Fluids.LAVA;

            case MANA ->
                    ModFluids.MANA;

            case MILK ->
                    Fluids.WATER;
        };
    }

    public boolean usesWhiteRenderTint() {
        return this == MILK;
    }

    public boolean usesLavaSounds() {
        return this == LAVA;
    }

    /**
     * Returns the liquid represented by a filled bucket, or null
     * when the item is not one of the supported buckets.
     */
    public static CanisterLiquid fromFilledBucket(
            ItemStack stack
    ) {
        if (stack.is(Items.WATER_BUCKET)) {
            return WATER;
        }

        if (stack.is(Items.LAVA_BUCKET)) {
            return LAVA;
        }

        if (stack.is(ModItems.MANA_BUCKET)) {
            return MANA;
        }

        if (stack.is(Items.MILK_BUCKET)) {
            return MILK;
        }

        return null;
    }

    public static CanisterLiquid byId(
            String idString
    ) {
        ResourceLocation parsed =
                ResourceLocation.tryParse(
                        idString
                );

        if (parsed == null) {
            return null;
        }

        for (CanisterLiquid liquid : values()) {
            if (liquid.id.equals(parsed)) {
                return liquid;
            }
        }

        return null;
    }
}