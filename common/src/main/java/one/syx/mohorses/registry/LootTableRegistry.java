package one.syx.mohorses.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import one.syx.mohorses.ModConstants;

import java.util.function.Supplier;

/// Extra heart drops added to vanilla entity loot tables
public final class LootTableRegistry {
    public static void init() {
        // HEART OF DARKNESS
        drop("bat", ItemRegistry.HEART_OF_DARKNESS, 0.01f);
        drop("ender_dragon", ItemRegistry.HEART_OF_DARKNESS, 1f);
        drop("enderman", ItemRegistry.HEART_OF_DARKNESS, 0.06f);
        drop("endermite", ItemRegistry.HEART_OF_DARKNESS, 0.06f);
        drop("phantom", ItemRegistry.HEART_OF_DARKNESS, 0.02f);
        drop("wither_skeleton", ItemRegistry.HEART_OF_DARKNESS, 0.02f);
        drop("wither", ItemRegistry.HEART_OF_DARKNESS, 1f);

        // HEART OF FIRE
        drop("blaze", ItemRegistry.HEART_OF_FIRE, 0.01f);
        drop("ghast", ItemRegistry.HEART_OF_FIRE, 0.06f);
        drop("magma_cube", ItemRegistry.HEART_OF_FIRE, 0.02f);
        drop("strider", ItemRegistry.HEART_OF_FIRE, 0.06f);

        // HEART OF UNDEAD
        drop("drowned", ItemRegistry.HEART_OF_UNDEAD, 0.06f);
        drop("husk", ItemRegistry.HEART_OF_UNDEAD, 0.06f);
        drop("zoglin", ItemRegistry.HEART_OF_UNDEAD, 0.12f);
        drop("zombie_horse", ItemRegistry.HEART_OF_UNDEAD, 0.5f);
        drop("zombie_villager", ItemRegistry.HEART_OF_UNDEAD, 0.12f);
        drop("zombie", ItemRegistry.HEART_OF_UNDEAD, 0.06f);
        drop("zombified_piglin", ItemRegistry.HEART_OF_UNDEAD, 0.02f);
    }

    private static void drop(String vanillaEntity, Supplier<? extends Item> item, float chance) {
        ModConstants.PLATFORM.addEntityDrop(Identifier.withDefaultNamespace("entities/" + vanillaEntity), item, chance);
    }
}
