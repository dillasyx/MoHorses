package one.syx.mohorses.registry;

import net.minecraft.core.particles.SimpleParticleType;
import one.syx.mohorses.ModConstants;

import java.util.function.Supplier;

public final class ParticleRegistry {
    public static void init() {}

    /// Bits of rotten flesh that drop off decaying zombie horses
    public static final Supplier<SimpleParticleType> UNDEAD_FLESH = ModConstants.PLATFORM.registerParticle("undead_flesh", () -> new SimpleParticleType(false) {});

    /// The stars unicorns leave in the air
    public static final Supplier<SimpleParticleType> UNICORN_STAR = ModConstants.PLATFORM.registerParticle("unicorn_star", () -> new SimpleParticleType(false) {});

    /// Feathers a Pegasus or Dark Pegasus loses
    public static final Supplier<SimpleParticleType> FEATHER = ModConstants.PLATFORM.registerParticle("feather", () -> new SimpleParticleType(false) {});
    public static final Supplier<SimpleParticleType> DARK_FEATHER = ModConstants.PLATFORM.registerParticle("dark_feather", () -> new SimpleParticleType(false) {});

    /// The little bats that flutter off from a Bat Horse
    public static final Supplier<SimpleParticleType> TINY_BAT = ModConstants.PLATFORM.registerParticle("tiny_bat", () -> new SimpleParticleType(false) {});
}
