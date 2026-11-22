package one.syx.mohorses.mixin.client;

import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.resources.Identifier;
import one.syx.mohorses.client.renderer.FlickerTexture;
import one.syx.mohorses.client.renderer.NuzzlePose;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/// Carries the texture a changing vanilla horse flickers to, and how far along its egg nuzzling is, from the renderer's
/// extractRenderState to the texture and the model
@Mixin(EquineRenderState.class)
public abstract class EquineRenderStateMixin implements FlickerTexture, NuzzlePose {
    @Unique
    private @Nullable Identifier mohorses$flickerTexture;
    @Unique
    private float mohorses$nuzzle = -1.0F;

    @Override
    public float mohorses$getNuzzle() {
        return this.mohorses$nuzzle;
    }

    @Override
    public void mohorses$setNuzzle(float progress) {
        this.mohorses$nuzzle = progress;
    }

    @Override
    public @Nullable Identifier mohorses$getFlickerTexture() {
        return this.mohorses$flickerTexture;
    }

    @Override
    public void mohorses$setFlickerTexture(@Nullable Identifier texture) {
        this.mohorses$flickerTexture = texture;
    }
}
