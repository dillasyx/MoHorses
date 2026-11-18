package one.syx.mohorses.mixin.client;

import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.resources.Identifier;
import one.syx.mohorses.client.renderer.FlickerTexture;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/// Carries the texture a changing vanilla horse flickers to, from the renderer's extractRenderState to its texture
@Mixin(EquineRenderState.class)
public abstract class EquineRenderStateMixin implements FlickerTexture {
    @Unique
    private @Nullable Identifier mohorses$flickerTexture;

    @Override
    public @Nullable Identifier mohorses$getFlickerTexture() {
        return this.mohorses$flickerTexture;
    }

    @Override
    public void mohorses$setFlickerTexture(@Nullable Identifier texture) {
        this.mohorses$flickerTexture = texture;
    }
}
