package one.syx.mohorses.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Variant;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import one.syx.mohorses.advancement.HorseEvents;
import one.syx.mohorses.registry.ItemRegistry;
import one.syx.mohorses.registry.ParticleRegistry;
import org.jetbrains.annotations.Nullable;

/// Essences on vanilla horses (through AbstractHorseMixin), as the spec asks:
///
/// - Essence of Undead on a horse, donkey or mule turns it into a zombie horse (which then rots into a skeleton horse)
/// - Essence of Light on a skeleton horse gives it its flesh back: it is a horse again
///
/// Like on the mod's horses this takes four seconds, in which the horse stands still in a cloud of particles
/// (rotting flesh and maggots, or light) and flickers between its old and its new look (client mixins on the vanilla
/// horse renderers, see flickerTexture).
public final class VanillaHorseEssences {
    public static final int TARGET_NONE = 0;
    public static final int TARGET_ZOMBIE_HORSE = 1;
    public static final int TARGET_HORSE = 2;

    /// Entity events telling the clients that a horse starts changing (so they can let it flicker): one for the zombie
    /// horse, and one per coat colour for a horse (EVENT_TO_HORSE + the Variant id). Clear of vanilla's entity events
    /// and of the decay stages (ZombieHorseDecay.EVENT_STAGE).
    public static final byte EVENT_TO_ZOMBIE_HORSE = 99;
    public static final byte EVENT_TO_HORSE = 100;

    /// The transformation state, kept on every horse by AbstractHorseMixin
    public interface State {
        int mohorses$getEssenceTarget();

        int mohorses$getEssenceTicks();

        int mohorses$getMaggotsLeft();

        void mohorses$setEssence(int target, int ticks, int maggotsLeft);

        /// The coat colour (Variant id) a skeleton horse gets back with its flesh, chosen when it starts changing
        int mohorses$getEssenceVariant();

        void mohorses$setEssenceVariant(int variant);

        /// Client side: what the horse is changing into (TARGET_ZOMBIE_HORSE, or TARGET_HORSE + 1 + Variant id) and the
        /// tick it started, for the flicker
        int mohorses$getLookTarget();

        int mohorses$getLookStart();

        void mohorses$setLook(int target, int start);

        /// Drops the chest and what is in it (a zombie horse can't carry one)
        void mohorses$dropChest(ServerLevel level);
    }

