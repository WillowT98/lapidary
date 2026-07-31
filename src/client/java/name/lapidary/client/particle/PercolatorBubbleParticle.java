package name.lapidary.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * A rising bubble that deliberately does not require a real water FluidState.
 *
 * The mana percolator stores its chamber contents in block-entity data and
 * renders them visually, so vanilla BubbleParticle behavior would immediately
 * remove itself after discovering that the block position is not water.
 */
public final class PercolatorBubbleParticle
        extends TextureSheetParticle {

    private static final double CHAMBER_TOP_OFFSET =
            11.55D / 16.0D;

    private final SpriteSet sprites;
    private final double maximumY;
    private final double wobblePhase;

    private PercolatorBubbleParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            SpriteSet sprites
    ) {
        super(
                level,
                x,
                y,
                z,
                velocityX,
                velocityY,
                velocityZ
        );

        this.sprites = sprites;
        this.maximumY =
                y + 9.0D / 16.0D;
        this.wobblePhase =
                level.random.nextDouble()
                        * Math.PI
                        * 2.0D;

        /*
         * The superclass adds a little random motion. Replace it with the
         * carefully bounded motion supplied by the percolator renderer.
         */
        this.xd = velocityX;
        this.yd = Math.max(0.018D, velocityY);
        this.zd = velocityZ;

        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.friction = 0.96F;
        this.lifetime =
                24 + level.random.nextInt(13);

        /*
         * Start visibly rather than relying on the first client particle tick
         * to raise the alpha from zero.
         */
        this.quadSize =
                0.11F
                        + level.random.nextFloat()
                        * 0.05F;

        this.alpha = 1.0F;
        this.setColor(
                0.92F,
                0.97F,
                1.0F
        );
        this.pickSprite(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime
                || this.y >= this.maximumY) {

            this.remove();
            return;
        }

        double wobble =
                Math.sin(
                        this.wobblePhase
                                + this.age * 0.48D
                ) * 0.0014D;

        this.move(
                this.xd + wobble,
                this.yd,
                this.zd - wobble
        );

        this.xd *= 0.95D;
        this.zd *= 0.95D;
        this.yd =
                Math.min(
                        0.034D,
                        this.yd + 0.00065D
                );

        this.alpha = 200.0F;
        /*
        float progress =
                (float) this.age
                        / (float) this.lifetime;

        if (progress < 0.14F) {
            this.alpha =
                    0.88F
                            * progress
                            / 0.14F;
        } else if (progress > 0.78F) {
            this.alpha =
                    0.88F
                            * (1.0F - progress)
                            / 0.22F;
        } else {
            this.alpha = 0.88F;
        }*/

        this.setSpriteFromAge(this.sprites);
    }

    @Override
    protected int getLightColor(
            float partialTick
    ) {
        /*
         * Keep the thin vanilla bubble outline readable through tinted glass
         * and the deliberately dim chamber-side fluid layer.
         */
        return 0x00F000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static final class Provider
            implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(
                SpriteSet sprites
        ) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double velocityX,
                double velocityY,
                double velocityZ
        ) {
            return new PercolatorBubbleParticle(
                    level,
                    x,
                    y,
                    z,
                    velocityX,
                    velocityY,
                    velocityZ,
                    sprites
            );
        }
    }
}
