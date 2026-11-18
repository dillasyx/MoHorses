package one.syx.mohorses.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import one.syx.mohorses.ModConstants;

/// Initializer class for NeoForge-specific client setup tasks
@Mod(value = ModConstants.MODID, dist = Dist.CLIENT)
public final class NeoForgeClient {
    public NeoForgeClient(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions.class, event -> ClientRegistration.registerLayerDefinitions(event::registerLayerDefinition));
        modBus.addListener(RegisterParticleProvidersEvent.class, event -> ClientRegistration.registerParticles((type, provider) -> event.registerSpriteSet(type, provider::apply)));
        modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, event -> ClientRegistration.registerRenderers(event::registerEntityRenderer));
    }
}
