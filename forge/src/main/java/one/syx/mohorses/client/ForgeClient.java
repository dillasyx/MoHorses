package one.syx.mohorses.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import one.syx.mohorses.ModConstants;
import one.syx.mohorses.item.AmuletTooltip;

/// Initializer class for Forge-specific client setup tasks
@Mod.EventBusSubscriber(modid = ModConstants.MODID, value = Dist.CLIENT)
public final class ForgeClient {
    /// Register the model layers of the horse shapes
    @SubscribeEvent
    public static void registerLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        ClientRegistration.registerLayerDefinitions(event::registerLayerDefinition);
    }

    /// Register the particle providers
    @SubscribeEvent
    public static void registerParticles(final RegisterParticleProvidersEvent event) {
        ClientRegistration.registerParticles((type, provider) -> event.registerSpriteSet(type, provider::apply));
    }

    /// Register the entity renderers
    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        ClientRegistration.registerRenderers(event::registerEntityRenderer);
    }

    /// Register the preview of the animal in a full amulet
    @SubscribeEvent
    public static void registerTooltipComponents(final RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(AmuletTooltip.class, AmuletPreviewTooltip::new);
    }
}
