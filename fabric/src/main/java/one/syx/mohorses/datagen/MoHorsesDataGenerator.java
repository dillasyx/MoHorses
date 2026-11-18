package one.syx.mohorses.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/// Generates the mod's recipes, item models and tags into common/src/main/generated/resources (shared by all loaders).
///
///     ./gradlew :fabric:runDatagen
///
/// Everything else (lang, sounds, entity loot tables, biome and block tags, textures) is written by hand in
/// common/src/main/resources.
public class MoHorsesDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(MoHorsesModelProvider::new);
        pack.addProvider(MoHorsesRecipeProvider::new);
        pack.addProvider(MoHorsesTagProviders.Items::new);
        pack.addProvider(MoHorsesTagProviders.EntityTypes::new);
    }
}
