package one.syx.mohorses.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.entity.MoHorse;
import one.syx.mohorses.registry.SoundRegistry;

/// A treat for any kind of horse. Foals grow up a bit faster from it.
///
/// It is also the only way to tame a zebra: wild zebras can't be broken in by riding, and they only trust a rider who comes on a horse
/// that looks like one of them: black and white (a black horse with white spots or patches, or a white one with black spots).
public class SugarLumpItem extends Item {
    /// Chance that one sugar lump tames a zebra
    private static final float ZEBRA_TAME_CHANCE = 1.0F / 3.0F;

    public SugarLumpItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof AbstractHorse horse))
            return InteractionResult.PASS;

        if (target instanceof MoHorse zebra && zebra.kind() == HorseKind.ZEBRA && !zebra.isTamed())
            return tameZebra(stack, player, zebra);

        if (target.level() instanceof ServerLevel level) {
            stack.consume(1, player);

            if (horse.isBaby()) {
                horse.ageUp(100);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + target.getEyeHeight() * 0.5, target.getZ(), 7, 0.5, 0.5, 0.5, 0.0);
            }

            level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundRegistry.FEEDING_SUGAR_LUMP.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);
        }

        return InteractionResult.SUCCESS;
    }

    private static InteractionResult tameZebra(ItemStack stack, Player player, MoHorse zebra) {
        if (!(zebra.level() instanceof ServerLevel level))
            return InteractionResult.SUCCESS;

        // Not on the right horse: the zebra won't take it
        if (!ridesZebraLookalike(player) || zebra.isBaby()) {
            level.sendParticles(ParticleTypes.SMOKE, zebra.getX(), zebra.getY() + zebra.getBbHeight() * 0.8, zebra.getZ(), 7, 0.4, 0.3, 0.4, 0.01);
            zebra.makeMad();

            return InteractionResult.SUCCESS;
        }

        stack.consume(1, player);
        level.playSound(null, zebra.getX(), zebra.getY(), zebra.getZ(), SoundRegistry.FEEDING_SUGAR_LUMP.get(), SoundSource.NEUTRAL, 1.0f, 1.0f);

        boolean tamed = zebra.getRandom().nextFloat() < ZEBRA_TAME_CHANCE;

        if (tamed)
            zebra.tameWithName(player);

        // Hearts when it is tamed, smoke when not yet (the same effect as taming a vanilla horse)
        level.broadcastEntityEvent(zebra, tamed ? (byte)7 : (byte)6);

        return InteractionResult.SUCCESS;
    }

    /// Whether the player sits on a black and white horse: black with white spots or patches, or white with black spots
    public static boolean ridesZebraLookalike(Player player) {
        if (!(player.getVehicle() instanceof Horse horse))
            return false;

        return switch (horse.getVariant()) {
            case BLACK -> horse.getMarkings() == Markings.WHITE_DOTS || horse.getMarkings() == Markings.WHITE_FIELD;
            case WHITE -> horse.getMarkings() == Markings.BLACK_DOTS;
            default -> false;
        };
    }
}
