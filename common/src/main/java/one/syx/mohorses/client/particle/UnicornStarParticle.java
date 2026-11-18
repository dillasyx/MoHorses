package one.syx.mohorses.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/// The little stars a unicorn leaves behind in the air: they glow, drift down slowly,
/// shrink and fade. Each one is white with a faint pastel tint.
public class UnicornStarParticle extends SingleQuadParticle {
    private static final float[][] TINTS = {
            {1.0F, 1.0F, 1.0F}, {1.0F, 0.85F, 0.95F}, {0.85F, 0.92F, 1.0F}, {1.0F, 0.97F, 0.8F}, {0.92F, 0.85F, 1.0F}
    };

    private final float startSize;

    protected UnicornStarParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);

        this.gravity = 0.02F;
        this.friction = 0.92F;
        this.lifetime = 20 + this.random.nextInt(15);
        this.startSize = 0.08F + this.random.nextFloat() * 0.06F;
        this.quadSize = this.startSize;
        this.xd = xd + (this.random.nextDouble() - 0.5) * 0.02;
        this.yd = yd - 0.01;
        this.zd = zd + (this.random.nextDouble() - 0.5) * 0.02;
        this.roll = this.random.nextFloat() * (float)Math.PI * 2.0F;
        this.oRoll = this.roll;

        float[] tint = TINTS[this.random.nextInt(TINTS.length)];
        setColor(tint[0], tint[1], tint[2]);
    }

    @Override
    public void tick() {
        super.tick();

        float life = (float)this.age / this.lifetime;

        this.quadSize = this.startSize * (1.0F - life * 0.6F);
        setAlpha(life < 0.6F ? 1.0F : 1.0F - (life - 0.6F) / 0.4F);
        this.oRoll = this.roll;
        this.roll += 0.05F;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    /// Stars shine in the dark
    @Override
    public int getLightCoords(float partialTick) {
        return 15728880;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
            return new UnicornStarParticle(level, x, y, z, xAux, yAux, zAux, this.sprites.get(random));
        }
    }
}
