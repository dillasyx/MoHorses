package one.syx.mohorses.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/// A feather a Pegasus loses: it rocks down to the ground from side to side, lies there for a moment and fades
public class FeatherParticle extends SingleQuadParticle {
    private final double swayX;
    private final double swayZ;
    private final float swing;
    private final float lean;

    protected FeatherParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);

        this.gravity = 0.06F;
        this.friction = 0.9F;
        this.lifetime = 90 + this.random.nextInt(40);
        this.quadSize = 0.1F + this.random.nextFloat() * 0.04F;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        double direction = this.random.nextDouble() * Math.PI * 2.0;

        this.swayX = Math.cos(direction) * 0.012;
        this.swayZ = Math.sin(direction) * 0.012;
        this.swing = this.random.nextFloat() * Mth.TWO_PI;
        this.lean = (this.random.nextFloat() - 0.5F) * 2.0F;
        this.roll = this.lean;
        this.oRoll = this.roll;
    }

    @Override
    public void tick() {
        super.tick();

        this.oRoll = this.roll;

        if (this.onGround) {
            this.xd = 0.0;
            this.zd = 0.0;
        }
        else {
            float beat = this.swing + this.age * 0.16F;

            this.xd += this.swayX * Mth.cos(beat);
            this.zd += this.swayZ * Mth.cos(beat);
            this.roll = this.lean + Mth.sin(beat) * 0.6F;
        }

        setAlpha(Mth.clamp((this.lifetime - this.age) / 20.0F, 0.0F, 1.0F));
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new FeatherParticle(level, x, y, z, xAux, yAux, zAux, this.sprites.get(random));
        }
    }
}
