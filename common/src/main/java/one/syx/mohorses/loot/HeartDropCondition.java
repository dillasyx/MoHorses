package one.syx.mohorses.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Set;

/// Passes only for kills by a player, with the given chance plus a bonus per Looting level.
/// Vanilla's looting condition needs the enchantment registry when the loot table is built, which Forge doesn't hand out
/// there, so this one looks Looting up when the mob dies.
public record HeartDropCondition(float chance, float lootingBonus) implements LootItemCondition {
    public static final MapCodec<HeartDropCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.floatRange(0, 1).fieldOf("chance").forGetter(HeartDropCondition::chance),
            Codec.floatRange(0, 1).fieldOf("looting_bonus").forGetter(HeartDropCondition::lootingBonus)
    ).apply(instance, HeartDropCondition::new));

    @Override
    public MapCodec<HeartDropCondition> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.LAST_DAMAGE_PLAYER, LootContextParams.ATTACKING_ENTITY);
    }

    @Override
    public boolean test(LootContext context) {
        if (!context.hasParameter(LootContextParams.LAST_DAMAGE_PLAYER))
            return false;

        int looting = 0;

        if (context.getOptional(LootContextParams.ATTACKING_ENTITY) instanceof LivingEntity attacker)
            looting = EnchantmentHelper.getEnchantmentLevel(
                    context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING), attacker);

        return context.getRandom().nextFloat() < chance + looting * lootingBonus;
    }
}
