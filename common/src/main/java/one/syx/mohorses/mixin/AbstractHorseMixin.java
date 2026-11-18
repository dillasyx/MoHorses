package one.syx.mohorses.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import one.syx.mohorses.entity.HorseTransfer;
import one.syx.mohorses.entity.VanillaHorseEssences;
import one.syx.mohorses.entity.ZombieHorseDecay;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Additions to every horse, all as injections at the head or tail of vanilla methods (nothing is replaced, so other
/// mods changing horses keep working):
///
/// - essences work on vanilla horses (see [VanillaHorseEssences]); only while the player holds an essence
/// - zombie horses rot (see [ZombieHorseDecay], the state lives in ZombieHorseMixin)
/// - the owner can be handed on to the horse that replaces this one (see [HorseTransfer])
@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal implements HorseTransfer.OwnerAccess, VanillaHorseEssences.State {
    @Shadow
    private @Nullable EntityReference<LivingEntity> owner;

    @Shadow
    protected SimpleContainer inventory;

    @Unique
    private int mohorses$essenceTarget;
    @Unique
    private int mohorses$essenceTicks;
    @Unique
    private int mohorses$maggotsLeft;
    @Unique
    private int mohorses$essenceVariant;
    @Unique
    private int mohorses$lookTarget;
    @Unique
    private int mohorses$lookStart;

    protected AbstractHorseMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Override
    public void mohorses$setOwnerReference(@Nullable EntityReference<LivingEntity> owner) {
        this.owner = owner;
    }

    // ------------------------------------------------------------------------------------------------
    // Essences on vanilla horses
    // ------------------------------------------------------------------------------------------------

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void mohorses$useEssence(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = VanillaHorseEssences.interact((AbstractHorse)(Object)this, player, hand);

        if (result != null)
            cir.setReturnValue(result);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void mohorses$tick(CallbackInfo ci) {
        if (this.mohorses$essenceTarget != VanillaHorseEssences.TARGET_NONE && level() instanceof ServerLevel serverLevel)
            VanillaHorseEssences.tick((AbstractHorse)(Object)this, serverLevel);

        if ((Object)this instanceof ZombieHorse zombie) {
            if (level() instanceof ServerLevel serverLevel) {
                if (isAlive())
                    ZombieHorseDecay.tick(zombie, serverLevel);
            }
            else {
                ZombieHorseDecay.spawnFleshParticles(zombie);
            }
        }
    }

    /// The decay stage of a zombie horse and the start of an essence's change arrive on the client as entity events
    /// (see ZombieHorseDecay.EVENT_STAGE and VanillaHorseEssences.EVENT_TO_HORSE)
    @Inject(method = "handleEntityEvent", at = @At("HEAD"), cancellable = true)
    private void mohorses$receiveDecayStage(byte event, CallbackInfo ci) {
        if (this instanceof ZombieHorseDecay.Decaying decaying && ZombieHorseDecay.handleEvent(decaying, event))
            ci.cancel();
        else if (VanillaHorseEssences.handleEvent((AbstractHorse)(Object)this, event))
            ci.cancel();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void mohorses$saveEssence(ValueOutput output, CallbackInfo ci) {
        if (this.mohorses$essenceTarget != VanillaHorseEssences.TARGET_NONE) {
            output.putInt("MoHorsesEssenceTarget", this.mohorses$essenceTarget);
            output.putInt("MoHorsesEssenceTicks", this.mohorses$essenceTicks);
            output.putInt("MoHorsesMaggotsLeft", this.mohorses$maggotsLeft);
            output.putInt("MoHorsesEssenceVariant", this.mohorses$essenceVariant);
        }

        if (this instanceof ZombieHorseDecay.Decaying decaying) {
            output.putInt("MoHorsesDecayStage", decaying.mohorses$getDecayStage());
            output.putInt("MoHorsesDecayTicks", decaying.mohorses$getDecayTicks());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void mohorses$loadEssence(ValueInput input, CallbackInfo ci) {
        this.mohorses$essenceTarget = input.getIntOr("MoHorsesEssenceTarget", VanillaHorseEssences.TARGET_NONE);
        this.mohorses$essenceTicks = input.getIntOr("MoHorsesEssenceTicks", 0);
        this.mohorses$maggotsLeft = input.getIntOr("MoHorsesMaggotsLeft", 0);
        this.mohorses$essenceVariant = input.getIntOr("MoHorsesEssenceVariant", 0);

        if (this instanceof ZombieHorseDecay.Decaying decaying) {
            decaying.mohorses$setDecayStage(input.getIntOr("MoHorsesDecayStage", 0));
            decaying.mohorses$setDecayTicks(input.getIntOr("MoHorsesDecayTicks", 0));
        }
    }

    @Override
    public int mohorses$getEssenceTarget() {
        return this.mohorses$essenceTarget;
    }

    @Override
    public int mohorses$getEssenceTicks() {
        return this.mohorses$essenceTicks;
    }

    @Override
    public int mohorses$getMaggotsLeft() {
        return this.mohorses$maggotsLeft;
    }

    @Override
    public void mohorses$setEssence(int target, int ticks, int maggotsLeft) {
        this.mohorses$essenceTarget = target;
        this.mohorses$essenceTicks = ticks;
        this.mohorses$maggotsLeft = maggotsLeft;
    }

    @Override
    public int mohorses$getEssenceVariant() {
        return this.mohorses$essenceVariant;
    }

    @Override
    public void mohorses$setEssenceVariant(int variant) {
        this.mohorses$essenceVariant = variant;
    }

    @Override
    public int mohorses$getLookTarget() {
        return this.mohorses$lookTarget;
    }

    @Override
    public int mohorses$getLookStart() {
        return this.mohorses$lookStart;
    }

    @Override
    public void mohorses$setLook(int target, int start) {
        this.mohorses$lookTarget = target;
        this.mohorses$lookStart = start;
    }

    @Override
    public void mohorses$dropChest(ServerLevel level) {
        if (!((Object)this instanceof AbstractChestedHorse chested) || !chested.hasChest())
            return;

        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);

            if (!stack.isEmpty()) {
                spawnAtLocation(level, stack);
                this.inventory.setItem(i, ItemStack.EMPTY);
            }
        }

        spawnAtLocation(level, Items.CHEST);
        chested.setChest(false);
    }
}
