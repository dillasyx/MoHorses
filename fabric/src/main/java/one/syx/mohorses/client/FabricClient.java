package one.syx.mohorses.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import one.syx.mohorses.item.AmuletTooltip;

/// Initializer class for Fabric-specific client setup tasks
public final class FabricClient implements ClientModInitializer {
    @SuppressWarnings("deprecation")
    @Override
    public void onInitializeClient() {
        ClientRegistration.registerLayerDefinitions((layer, definition) -> ModelLayerRegistry.registerModelLayer(layer, definition::get));
        ClientRegistration.registerParticles((type, provider) -> ParticleProviderRegistry.getInstance().register(type, provider::apply));
        ClientRegistration.registerRenderers(EntityRendererRegistry::register);
        ClientTooltipComponentCallback.EVENT.register(data -> data instanceof AmuletTooltip amulet ? new AmuletPreviewTooltip(amulet) : null);
    }
}
