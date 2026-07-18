package name.lapidary.client;

import name.lapidary.block.entity.ModBlockEntities;
import name.lapidary.client.hud.InsightHud;
import name.lapidary.client.network.ClientNetworking;
import name.lapidary.block.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;

import name.lapidary.fluid.ModFluids;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;

import name.lapidary.client.renderer.OreMimicRenderer;
import name.lapidary.entity.ModEntities;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;

public class LapidaryClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientNetworking.initialize();
		InsightHud.initialize();

		BlockRenderLayerMap.INSTANCE.putBlock(
				ModBlocks.SEA_GLASS_BLOCK,
				RenderType.translucent()
		);
		FluidRenderHandlerRegistry.INSTANCE.register(
				ModFluids.MANA,
				ModFluids.FLOWING_MANA,
				SimpleFluidRenderHandler.coloredWater(0x0AFBEA)
		);

		BlockRenderLayerMap.INSTANCE.putFluids(
				RenderType.translucent(),
				ModFluids.MANA,
				ModFluids.FLOWING_MANA
		);
		EntityRendererRegistry.register(
				ModEntities.ORE_MIMIC,
				OreMimicRenderer::new
		);
		EntityRendererRegistry.register(
				ModEntities.SABLE,
				FoxRenderer::new
		);
		BlockEntityRendererRegistry.register(
				ModBlockEntities.SABLE_CACHE,
				ChestRenderer::new
		);
	}
}