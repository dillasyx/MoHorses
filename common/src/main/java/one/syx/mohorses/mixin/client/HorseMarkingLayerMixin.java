package one.syx.mohorses.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import one.syx.mohorses.client.renderer.FlickerTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// No white spots or patches on a horse while it shows another look (see AbstractHorseRendererMixin)
@Mixin(HorseMarkingLayer.class)
public abstract class HorseMarkingLayerMixin {
    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HorseRenderState;FF)V", at = @At("HEAD"), cancellable = true)
    private void mohorses$hideMarkings(PoseStack poseStack, SubmitNodeCollector collector, int light, HorseRenderState state, float yRot, float xRot, CallbackInfo ci) {
        if (((FlickerTexture)state).mohorses$getFlickerTexture() != null)
            ci.cancel();
    }
}
