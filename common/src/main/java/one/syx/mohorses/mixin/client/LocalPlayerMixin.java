package one.syx.mohorses.mixin.client;

import net.minecraft.client.player.LocalPlayer;
import one.syx.mohorses.entity.MoHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// The jump bar stays empty while a winged horse is in the air: there the jump key makes it climb
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Shadow
    private int jumpRidingTicks;

    @Shadow
    private float jumpRidingScale;

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void mohorses$noChargeInTheAir(CallbackInfo ci) {
        if (((LocalPlayer)(Object)this).getControlledVehicle() instanceof MoHorse horse && horse.onItsWings()) {
            this.jumpRidingTicks = 0;
            this.jumpRidingScale = 0.0F;
        }
    }
}
