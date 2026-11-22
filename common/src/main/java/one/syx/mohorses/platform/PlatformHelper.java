package one.syx.mohorses.platform;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.function.Supplier;

/// Generic interface for each platform to extend, for platform-agnostic code.
public interface PlatformHelper {
    /// Register a new item
    <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item);

    /// Register a new entity
    <T extends Entity> Supplier<EntityType<T>> registerEntity(String id, Supplier<EntityType<T>> entityType);

    /// Register a new sound event
    <T extends SoundEvent> Supplier<T> registerSound(String id, Supplier<T> sound);

    /// Register a new particle type
    <T extends ParticleType<?>> Supplier<T> registerParticle(String id, Supplier<T> particle);

    /// Register a new creative tab
    <T extends CreativeModeTab> Supplier<T> registerCreativeModeTab(String id, Supplier<T> tab);

    /// Register a new advancement trigger
    <T extends CriterionTrigger<?>> Supplier<T> registerTrigger(String id, Supplier<T> trigger);

    /// Register a new loot condition type
    <T extends MapCodec<? extends LootItemCondition>> Supplier<T> registerLootCondition(String id, Supplier<T> codec);

    CreativeModeTab.Builder newCreativeTabBuilder();

    /// Add an extra single-item drop, guarded by the given condition, to an existing (vanilla) loot table
    void addEntityDrop(Identifier lootTableId, Supplier<? extends Item> item, LootItemCondition condition);
}
