package one.syx.mohorses.mixin;

import net.minecraft.world.entity.animal.equine.ZombieHorse;
import one.syx.mohorses.entity.ZombieHorseDecay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/// Gives vanilla zombie horses the state of their decay: the stage (for the texture) and a timer.
///
/// This only adds the fields. Ticking, saving, the maggots on death and telling the clients the stage are injections
/// into inherited methods (AbstractHorseMixin, LivingEntityMixin, EntityMixin), so no method of ZombieHorse is added or
/// replaced and no synced data is added to it: other mods changing zombie horses don't collide with this.
/// The logic is in [ZombieHorseDecay].
@Mixin(ZombieHorse.class)
public abstract class ZombieHorseMixin implements ZombieHorseDecay.Decaying {
    @Unique
    private int mohorses$decayStage;

    @Unique
    private int mohorses$decayTicks;

    @Override
    public int mohorses$getDecayStage() {
        return this.mohorses$decayStage;
    }

    @Override
    public void mohorses$setDecayStage(int stage) {
        this.mohorses$decayStage = stage;
    }

    @Override
    public int mohorses$getDecayTicks() {
        return this.mohorses$decayTicks;
    }

    @Override
    public void mohorses$setDecayTicks(int ticks) {
        this.mohorses$decayTicks = ticks;
    }
}
