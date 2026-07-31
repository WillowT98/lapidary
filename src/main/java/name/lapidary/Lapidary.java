package name.lapidary;

import name.lapidary.block.ModBlocks;
import name.lapidary.block.entity.ModBlockEntities;
import name.lapidary.command.LapidaryCommands;
import name.lapidary.entity.ModEntities;
import name.lapidary.fluid.ModFluids;
import name.lapidary.guide.GuidebookProgression;
import name.lapidary.item.ModCreativeTabs;
import name.lapidary.item.ModItems;
import name.lapidary.magic.spell.ModSpells;
import name.lapidary.magic.spell.SpellRuntime;
import name.lapidary.network.ModNetworking;
import name.lapidary.origin.OriginManager;
import name.lapidary.particle.ModParticles;
import name.lapidary.progression.ModAttachments;
import name.lapidary.screen.ModMenus;
import name.lapidary.sifting.SieveProcessing;
import name.lapidary.world.ModWorldGeneration;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lapidary implements ModInitializer {

    public static final String MOD_ID = "lapidary";
    public static final Logger LOGGER =
            LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");

        ModAttachments.initialize();
        ModSpells.initialize();
        ModNetworking.initialize();
        ModFluids.initialize();
        ModItems.initialize();
        ModBlocks.initialize();
        ModParticles.initialize();
        SpellRuntime.initialize();
        ModBlockEntities.initialize();
        ModEntities.initialize();
        ModWorldGeneration.initialize();
        SieveProcessing.initialize();
        LapidaryCommands.initialize();
        ModCreativeTabs.initialize();
        ModMenus.initialize();
        OriginManager.initialize();
        GuidebookProgression.initialize();
    }

    public static ResourceLocation id(
            String path
    ) {
        return ResourceLocation.fromNamespaceAndPath(
                MOD_ID,
                path
        );
    }
}
