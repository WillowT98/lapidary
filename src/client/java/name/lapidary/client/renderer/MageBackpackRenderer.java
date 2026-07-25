package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import name.lapidary.block.ModBlocks;
import name.lapidary.fluid.CanisterItemContents;
import name.lapidary.item.MageBackpackItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class MageBackpackRenderer
        implements TrinketRenderer {

    private static final double BACKPACK_OFFSET_X =
            0.0D;

    private static final double BACKPACK_OFFSET_Y =
            0.42D;

    private static final double BACKPACK_OFFSET_Z =
            0.22D;

    private static final float BACKPACK_SCALE =
            0.72F;

    private static final float BACKPACK_Y_ROTATION =
            180.0F;

    /*
     * These four values control the mounted canister position.
     *
     * The model and its liquid are transformed together.
     */
    private static final double CANISTER_OFFSET_X =
            0.0D;

    private static final double CANISTER_OFFSET_Y =
            0.285D;

    private static final double CANISTER_OFFSET_Z =
            0.409D;

    private static final float CANISTER_SCALE =
            0.58F;

    private static final float CANISTER_Y_ROTATION =
            180.0F;

    /*
     * Rotates the originally upright canister onto its side.
     * Changing this to -90 reverses which end points left.
     */
    private static final float CANISTER_SIDEWAYS_ROTATION =
            90.0F;

    @Override
    public void render(
            ItemStack backpackStack,
            SlotReference slotReference,
            EntityModel contextModel,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            LivingEntity entity,
            float limbAngle,
            float limbDistance,
            float tickDelta,
            float animationProgress,
            float headYaw,
            float headPitch
    ) {
        if (!(contextModel
                instanceof PlayerModel
                untypedPlayerModel)) {

            return;
        }

        if (!(entity
                instanceof AbstractClientPlayer
                clientPlayer)) {

            return;
        }

        @SuppressWarnings("unchecked")
        PlayerModel<AbstractClientPlayer>
                playerModel =
                (PlayerModel<AbstractClientPlayer>)
                        untypedPlayerModel;

        renderBackpack(
                backpackStack,
                playerModel,
                clientPlayer,
                poseStack,
                bufferSource,
                light
        );

        ItemStack mountedCanister =
                MageBackpackItem
                        .getMountedCanister(
                                backpackStack
                        );

        if (!mountedCanister.isEmpty()) {
            renderMountedCanister(
                    mountedCanister,
                    playerModel,
                    clientPlayer,
                    poseStack,
                    bufferSource,
                    light
            );
        }
    }

    private static void renderBackpack(
            ItemStack backpackStack,
            PlayerModel<AbstractClientPlayer>
                    playerModel,
            AbstractClientPlayer player,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light
    ) {
        poseStack.pushPose();

        TrinketRenderer.translateToChest(
                poseStack,
                playerModel,
                player
        );

        poseStack.translate(
                BACKPACK_OFFSET_X,
                BACKPACK_OFFSET_Y,
                BACKPACK_OFFSET_Z
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        BACKPACK_Y_ROTATION
                )
        );

        poseStack.scale(
                BACKPACK_SCALE,
                -BACKPACK_SCALE,
                -BACKPACK_SCALE
        );

        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(
                        backpackStack,
                        ItemDisplayContext.NONE,
                        light,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        bufferSource,
                        player.level(),
                        player.getId()
                );

        poseStack.popPose();
    }

    private static void renderMountedCanister(
            ItemStack canisterStack,
            PlayerModel<AbstractClientPlayer>
                    playerModel,
            AbstractClientPlayer player,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light
    ) {
        poseStack.pushPose();

        TrinketRenderer.translateToChest(
                poseStack,
                playerModel,
                player
        );

        poseStack.translate(
                CANISTER_OFFSET_X,
                CANISTER_OFFSET_Y,
                CANISTER_OFFSET_Z
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        CANISTER_Y_ROTATION
                )
        );

        /*
         * The block model and the liquid geometry receive this
         * same rotation. The liquid therefore remains a rigid,
         * sideways version of its ordinary upright fill rather
         * than responding to simulated gravity.
         */
        poseStack.mulPose(
                Axis.ZP.rotationDegrees(
                        CANISTER_SIDEWAYS_ROTATION
                )
        );

        poseStack.scale(
                CANISTER_SCALE,
                -CANISTER_SCALE,
                -CANISTER_SCALE
        );

        /*
         * The block model occupies 0–1 on each axis. Center it on
         * the mount transform before rendering.
         */
        poseStack.translate(
                -0.5D,
                -0.5D,
                -0.5D
        );

        /*
         * Render the canister frame and glass using its ordinary
         * block model.
         */
        Minecraft.getInstance()
                .getBlockRenderer()
                .renderSingleBlock(
                        ModBlocks.CANISTER
                                .defaultBlockState(),
                        poseStack,
                        bufferSource,
                        light,
                        OverlayTexture.NO_OVERLAY
                );

        /*
         * Read and render the exact liquid type and amount stored
         * on this particular canister ItemStack.
         */
        CanisterItemContents.Contents contents =
                CanisterItemContents.read(
                        canisterStack
                );

        if (!contents.isEmpty()) {
            CanisterLiquidRenderer.render(
                    contents.liquid(),
                    contents.amount(),
                    player.level(),
                    player.blockPosition(),
                    poseStack,
                    bufferSource,
                    light,
                    OverlayTexture.NO_OVERLAY
            );
        }

        poseStack.popPose();
    }
}