package one.syx.mohorses.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import one.syx.mohorses.ModConstants;

import java.util.EnumMap;
import java.util.Map;

public final class ArmorMaterialRegistry {
    public static void init() {}

    public static final ResourceKey<EquipmentAsset> CRYSTAL_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ModConstants.id("crystal"));
    public static final ArmorMaterial CRYSTAL = new ArmorMaterial(37, createDefenseMap(3, 6, 8, 3, 19), 15, SoundEvents.ARMOR_EQUIP_DIAMOND, 3.0F, 0.1F, ItemTags.REPAIRS_DIAMOND_ARMOR, CRYSTAL_KEY);

    private static Map<ArmorType, Integer> createDefenseMap(int boots, int leggings, int chestplate, int helmet, int body) {
        return new EnumMap<>(Map.of(
                ArmorType.BOOTS, boots,
                ArmorType.LEGGINGS, leggings,
                ArmorType.CHESTPLATE, chestplate,
                ArmorType.HELMET, helmet,
                ArmorType.BODY, body));
    }
}
