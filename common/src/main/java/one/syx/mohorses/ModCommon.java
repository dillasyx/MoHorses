package one.syx.mohorses;

import one.syx.mohorses.registry.ArmorMaterialRegistry;
import one.syx.mohorses.registry.EntityRegistry;
import one.syx.mohorses.registry.ItemRegistry;
import one.syx.mohorses.registry.LootTableRegistry;
import one.syx.mohorses.registry.ParticleRegistry;
import one.syx.mohorses.registry.SoundRegistry;

/// Platform-agnostic common-code setup
public final class ModCommon {
    /// Registers everything of the mod, on every loader (the registry classes register their entries when loaded)
    public static void init() {
        SoundRegistry.init();
        ParticleRegistry.init();
        EntityRegistry.init();
        ArmorMaterialRegistry.init();
        ItemRegistry.init();
        LootTableRegistry.init();
    }
}
