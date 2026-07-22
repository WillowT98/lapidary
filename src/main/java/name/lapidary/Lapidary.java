package name.lapidary;

import name.lapidary.block.entity.ModBlockEntities;
import name.lapidary.fluid.ModFluids;
import name.lapidary.item.ModCreativeTabs;
import name.lapidary.sifting.SieveProcessing;
import name.lapidary.block.ModBlocks;
import name.lapidary.item.ModItems;
import name.lapidary.world.ModWorldGeneration;
import net.fabricmc.api.ModInitializer;
import name.lapidary.command.LapidaryCommands;
import name.lapidary.network.ModNetworking;
import name.lapidary.progression.ModAttachments;
import net.minecraft.resources.ResourceLocation;
import name.lapidary.entity.ModEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lapidary implements ModInitializer {
	public static final String MOD_ID = "lapidary";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		ModAttachments.initialize();
		ModNetworking.initialize();
		ModFluids.initialize();
		ModItems.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModEntities.initialize();
		ModWorldGeneration.initialize();
		SieveProcessing.initialize();
		LapidaryCommands.initialize();
		ModCreativeTabs.initialize();
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
