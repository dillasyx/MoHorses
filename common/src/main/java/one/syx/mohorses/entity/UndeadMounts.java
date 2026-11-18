package one.syx.mohorses.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

/// Riding an undead horse hides you from monsters (through LivingEntityMixin)
public final class UndeadMounts {
    /// Whether this player rides an undead horse (zombie or skeleton, vanilla or of this mod). Like in Mo' Creatures,
    /// monsters then take them for one of their own and leave them alone, unless the player hurt them first.
    public static boolean hiddenFrom(LivingEntity attacker, LivingEntity target) {
        if (!(attacker instanceof Enemy) || !(target instanceof Player))
            return false;

        if (attacker.getLastHurtByMob() == target)
            return false;

        return target.getVehicle() instanceof ZombieHorse
                || target.getVehicle() instanceof SkeletonHorse
                || (target.getVehicle() instanceof MoHorse horse && horse.kind().isUndead());
    }

    private UndeadMounts() {}
}
