package one.syx.mohorses.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import one.syx.mohorses.registry.EntityRegistry;
import one.syx.mohorses.registry.SoundRegistry;
import org.jetbrains.annotations.Nullable;

/// The ghosts tamed horses may leave behind when they die (called from LivingEntityMixin)
public final class GhostHorses {
    /// Chance that a tamed horse leaves a ghost when it dies
    public static final float GHOST_CHANCE = 0.1F;

    /// A tamed horse died. One in ten of the rare kinds rises again as a ghost that belongs to the same player
    /// (which kinds, and which ghost: [HorseKind#ghost()]).
    public static void onHorseDied(AbstractHorse horse, ServerLevel level) {
        if (!horse.isTamed() || horse.getRandom().nextFloat() >= GHOST_CHANCE)
            return;

        HorseKind ghostKind = ghostOf(horse);

        if (ghostKind == null)
            return;

        MoHorse ghost = EntityRegistry.horse(ghostKind).get().create(level, EntitySpawnReason.TRIGGERED);

        if (ghost == null)
            return;

        ghost.snapTo(horse.getX(), horse.getY(), horse.getZ(), horse.getYRot(), 0.0F);
        ghost.setYHeadRot(horse.getYHeadRot());

        // The ghost is still the same horse: same owner, name and stats
        HorseTransfer.copy(horse, ghost, true);
        ghost.setCustomName(horse.getCustomName());
        ghost.setPersistenceRequired();
        ghost.startAppearing();

        level.addFreshEntity(ghost);
        level.sendParticles(ParticleTypes.SOUL, ghost.getX(), ghost.getY() + 0.8, ghost.getZ(), 20, 0.5, 0.6, 0.5, 0.02);
        level.sendParticles(ParticleTypes.WHITE_SMOKE, ghost.getX(), ghost.getY() + 0.8, ghost.getZ(), 15, 0.5, 0.5, 0.5, 0.01);
        ghost.playSound(SoundRegistry.GHOST_HORSE_AMBIENT.get(), 1.0F, 1.0F);
    }

    private static @Nullable HorseKind ghostOf(AbstractHorse horse) {
        return horse instanceof MoHorse moHorse ? moHorse.kind().ghost() : null;
    }

    private GhostHorses() {}
}
