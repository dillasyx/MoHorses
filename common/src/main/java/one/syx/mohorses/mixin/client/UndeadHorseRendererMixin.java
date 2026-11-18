package one.syx.mohorses.mixin.client;

import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.resources.Identifier;
import one.syx.mohorses.client.renderer.FlickerTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Shows the look a changing horse flickers to (see AbstractHorseRendererMixin)
@Mixin(UndeadHorseRenderer.class)
public abstract class UndeadHorseRendererMixin {
    @Inject(method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/EquineRenderState;)Lnet/minecraft/resources/Identifier;", at = @At("HEAD"), cancellable = true)
    private void mohorses$flicker(EquineRenderState state, CallbackInfoReturnable<Identifier> cir) {
        Identifier flicker = ((FlickerTexture)state).mohorses$getFlickerTexture();

        if (flicker != null)
            cir.setReturnValue(flicker);
    }
}
