package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import name.lapidary.block.entity.CanisterBlockEntity;
import name.lapidary.fluid.CanisterFluidStorage;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class CanisterBlockEntityRenderer
        implements BlockEntityRenderer<
        CanisterBlockEntity
        > {

    public CanisterBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            CanisterBlockEntity canister,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        CanisterFluidStorage storage =
                canister.getStorage();

        if (storage.isEmpty()
                || canister.getLevel() == null) {

            return;
        }

        CanisterLiquidRenderer.render(
                storage.getLiquid(),
                storage.getAmount(),
                canister.getLevel(),
                canister.getBlockPos(),
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );
    }
}