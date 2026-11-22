package one.syx.mohorses.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import one.syx.mohorses.advancement.HorseEvents;
import one.syx.mohorses.registry.ParticleRegistry;

/// Vanilla zombie horses slowly rot away, like the undead horses of Mo' Creatures: every few minutes another stage of
/// flesh falls off (textures/entity/zombie_horse/decay_<stage>.png), with bits dropping to the ground all the while,
/// until only a skeleton horse is left. The state lives on the zombie horse itself (see ZombieHorseMixin).
public final class ZombieHorseDecay {
    /// Number of stages between zombie and skeleton (one decay texture per stage).
    public static final int STAGES = 5;
    /// How long each stage lasts, in ticks (4 minutes; from fresh zombie to skeleton takes 24 minutes)
    public static final int TICKS_PER_STAGE = 4 * 60 * 20;

    /// Entity events (the byte vanilla sends to every player watching an entity) that tell the clients the decay stage:
    /// EVENT_STAGE + stage. Vanilla entity events stop well below this, so they don't clash. This way the stage needs
    /// no synced data on the vanilla zombie horse (NeoForge forbids adding that to other mods' or vanilla entities)
    /// and no network channel of its own.
    public static final byte EVENT_STAGE = 90;

    /// Implemented by zombie horses through the mixin
    public interface Decaying {
        int mohorses$getDecayStage();

        void mohorses$setDecayStage(int stage);

        int mohorses$getDecayTicks();

        void mohorses$setDecayTicks(int ticks);
    }

    public static byte stageEvent(int stage) {
        return (byte)(EVENT_STAGE + stage);
    }

    /// Client side: takes the stage out of an entity event. Returns false if the event is something else.
    public static boolean handleEvent(Decaying horse, byte event) {
        if (event < EVENT_STAGE || event > EVENT_STAGE + STAGES)
            return false;

        horse.mohorses$setDecayStage(event - EVENT_STAGE);

        return true;
    }

    public static int stageOf(ZombieHorse horse) {
        return horse instanceof Decaying decaying ? decaying.mohorses$getDecayStage() : 0;
    }

    /// Server side, every tick
    public static void tick(ZombieHorse horse, ServerLevel level) {
        Decaying decaying = (Decaying)horse;
        int ticks = decaying.mohorses$getDecayTicks() + 1;

        if (ticks < TICKS_PER_STAGE) {
            decaying.mohorses$setDecayTicks(ticks);

            return;
        }

        decaying.mohorses$setDecayTicks(0);

        int stage = decaying.mohorses$getDecayStage() + 1;

        if (stage <= STAGES) {
            decaying.mohorses$setDecayStage(stage);
            level.broadcastEntityEvent(horse, stageEvent(stage));
            MoHorse.spawnMaggots(level, horse, 1);
            level.sendParticles(ParticleRegistry.UNDEAD_FLESH.get(), horse.getX(), horse.getY() + horse.getBbHeight() * 0.6, horse.getZ(), 12, 0.5, 0.3, 0.5, 0.0);

            return;
        }

        turnToSkeleton(horse, level);
    }

    /// The last of the flesh falls off: the zombie horse becomes a skeleton horse, keeping its owner, saddle and name
    private static void turnToSkeleton(ZombieHorse horse, ServerLevel level) {
        MoHorse.spawnMaggots(level, horse, 1);

        SkeletonHorse skeleton = horse.convertTo(EntityTypes.SKELETON_HORSE, ConversionParams.single(horse, true, false), next -> HorseTransfer.copy(horse, next, false));

        if (skeleton != null) {
            HorseEvents.awardOwner(skeleton, HorseEvents.SKELETON_HORSE);
            level.sendParticles(ParticleRegistry.UNDEAD_FLESH.get(), skeleton.getX(), skeleton.getY() + skeleton.getBbHeight() * 0.6, skeleton.getZ(), 30, 0.6, 0.4, 0.6, 0.0);
            level.sendParticles(ParticleTypes.SMOKE, skeleton.getX(), skeleton.getY() + 1.0, skeleton.getZ(), 15, 0.5, 0.5, 0.5, 0.01);
            skeleton.playSound(SoundEvents.SKELETON_HORSE_AMBIENT, 1.0F, 0.8F);
        }
    }

    /// Client side, every tick: now and then a bit of flesh drops off, more often the further the horse has rotted
    public static void spawnFleshParticles(ZombieHorse horse) {
        spawnFleshParticles(horse, stageOf(horse));
    }

    /// Client side, every tick, for any rotting horse
    public static void spawnFleshParticles(LivingEntity horse, int stage) {
        float chance = 0.025F + stage * 0.02F;

        if (horse.getRandom().nextFloat() < chance)
            horse.level().addParticle(ParticleRegistry.UNDEAD_FLESH.get(), horse.getRandomX(0.6), horse.getY() + horse.getBbHeight() * (0.4 + horse.getRandom().nextFloat() * 0.5), horse.getRandomZ(0.6), 0.0, 0.0, 0.0);
    }

    private ZombieHorseDecay() {}
}
