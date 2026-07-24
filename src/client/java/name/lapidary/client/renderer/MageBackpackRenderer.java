package name.lapidary.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
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

    /*
     * These affect only the backpack's position on the player.
     * They do not alter the actual Blockbench model.
     */
    private static final double OFFSET_X =
            0.0D;

    private static final double OFFSET_Y =
            0.42D;

    private static final double OFFSET_Z =
            0.22D;

    private static final float SCALE =
            0.72F;

    private static final float Y_ROTATION =
            180.0F;

    @Override
    public void render(
            ItemStack stack,
            SlotReference slotReference,
            EntityModel<? extends LivingEntity>
                    contextModel,
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
                instanceof PlayerModel<?>
                untypedPlayerModel)) {

            return;
        }

        if (!(entity
                instanceof AbstractClientPlayer
                clientPlayer)) {

            return;
        }

        /*
         * Trinkets requires PlayerModel<AbstractClientPlayer> exactly.
         *
         * We have already verified that this is a PlayerModel and that the
         * rendered entity is an AbstractClientPlayer, so this cast is safe
         * for the player-rendering context.
         */
        @SuppressWarnings("unchecked")
        PlayerModel<AbstractClientPlayer> playerModel =
                (PlayerModel<AbstractClientPlayer>)
                        untypedPlayerModel;

        poseStack.pushPose();

        TrinketRenderer.translateToChest(
                poseStack,
                playerModel,
                clientPlayer
        );

        poseStack.translate(
                OFFSET_X,
                OFFSET_Y,
                OFFSET_Z
        );

        poseStack.mulPose(
                Axis.YP.rotationDegrees(
                        Y_ROTATION
                )
        );

        /*
         * Item models and entity models use opposite Y/Z
         * orientation conventions.
         */
        poseStack.scale(
                SCALE,
                -SCALE,
                -SCALE
        );

        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(
                        stack,
                        ItemDisplayContext.NONE,
                        light,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        bufferSource,
                        entity.level(),
                        entity.getId()
                );

        poseStack.popPose();
    }
}