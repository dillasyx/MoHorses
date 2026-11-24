package one.syx.mohorses.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.EntityTypes;
import one.syx.mohorses.client.model.MoHorseModel;
import one.syx.mohorses.client.particle.FeatherParticle;
import one.syx.mohorses.client.particle.TinyBatParticle;
import one.syx.mohorses.client.particle.UndeadFleshParticle;
import one.syx.mohorses.client.particle.UnicornStarParticle;
import one.syx.mohorses.client.renderer.DecayingZombieHorseRenderer;
import one.syx.mohorses.registry.ParticleRegistry;
import one.syx.mohorses.client.renderer.MaggotRenderer;
import one.syx.mohorses.client.renderer.MoHorseRenderer;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.entity.MoHorse;
import one.syx.mohorses.registry.EntityRegistry;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/// Platform-agnostic client registration
public final class ClientRegistration {
    /// Register the entity renderers
    @SuppressWarnings("rawtypes")
    public static void registerRenderers(BiConsumer<EntityType<? extends Entity>, EntityRendererProvider> entityRenderers) {
        entityRenderers.accept(EntityRegistry.MAGGOT.get(), MaggotRenderer::new);
        // Replaces the vanilla zombie horse renderer, to show the rotting stages
        entityRenderers.accept(EntityTypes.ZOMBIE_HORSE, DecayingZombieHorseRenderer::new);

        EntityRendererProvider<MoHorse> wildNightmare = context -> new MoHorseRenderer(context, HorseKind.NIGHTMARE);

        entityRenderers.accept(EntityRegistry.WILD_NIGHTMARE.get(), wildNightmare);

        EntityRendererProvider<MoHorse> wildUnicorn = context -> new MoHorseRenderer(context, HorseKind.UNICORN);

        entityRenderers.accept(EntityRegistry.WILD_UNICORN.get(), wildUnicorn);

        for (HorseKind kind : HorseKind.values()) {
            EntityRendererProvider<MoHorse> renderer = context -> new MoHorseRenderer(context, kind);

            entityRenderers.accept(EntityRegistry.horse(kind).get(), renderer);
        }
    }

    /// Register the particle providers: takes each particle type and a factory that makes its provider from its sprites
    public static void registerParticles(BiConsumer<ParticleType<SimpleParticleType>, Function<SpriteSet, ParticleProvider<SimpleParticleType>>> registrar) {
        registrar.accept(ParticleRegistry.UNDEAD_FLESH.get(), UndeadFleshParticle.Provider::new);
        registrar.accept(ParticleRegistry.UNICORN_STAR.get(), UnicornStarParticle.Provider::new);
        registrar.accept(ParticleRegistry.FEATHER.get(), FeatherParticle.Provider::new);
        registrar.accept(ParticleRegistry.DARK_FEATHER.get(), FeatherParticle.Provider::new);
        registrar.accept(ParticleRegistry.TINY_BAT.get(), TinyBatParticle.Provider::new);
    }

    /// Register the model layers of the horse shapes
    public static void registerLayerDefinitions(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> registrar) {
        MoHorseModel.registerLayers(registrar);
    }
}