    /// Called first when a player uses an item on any horse. Returns null if this is nothing for us.
    public static @Nullable InteractionResult interact(AbstractHorse horse, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int target = targetFor(horse, stack);

        if (target == TARGET_NONE)
            return null;

        State state = (State)horse;

        // Essences only work on your own grown horses, one at a time
        if (!horse.isTamed() || horse.isBaby() || horse.isVehicle() || state.mohorses$getEssenceTarget() != TARGET_NONE)
            return InteractionResult.PASS;

        if (horse.level() instanceof ServerLevel) {
            stack.consume(1, player);

            boolean undead = target == TARGET_ZOMBIE_HORSE;
            state.mohorses$setEssence(target, MoHorse.TRANSFORM_DURATION, undead ? 2 + horse.getRandom().nextInt(2) : 0);
            horse.playSound(undead ? SoundEvents.ZOMBIE_INFECT : SoundEvents.BEACON_POWER_SELECT, 1.0F, 0.8F);

            // Tell the clients, so the horse flickers between its old and its new look
            if (undead) {
                horse.level().broadcastEntityEvent(horse, EVENT_TO_ZOMBIE_HORSE);
            }
            else {
                Variant coat = Util.getRandom(Variant.values(), horse.getRandom());
                state.mohorses$setEssenceVariant(coat.getId());
                horse.level().broadcastEntityEvent(horse, (byte)(EVENT_TO_HORSE + coat.getId()));
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static int targetFor(AbstractHorse horse, ItemStack stack) {
        if (horse instanceof MoHorse)
            return TARGET_NONE;

        if (stack.is(ItemRegistry.ESSENCE_OF_UNDEAD.get()) && (horse instanceof Horse || horse instanceof Donkey || horse instanceof Mule))
            return TARGET_ZOMBIE_HORSE;

        if (stack.is(ItemRegistry.ESSENCE_OF_LIGHT.get()) && horse instanceof SkeletonHorse)
            return TARGET_HORSE;

        return TARGET_NONE;
    }

    /// Server side, every tick of every horse
    public static void tick(AbstractHorse horse, ServerLevel level) {
        State state = (State)horse;
        int target = state.mohorses$getEssenceTarget();

        if (target == TARGET_NONE || !horse.isAlive())
            return;

        boolean undead = target == TARGET_ZOMBIE_HORSE;
        int ticksLeft = state.mohorses$getEssenceTicks() - 1;
        int maggots = state.mohorses$getMaggotsLeft();
        float progress = 1.0F - (float)ticksLeft / MoHorse.TRANSFORM_DURATION;

        // The horse stands still meanwhile
        horse.getNavigation().stop();

        // Particles, thicker towards the end
        if (horse.getRandom().nextFloat() < 0.4F + progress * 0.6F) {
            if (undead)
                level.sendParticles(ParticleRegistry.UNDEAD_FLESH.get(), horse.getX(), horse.getY() + horse.getBbHeight() * 0.7, horse.getZ(), 1 + (int)(progress * 3), 0.4, 0.3, 0.4, 0.0);
            else
                level.sendParticles(ParticleTypes.END_ROD, horse.getX(), horse.getY() + horse.getBbHeight() * 0.6, horse.getZ(), 1 + (int)(progress * 3), 0.5, 0.5, 0.5, 0.02);

            level.sendParticles(undead ? ParticleTypes.SMOKE : ParticleTypes.ENCHANT, horse.getX(), horse.getY() + horse.getBbHeight() * 0.6, horse.getZ(), 1, 0.5, 0.5, 0.5, 0.02);
        }

        // The maggots come out one by one
        if (undead && maggots > 0 && ticksLeft % 25 == 5) {
            MoHorse.spawnMaggots(level, horse, 1);
            maggots--;
        }

        if (ticksLeft == MoHorse.TRANSFORM_DURATION / 2)
            horse.playSound(undead ? SoundEvents.ZOMBIE_HORSE_HURT : SoundEvents.AMETHYST_BLOCK_CHIME, 1.5F, 0.7F);

        if (ticksLeft > 0) {
            state.mohorses$setEssence(target, ticksLeft, maggots);

            return;
        }

        state.mohorses$setEssence(TARGET_NONE, 0, 0);

        if (maggots > 0)
            MoHorse.spawnMaggots(level, horse, maggots);

        if (undead) {
            state.mohorses$dropChest(level);

            if (convert(horse, EntityType.ZOMBIE_HORSE) instanceof ZombieHorse zombie)
                HorseEvents.awardOwner(zombie, HorseEvents.ZOMBIE_HORSE);

            level.sendParticles(ParticleRegistry.UNDEAD_FLESH.get(), horse.getX(), horse.getY() + 1.0, horse.getZ(), 30, 0.6, 0.5, 0.6, 0.0);
            horse.playSound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 1.0F, 0.8F);
        }
        else {
            Horse restored = convert(horse, EntityType.HORSE);

            if (restored != null) {
                ((HorseVariantAccess)restored).mohorses$setVariantAndMarkings(Variant.byId(state.mohorses$getEssenceVariant()), Markings.NONE);
                HorseEvents.awardOwner(restored, HorseEvents.REVIVED);
            }

            level.sendParticles(ParticleTypes.END_ROD, horse.getX(), horse.getY() + 1.0, horse.getZ(), 40, 0.6, 0.6, 0.6, 0.05);
            horse.playSound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 1.0F, 1.3F);
        }
    }

    /// The same horse as another type: owner, name, saddle, armor and stats stay
    private static <T extends AbstractHorse> @Nullable T convert(AbstractHorse horse, EntityType<T> type) {
        return horse.convertTo(type, ConversionParams.single(horse, true, false), next -> HorseTransfer.copy(horse, next, false));
    }

    /// Implemented by Horse through HorseMixin: sets the coat (vanilla keeps the setter private)
    public interface HorseVariantAccess {
        void mohorses$setVariantAndMarkings(Variant variant, Markings markings);
    }

    /// Client side: takes a "starts changing" entity event. Returns false if the event is something else.
    public static boolean handleEvent(AbstractHorse horse, byte event) {
        State state = (State)horse;

        if (event == EVENT_TO_ZOMBIE_HORSE)
            state.mohorses$setLook(TARGET_ZOMBIE_HORSE, horse.tickCount);
        else if (event >= EVENT_TO_HORSE && event < EVENT_TO_HORSE + Variant.values().length)
            state.mohorses$setLook(TARGET_HORSE + 1 + event - EVENT_TO_HORSE, horse.tickCount);
        else
            return false;

        return true;
    }

    /// Client side: the texture a changing vanilla horse shows right now instead of its own, or null
    public static @Nullable Identifier flickerTexture(AbstractHorse horse, float partialTick) {
        State state = (State)horse;
        int target = state.mohorses$getLookTarget();

        if (target == TARGET_NONE)
            return null;

        float progress = (horse.tickCount - state.mohorses$getLookStart() + partialTick) / MoHorse.TRANSFORM_DURATION;

        // The server replaces the horse at the end; should that not reach us, stop flickering
        if (progress > 1.5F) {
            state.mohorses$setLook(TARGET_NONE, 0);
            return null;
        }

        if (!TransformFlicker.showsNewLook(progress))
            return null;

        String baby = horse.isBaby() ? "_baby" : "";

        if (target == TARGET_ZOMBIE_HORSE)
            return Identifier.withDefaultNamespace("textures/entity/horse/horse_zombie" + baby + ".png");

        return Identifier.withDefaultNamespace("textures/entity/horse/horse_" + Variant.byId(target - TARGET_HORSE - 1).getSerializedName() + baby + ".png");
    }

    private VanillaHorseEssences() {}
}
