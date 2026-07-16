package name.lapidary.client;

import name.lapidary.block.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public class LapidaryClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(
				ModBlocks.SEA_GLASS_BLOCK,
				RenderType.translucent()
		);
	}
}