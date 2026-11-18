package one.syx.mohorses.item;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.entity.MoHorse;
import one.syx.mohorses.registry.SoundRegistry;
import org.jetbrains.annotations.Nullable;

/// The whip of Mo' Creatures. Cracked while riding a unicorn, it makes the unicorn charge: at the creature it hit, or
/// straight ahead when it was cracked at the ground or in the air.
public class WhipItem extends Item {
    public WhipItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        crack(attacker);

        MoHorse unicorn = ridden(attacker);

        if (unicorn != null)
            unicorn.startCharge(target);
    }

    /// The unicorn the user rides and steers, or null
    private static @Nullable MoHorse ridden(LivingEntity user) {
        return user.getVehicle() instanceof MoHorse horse && horse.kind() == HorseKind.UNICORN && horse.getControllingPassenger() == user ? horse : null;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            crack(player);

            MoHorse unicorn = ridden(player);

            if (unicorn != null)
                unicorn.startCharge(null);
        }

        // SUCCESS swings the arm
        player.getCooldowns().addCooldown(player.getItemInHand(hand), 10);

        return InteractionResult.SUCCESS;
    }

    private static void crack(LivingEntity user) {
        user.level().playSound(null, user.getX(), user.getY(), user.getZ(), SoundRegistry.WHIP.get(), SoundSource.PLAYERS, 1.0F, 0.9F + user.getRandom().nextFloat() * 0.2F);
    }
}
