package one.syx.mohorses.mixin.client;

import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.resources.Identifier;
import one.syx.mohorses.client.renderer.FlickerTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Shows the look a changing horse flickers to (see AbstractHorseRendererMixin)
@Mixin(HorseRenderer.class)
public abstract class HorseRendererMixin {
    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/HorseRenderState;)Lnet/minecraft/resources/Identifier;", at = @At("HEAD"), cancellable = true)
    private void mohorses$flicker(HorseRenderState state, CallbackInfoReturnable<Identifier> cir) {
        Identifier flicker = ((FlickerTexture)state).mohorses$getFlickerTexture();

        if (flicker != null)
            cir.setReturnValue(flicker);
    }
}
