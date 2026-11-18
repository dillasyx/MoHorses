package one.syx.mohorses;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.impl.creativetab.FabricCreativeModeTabBuilderImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import one.syx.mohorses.platform.PlatformHelper;

import java.util.function.Supplier;

/// Fabric [SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) implementation for [PlatformHelper]
public final class FabricPlatform implements PlatformHelper {
    @Override
    public <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item) {
        return register(BuiltInRegistries.ITEM, id, item);
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(String id, Supplier<EntityType<T>> entityType) {
        return register(BuiltInRegistries.ENTITY_TYPE, id, entityType);
    }

    @Override
    public <T extends ParticleType<?>> Supplier<T> registerParticle(String id, Supplier<T> particle) {
        return register(BuiltInRegistries.PARTICLE_TYPE, id, particle);
    }

    @Override
    public <T extends SoundEvent> Supplier<T> registerSound(String id, Supplier<T> sound) {
        return register(BuiltInRegistries.SOUND_EVENT, id, sound);
    }

    @Override
    public <T extends CreativeModeTab> Supplier<T> registerCreativeModeTab(String id, Supplier<T> tab) {
        return register(BuiltInRegistries.CREATIVE_MODE_TAB, id, tab);
    }

    @Override
    public CreativeModeTab.Builder newCreativeTabBuilder() {
        return new FabricCreativeModeTabBuilderImpl();
    }

    @Override
    public <T extends MapCodec<? extends LootItemCondition>> Supplier<T> registerLootCondition(String id, Supplier<T> codec) {
        return register(BuiltInRegistries.LOOT_CONDITION_TYPE, id, codec);
    }

    @Override
    public void addEntityDrop(Identifier lootTableId, Supplier<? extends Item> item, LootItemCondition condition) {
        final ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableId);

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (source.isBuiltin() && lootTableKey.equals(key)) {
                tableBuilder.withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(item.get())
                                .when(() -> condition)));
            }
        });
    }

    /// Fabric registers immediately, the returned supplier just hands the registered object back
    @SuppressWarnings("unchecked")
    private static <T, R extends Registry<? super T>> Supplier<T> register(R registry, String id, Supplier<T> object) {
        final T registeredObject = Registry.register((Registry<T>)registry, ModConstants.id(id), object.get());

        return () -> registeredObject;
    }
}
