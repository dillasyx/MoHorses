package one.syx.mohorses.mixin;

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.phys.EntityHitResult;
import one.syx.mohorses.entity.EggNuzzle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// A horse hit by a thrown egg reacts like one fed an egg (see [EggNuzzle])
@Mixin(ThrownEgg.class)
public abstract class ThrownEggMixin {
    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void mohorses$nuzzle(EntityHitResult hit, CallbackInfo ci) {
        EggNuzzle.hitBy(hit.getEntity());
    }
}
