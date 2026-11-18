package one.syx.mohorses.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import one.syx.mohorses.item.HorseAmuletItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Amulets come first when used on a mob, like name tags and spawn eggs in vanilla. Otherwise a tamed wolf, cat or
/// parrot would just sit down when its owner clicks it with a Pet Amulet, and the amulet would never get a say.
@Mixin(Mob.class)
public abstract class MobMixin {
    @Inject(method = "checkAndHandleImportantInteractions", at = @At("HEAD"), cancellable = true)
    private void mohorses$amuletFirst(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof HorseAmuletItem) {
            InteractionResult result = stack.interactLivingEntity(player, (Mob)(Object)this, hand);

            if (result.consumesAction())
                cir.setReturnValue(result);
        }
    }
}
