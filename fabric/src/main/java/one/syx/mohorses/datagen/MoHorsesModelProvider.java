package one.syx.mohorses.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.color.item.CustomModelDataSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.registry.ItemRegistry;

import java.util.List;
import java.util.function.Supplier;

/// Item models. Amulets show their "_full" texture while they hold a horse.
public class MoHorsesModelProvider extends FabricModelProvider {
    public MoHorsesModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generators) {}

    @Override
    public void generateItemModels(ItemModelGenerators generators) {
        for (Supplier<? extends Item> item : List.of(
                ItemRegistry.SUGAR_LUMP, ItemRegistry.SHUFFLING_MUSIC_DISC, ItemRegistry.UNICORN_HORN, ItemRegistry.CRYSTAL_HORSE_ARMOR,
                ItemRegistry.HEART_OF_DARKNESS, ItemRegistry.HEART_OF_FIRE, ItemRegistry.HEART_OF_UNDEAD,
                ItemRegistry.ESSENCE_OF_DARKNESS, ItemRegistry.ESSENCE_OF_FIRE, ItemRegistry.ESSENCE_OF_LIGHT, ItemRegistry.ESSENCE_OF_UNDEAD,
                ItemRegistry.MAGGOT_SPAWN_EGG)) {
            generators.generateFlatItem(item.get(), ModelTemplates.FLAT_ITEM);
        }

        generators.generateFlatItem(ItemRegistry.WHIP.get(), ModelTemplates.FLAT_HANDHELD_ITEM);

        for (Supplier<? extends Item> amulet : List.of(
                ItemRegistry.PET_AMULET, ItemRegistry.PEGASUS_AMULET, ItemRegistry.FAIRY_AMULET, ItemRegistry.BONE_AMULET, ItemRegistry.GHOST_AMULET)) {
            Item item = amulet.get();
            Identifier empty = generators.createFlatItemModel(item, ModelTemplates.FLAT_ITEM);
            Identifier full = generators.createFlatItemModel(item, "_full", ModelTemplates.FLAT_ITEM);

            generators.itemModelOutput.accept(item, ItemModelUtils.conditional(ItemModelUtils.hasComponent(DataComponents.ENTITY_DATA),
                    ItemModelUtils.plainModel(full), ItemModelUtils.plainModel(empty)));
        }

        for (HorseKind kind : HorseKind.values()) {
            if (kind != HorseKind.FAIRY_HORSE)
                generators.generateFlatItem(ItemRegistry.horseSpawnEgg(kind).get(), ModelTemplates.FLAT_ITEM);
        }

        // The Fairy Horse egg has a second layer (wings and mane) tinted with the colour the egg spawns, kept in its
        // custom model data (see ItemRegistry.fairySpawnEgg); the plain egg is pink
        Item fairyEgg = ItemRegistry.horseSpawnEgg(HorseKind.FAIRY_HORSE).get();
        Identifier fairyModel = generators.generateLayeredItem(fairyEgg, TextureMapping.getItemTexture(fairyEgg), TextureMapping.getItemTexture(fairyEgg, "_overlay"));

        generators.itemModelOutput.accept(fairyEgg, ItemModelUtils.tintedModel(fairyModel, ItemModelUtils.constantTint(-1), new CustomModelDataSource(0, 0xFFF2B8EC)));

        generators.generateFlatItem(ItemRegistry.WILD_NIGHTMARE_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
        generators.generateFlatItem(ItemRegistry.WILD_UNICORN_SPAWN_EGG.get(), ModelTemplates.FLAT_ITEM);
    }
}
