package one.syx.mohorses.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.item.ItemStack;
import one.syx.mohorses.entity.GhostHorses;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.entity.MoHorse;
import one.syx.mohorses.entity.UndeadMounts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// - Monsters leave players on undead horses alone (see [UndeadMounts#hiddenFrom]). `canAttack` is what both finding a
///   new target and keeping the current one check, so a monster already chasing the player gives up once they mount.
/// - Which horse armor a horse of this mod wears (see [HorseKind#canWear]). `isEquippableInSlot` can't be overridden
///   in this version, and the armor slot, dispensers and right-clicking a horse all ask it.
/// - When a horse dies: a tamed one may come back as a ghost, and maggots crawl out of a zombie horse.
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    protected boolean dead;

    /// Whether the entity was still alive when die() was called (it may be called again on a dead one, and other mods
    /// can cancel the death)
    @Unique
    private boolean mohorses$wasAlive;

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void mohorses$ignoreUndeadRiders(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (UndeadMounts.hiddenFrom((LivingEntity)(Object)this, target))
            cir.setReturnValue(false);
    }

    @Inject(method = "isEquippableInSlot", at = @At("HEAD"), cancellable = true)
    private void mohorses$armorRules(ItemStack stack, EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        if (slot == EquipmentSlot.BODY && (Object)this instanceof MoHorse horse && !horse.kind().canWear(stack))
            cir.setReturnValue(false);
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void mohorses$beforeDeath(DamageSource source, CallbackInfo ci) {
        this.mohorses$wasAlive = !((LivingEntity)(Object)this).isRemoved() && !this.dead;
    }

    @Inject(method = "die", at = @At("TAIL"))
    private void mohorses$afterDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (!this.mohorses$wasAlive || !this.dead || !(self instanceof AbstractHorse horse) || !(self.level() instanceof ServerLevel level))
            return;

        GhostHorses.onHorseDied(horse, level);

        if (horse instanceof ZombieHorse)
            MoHorse.spawnMaggots(level, horse);
    }
}
