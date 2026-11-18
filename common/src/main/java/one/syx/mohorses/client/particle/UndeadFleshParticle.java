package one.syx.mohorses.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/// A bit of rotten flesh dropping off a zombie horse: it tumbles to the ground, lies there for a moment and is gone
public class UndeadFleshParticle extends SingleQuadParticle {
    private int groundTicks;

    protected UndeadFleshParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);

        this.gravity = 0.9F;
        this.lifetime = 120;
        this.quadSize = 0.06F + this.random.nextFloat() * 0.04F;
        this.xd = (this.random.nextDouble() - 0.5) * 0.04;
        this.yd = 0.02;
        this.zd = (this.random.nextDouble() - 0.5) * 0.04;
        this.roll = this.random.nextFloat() * (float)Math.PI * 2.0F;
        this.oRoll = this.roll;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.onGround) {
            this.xd = 0.0;
            this.zd = 0.0;

            if (++this.groundTicks > 30)
                remove();
        }
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new UndeadFleshParticle(level, x, y, z, this.sprites.get(random));
        }
    }
}
