package one.syx.mohorses;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import one.syx.mohorses.platform.PlatformHelper;

import java.util.function.Supplier;

/// Forge [SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) implementation for [PlatformHelper]
public final class ForgePlatform implements PlatformHelper {
    @Override
    public <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item) {
        return ModMain.ITEM_REGISTRY.register(id, item);
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(String id, Supplier<EntityType<T>> entityType) {
        return ModMain.ENTITY_REGISTRY.register(id, entityType);
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
    public void addEntityDrop(Identifier lootTableId, Supplier<? extends Item> item, float chance) {
        ModMain.EXTRA_DROPS.add(new ModMain.ExtraDrop(lootTableId, item, chance));
    }
}
