package one.syx.mohorses.mixin.client;

import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import one.syx.mohorses.client.renderer.FlickerTexture;
import one.syx.mohorses.client.renderer.NuzzlePose;
import one.syx.mohorses.entity.EggNuzzle;
import one.syx.mohorses.entity.VanillaHorseEssences;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Works out whether a vanilla horse flickers to another look right now (see VanillaHorseEssences.flickerTexture) and
/// whether it is nuzzling (see EggNuzzle)
@Mixin(AbstractHorseRenderer.class)
public abstract class AbstractHorseRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/equine/AbstractHorse;Lnet/minecraft/client/renderer/entity/state/EquineRenderState;F)V", at = @At("TAIL"))
    private void mohorses$extractFlicker(AbstractHorse horse, EquineRenderState state, float partialTicks, CallbackInfo ci) {
        ((FlickerTexture)state).mohorses$setFlickerTexture(VanillaHorseEssences.flickerTexture(horse, partialTicks));
        NuzzlePose.set(state, EggNuzzle.progress(horse, partialTicks));
    }
}
