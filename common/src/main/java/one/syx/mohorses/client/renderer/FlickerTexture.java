package one.syx.mohorses.client.renderer;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/// Implemented by the vanilla horse render state (EquineRenderStateMixin): the texture a horse that an essence is
/// changing shows right now instead of its own, or null
public interface FlickerTexture {
    @Nullable Identifier mohorses$getFlickerTexture();

    void mohorses$setFlickerTexture(@Nullable Identifier texture);
}
