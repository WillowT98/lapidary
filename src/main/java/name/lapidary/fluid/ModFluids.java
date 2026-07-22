package name.lapidary.fluid;

import name.lapidary.Lapidary;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FlowingFluid;

public final class ModFluids {

    /*
     * Minecraft represents a flowing liquid using two registered fluids:
     *
     * MANA         = a full source block
     * FLOWING_MANA = every non-source flow level
     */
    public static final FlowingFluid MANA = register(
            "mana",
            new MoltenBismuthFluid.Source()
    );

    public static final FlowingFluid FLOWING_MANA = register(
            "flowing_mana",
            new MoltenBismuthFluid.Flowing()
    );
    public static final FlowingFluid FLOWING_MOLTEN_BISMUTH =
            register(
                    "flowing_molten_bismuth",
                    new MoltenBismuthFluid.Flowing()
            );

    public static final FlowingFluid MOLTEN_BISMUTH =
            register(
                    "molten_bismuth",
                    new MoltenBismuthFluid.Source()
            );

    private ModFluids() {
    }

    private static FlowingFluid register(
            String name,
            FlowingFluid fluid
    ) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                Lapidary.MOD_ID,
                name
        );

        return Registry.register(
                BuiltInRegistries.FLUID,
                id,
                fluid
        );
    }

    public static void initialize() {
        Lapidary.LOGGER.info("Registering Lapidary fluids");
    }
}