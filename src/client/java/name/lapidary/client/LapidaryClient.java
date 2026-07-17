package name.lapidary.client;

import name.lapidary.block.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

import name.lapidary.fluid.ModFluids;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;

public class LapidaryClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
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
	}
}