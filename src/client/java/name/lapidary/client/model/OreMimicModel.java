package name.lapidary.client.model;

import name.lapidary.entity.OreMimicEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;

public final class OreMimicModel
        extends PlayerModel<OreMimicEntity> {

    public OreMimicModel(ModelPart root) {
        /*
         * false means standard-width player arms rather than the
         * narrow-arm player model.
         */
        super(root, false);

        /*
         * Disable the player's optional outer clothing layers.
         *
         * This leaves the exact underlying player-shaped body without
         * duplicate sleeves, trousers, jacket, or hat geometry.
         */
        this.hat.visible = false;
        this.leftSleeve.visible = false;
        this.rightSleeve.visible = false;
        this.leftPants.visible = false;
        this.rightPants.visible = false;
        this.jacket.visible = false;
    }

    @Override
    public void setupAnim(
            OreMimicEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        /*
         * Give an active mimic ordinary humanoid walking and attacking
         * animations.
         */
        super.setupAnim(
                entity,
                limbSwing,
                limbSwingAmount,
                ageInTicks,
                netHeadYaw,
                headPitch
        );

        if (entity.isHiding()) {
            applyPerfectlyStillPose();
        }
    }

    private void applyPerfectlyStillPose() {
        /*
         * Head faces directly forward.
         */
        this.head.xRot = 0.0F;
        this.head.yRot = 0.0F;
        this.head.zRot = 0.0F;

        /*
         * Torso remains square and upright.
         */
        this.body.xRot = 0.0F;
        this.body.yRot = 0.0F;
        this.body.zRot = 0.0F;

        /*
         * Arms hang perfectly straight.
         */
        this.rightArm.xRot = 0.0F;
        this.rightArm.yRot = 0.0F;
        this.rightArm.zRot = 0.0F;

        this.leftArm.xRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        this.leftArm.zRot = 0.0F;

        /*
         * Legs remain together in the ordinary standing position.
         */
        this.rightLeg.xRot = 0.0F;
        this.rightLeg.yRot = 0.0F;
        this.rightLeg.zRot = 0.0F;

        this.leftLeg.xRot = 0.0F;
        this.leftLeg.yRot = 0.0F;
        this.leftLeg.zRot = 0.0F;
    }
}