package one.syx.mohorses.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import one.syx.mohorses.registry.ItemRegistry;

import java.util.concurrent.CompletableFuture;

public class MoHorsesRecipeProvider extends FabricRecipeProvider {
    public MoHorsesRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
        return new RecipeProvider(recipes, advancements) {
            @Override
            public void buildRecipes() {
                // Food
                shaped(RecipeCategory.FOOD, ItemRegistry.SUGAR_LUMP.get())
                        .pattern("SS")
                        .pattern("SS")
                        .define('S', Items.SUGAR)
                        .unlockedBy("has_sugar", has(Items.SUGAR))
                        .save(this.output);

                // Armor: crystal horse armor is netherite horse armor set in diamond and glass
                shaped(RecipeCategory.COMBAT, ItemRegistry.CRYSTAL_HORSE_ARMOR.get())
                        .pattern("  D")
                        .pattern("GAG")
                        .pattern("DGD")
                        .define('D', Items.DIAMOND)
                        .define('G', Items.GLASS)
                        .define('A', Items.NETHERITE_HORSE_ARMOR)
                        .unlockedBy("has_netherite_horse_armor", has(Items.NETHERITE_HORSE_ARMOR))
                        .save(this.output);

                // Tools: the whip of Mo' Creatures, with unicorn horns instead of big cat claws
                shaped(RecipeCategory.COMBAT, ItemRegistry.WHIP.get())
                        .pattern("ULU")
                        .pattern("L L")
                        .pattern("U I")
                        .define('U', ItemRegistry.UNICORN_HORN.get())
                        .define('L', Items.LEATHER)
                        .define('I', Items.IRON_INGOT)
                        .unlockedBy("has_unicorn_horn", has(ItemRegistry.UNICORN_HORN.get()))
                        .save(this.output);

                // Amulets
                shaped(RecipeCategory.MISC, ItemRegistry.BONE_AMULET.get())
                        .pattern("BGB")
                        .pattern("GEG")
                        .pattern("BGB")
                        .define('B', Items.BONE)
                        .define('G', Items.GOLD_NUGGET)
                        .define('E', Items.ENDER_PEARL)
                        .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                        .save(this.output);

                shaped(RecipeCategory.MISC, ItemRegistry.FAIRY_AMULET.get())
                        .pattern("BGB")
                        .pattern("GUG")
                        .pattern("BGB")
                        .define('B', Items.BLAZE_POWDER)
                        .define('G', Items.GOLD_NUGGET)
                        .define('U', ItemRegistry.UNICORN_HORN.get())
                        .unlockedBy("has_unicorn_horn", has(ItemRegistry.UNICORN_HORN.get()))
                        .save(this.output);

                shaped(RecipeCategory.MISC, ItemRegistry.GHOST_AMULET.get())
                        .pattern("BGB")
                        .pattern("GTG")
                        .pattern("BGB")
                        .define('B', Items.BONE)
                        .define('G', Items.GOLD_NUGGET)
                        .define('T', Items.GHAST_TEAR)
                        .unlockedBy("has_ghast_tear", has(Items.GHAST_TEAR))
                        .save(this.output);

                shaped(RecipeCategory.MISC, ItemRegistry.PEGASUS_AMULET.get())
                        .pattern("BGB")
                        .pattern("GDG")
                        .pattern("BGB")
                        .define('B', Items.BLAZE_POWDER)
                        .define('G', Items.GOLD_NUGGET)
                        .define('D', Items.DIAMOND)
                        .unlockedBy("has_diamond", has(Items.DIAMOND))
                        .save(this.output);

                shaped(RecipeCategory.MISC, ItemRegistry.PET_AMULET.get())
                        .pattern("G G")
                        .pattern(" D ")
                        .pattern("G G")
                        .define('G', Items.GOLD_NUGGET)
                        .define('D', Items.DIAMOND)
                        .unlockedBy("has_diamond", has(Items.DIAMOND))
                        .save(this.output);

                // Essences
                shaped(RecipeCategory.MISC, ItemRegistry.ESSENCE_OF_DARKNESS.get())
                        .pattern("E")
                        .pattern("H")
                        .pattern("G")
                        .define('E', Items.ENDER_PEARL)
                        .define('H', ItemRegistry.HEART_OF_DARKNESS.get())
                        .define('G', Items.GLASS_BOTTLE)
                        .unlockedBy("has_heart_of_darkness", has(ItemRegistry.HEART_OF_DARKNESS.get()))
                        .save(this.output);

                shaped(RecipeCategory.MISC, ItemRegistry.ESSENCE_OF_UNDEAD.get())
                        .pattern("R")
                        .pattern("H")
                        .pattern("G")
                        .define('R', Items.ROTTEN_FLESH)
                        .define('H', ItemRegistry.HEART_OF_UNDEAD.get())
                        .define('G', Items.GLASS_BOTTLE)
                        .unlockedBy("has_heart_of_undead", has(ItemRegistry.HEART_OF_UNDEAD.get()))
                        .save(this.output);

                shaped(RecipeCategory.MISC, ItemRegistry.ESSENCE_OF_FIRE.get())
                        .pattern("B")
                        .pattern("H")
                        .pattern("G")
                        .define('B', Items.BLAZE_POWDER)
                        .define('H', ItemRegistry.HEART_OF_FIRE.get())
                        .define('G', Items.GLASS_BOTTLE)
                        .unlockedBy("has_heart_of_fire", has(ItemRegistry.HEART_OF_FIRE.get()))
                        .save(this.output);

                shaped(RecipeCategory.MISC, ItemRegistry.ESSENCE_OF_LIGHT.get())
                        .pattern("U")
                        .pattern("F")
                        .pattern("D")
                        .define('U', ItemRegistry.ESSENCE_OF_UNDEAD.get())
                        .define('F', ItemRegistry.ESSENCE_OF_FIRE.get())
                        .define('D', ItemRegistry.ESSENCE_OF_DARKNESS.get())
                        .unlockedBy("has_essence_of_undead", has(ItemRegistry.ESSENCE_OF_UNDEAD.get()))
                        .unlockedBy("has_essence_of_fire", has(ItemRegistry.ESSENCE_OF_FIRE.get()))
                        .unlockedBy("has_essence_of_darkness", has(ItemRegistry.ESSENCE_OF_DARKNESS.get()))
                        .save(this.output);
            }
        };
    }

    @Override
    public String getName() {
        return "Mo' Horses Recipes";
    }
}
