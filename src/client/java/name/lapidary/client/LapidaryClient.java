package name.lapidary.client;

import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import name.lapidary.client.hud.OriginHud;
import name.lapidary.client.input.MageBackpackKeybinds;
import name.lapidary.client.input.OriginKeybinds;
import name.lapidary.client.renderer.*;
import name.lapidary.client.screen.GemCutterScreen;
import name.lapidary.block.entity.ModBlockEntities;
import name.lapidary.client.hud.InsightHud;
import name.lapidary.client.network.ClientNetworking;
import name.lapidary.block.ModBlocks;
import name.lapidary.item.ModItems;
import name.lapidary.screen.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import name.lapidary.client.screen.JewelersTableScreen;
import net.minecraft.client.renderer.RenderType;

import name.lapidary.fluid.ModFluids;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;

import name.lapidary.entity.ModEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

import name.lapidary.client.screen.MageBackpackScreen;
import name.lapidary.client.screen.StainedGlassFabricatorScreen;

public class LapidaryClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientNetworking.initialize();
		InsightHud.initialize();
		OriginKeybinds.initialize();
		OriginHud.initialize();


		BlockRenderLayerMap.INSTANCE.putBlock(
				ModBlocks.REINFORCED_GLASS,
				RenderType.translucent()
		);
		BlockRenderLayerMap.INSTANCE.putBlock(
				ModBlocks.HARD_LIGHT_BLOCK,
				RenderType.translucent()
		);

		BlockRenderLayerMap.INSTANCE.putBlock(
				ModBlocks.SEA_GLASS_BLOCK,
				RenderType.translucent()
		);
		FluidRenderHandlerRegistry.INSTANCE.register(
				ModFluids.MANA,
				ModFluids.FLOWING_MANA,
				SimpleFluidRenderHandler.coloredWater(0x0AFBEA)
		);
		FluidRenderHandlerRegistry.INSTANCE.register(
				ModFluids.MOLTEN_BISMUTH,
				ModFluids.FLOWING_MOLTEN_BISMUTH,
				SimpleFluidRenderHandler.coloredWater(0x353535)
		);

		BlockRenderLayerMap.INSTANCE.putFluids(
				RenderType.translucent(),
				ModFluids.MANA,
				ModFluids.FLOWING_MANA
		);
		BlockRenderLayerMap.INSTANCE.putFluids(
				RenderType.translucent(),
				ModFluids.MOLTEN_BISMUTH,
				ModFluids.FLOWING_MOLTEN_BISMUTH
		);
		EntityRendererRegistry.register(
				ModEntities.ORE_MIMIC,
				OreMimicRenderer::new
		);
		EntityRendererRegistry.register(
				ModEntities.SABLE,
				FoxRenderer::new
		);
		EntityRendererRegistry.register(
				ModEntities.GLOW_TROUT,
				GlowTroutRenderer::new
		);

		EntityRendererRegistry.register(
				ModEntities.BRIGHT_SALMON,
				BrightSalmonRenderer::new
		);
		BlockRenderLayerMap.INSTANCE.putBlock(
				ModBlocks.SABLE_CACHE,
				RenderType.cutout()
		);
		EntityRendererRegistry.register(
				ModEntities.AMEFYSH,
				AmefyshRenderer::new
		);
		EntityRendererRegistry.register(
				ModEntities.THROWN_MOLTEN_BISMUTH,
				ThrownItemRenderer::new
		);
		EntityRendererRegistry.register(
				ModEntities.MAGE_LIGHT,
				ThrownItemRenderer::new
		);
		MenuScreens.register(
				ModMenus.GEM_CUTTER,
				GemCutterScreen::new
		);
		MenuScreens.register(
				ModMenus.JEWELERS_TABLE,
				JewelersTableScreen::new
		);
		BlockRenderLayerMap.INSTANCE.putBlock(
				ModBlocks.GEM_CUTTER,
				RenderType.cutout()
		);
		BlockRenderLayerMap.INSTANCE.putBlock(
				ModBlocks.CANISTER,
				RenderType.translucent()
		);
		BlockEntityRendererRegistry.register(
				ModBlockEntities.CANISTER,
				CanisterBlockEntityRenderer::new
		);
		BlockEntityRendererRegistry.register(
				ModBlockEntities.MANA_PERCOLATOR,
				ManaPercolatorBlockEntityRenderer::new
		);
		BlockRenderLayerMap.INSTANCE.putBlock(
				ModBlocks.MANA_PERCOLATOR,
				RenderType.translucent()
		);

		BlockEntityRendererRegistry.register(
				ModBlockEntities.CUSTOM_WINDOW_CONTROLLER,
				CustomWindowRenderer::new
		);

		MageBackpackKeybinds.initialize();

		TrinketRendererRegistry.registerRenderer(
				ModItems.MAGE_BACKPACK,
				new MageBackpackRenderer()
		);
		MenuScreens.register(
				ModMenus.MAGE_BACKPACK,
				MageBackpackScreen::new
		);
		MenuScreens.register(
				ModMenus.STAINED_GLASS_FABRICATOR,
				StainedGlassFabricatorScreen::new
		);

	}
}