package one.syx.mohorses;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import one.syx.mohorses.platform.PlatformHelper;

import java.util.function.Supplier;

/// NeoForge [SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) implementation for [PlatformHelper]
public final class NeoForgePlatform implements PlatformHelper {
    @Override
    public <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item) {
        return ModMain.ITEM_REGISTRY.register(id, item);
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(String id, Supplier<EntityType<T>> entity) {
        return ModMain.ENTITY_REGISTRY.register(id, entity);
    }

    @Override
    public <T extends ParticleType<?>> Supplier<T> registerParticle(String id, Supplier<T> particle) {
        return ModMain.PARTICLE_REGISTRY.register(id, particle);
    }

    @Override
    public <T extends SoundEvent> Supplier<T> registerSound(String id, Supplier<T> sound) {
        return ModMain.SOUND_EVENT_REGISTRY.register(id, sound);
    }

    @Override
    public <T extends CreativeModeTab> Supplier<T> registerCreativeModeTab(String id, Supplier<T> tab) {
        return ModMain.CREATIVE_TAB_REGISTRY.register(id, tab);
    }

    @Override
    public CreativeModeTab.Builder newCreativeTabBuilder() {
        return CreativeModeTab.builder();
    }

    @Override
    public <T extends CriterionTrigger<?>> Supplier<T> registerTrigger(String id, Supplier<T> trigger) {
        return ModMain.TRIGGER_REGISTRY.register(id, trigger);
    }

    @Override
    public <T extends MapCodec<? extends LootItemCondition>> Supplier<T> registerLootCondition(String id, Supplier<T> codec) {
        return ModMain.LOOT_CONDITION_REGISTRY.register(id, codec);
    }

    @Override
    public void addEntityDrop(Identifier lootTableId, Supplier<? extends Item> item, LootItemCondition condition) {
        ModMain.EXTRA_DROPS.add(new ModMain.ExtraDrop(lootTableId, item, condition));
    }
}
