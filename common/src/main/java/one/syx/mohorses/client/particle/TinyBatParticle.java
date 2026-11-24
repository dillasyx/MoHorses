package one.syx.mohorses.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/// A tiny bat that flutters off from a Bat Horse, zigzagging upwards
public class TinyBatParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private double heading;

    protected TinyBatParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());

        this.sprites = sprites;
        this.gravity = 0.0F;
        this.friction = 0.9F;
        this.hasPhysics = false;
        this.lifetime = 35 + this.random.nextInt(25);
        this.quadSize = 0.12F + this.random.nextFloat() * 0.05F;
        this.heading = this.random.nextDouble() * Math.PI * 2.0;
        this.xd = 0.0;
        this.yd = 0.03;
        this.zd = 0.0;
    }

    @Override
    public void tick() {
        super.tick();

        // A bat never flies straight on
        if (this.random.nextInt(6) == 0)
            this.heading += (this.random.nextDouble() - 0.5) * 2.4;

        this.xd += Math.cos(this.heading) * 0.013;
        this.zd += Math.sin(this.heading) * 0.013;
        this.yd += 0.004 + Mth.sin(this.age * 0.9F) * 0.008;

        // Two pictures: wings up, wings down
        setSprite(this.sprites.get(this.age / 2 % 2, 1));
        setAlpha(Mth.clamp((this.lifetime - this.age) / 10.0F, 0.0F, 1.0F));
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
            return new TinyBatParticle(level, x, y, z, this.sprites);
        }
    }
}
