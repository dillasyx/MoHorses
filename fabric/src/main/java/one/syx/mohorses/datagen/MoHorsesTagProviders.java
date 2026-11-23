package one.syx.mohorses.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import one.syx.mohorses.ModConstants;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.item.HorseAmuletItem;
import one.syx.mohorses.registry.ItemRegistry;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public final class MoHorsesTagProviders {
    /// Sugar lumps are horse food and lure horses
    public static class Items extends FabricTagsProvider.ItemTagsProvider {
        public Items(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            var sugarLump = BuiltInRegistries.ITEM.getResourceKey(ItemRegistry.SUGAR_LUMP.get()).orElseThrow();

            builder(ItemTags.HORSE_FOOD).add(sugarLump);
            builder(ItemTags.HORSE_TEMPT_ITEMS).add(sugarLump);
        }
    }

    /// The vanilla entity tags the horse kinds belong to, straight from [HorseKind]
    public static class EntityTypes extends FabricTagsProvider.EntityTypeTagsProvider {
        public EntityTypes(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            addKinds(EntityTypeTags.CAN_EQUIP_SADDLE, kind -> true);
            addKinds(EntityTypeTags.CAN_WEAR_HORSE_ARMOR, kind -> kind.armorRule() != HorseKind.ArmorRule.NONE);
            addKinds(ItemRegistry.CAN_WEAR_CRYSTAL_HORSE_ARMOR, kind -> kind.armorRule() == HorseKind.ArmorRule.CRYSTAL);
            addKinds(EntityTypeTags.UNDEAD, HorseKind::isUndead);
            addKinds(EntityTypeTags.INVERTED_HEALING_AND_HARM, HorseKind::isUndead);
            // Only the undead with flesh on them burn in daylight, not the skeletons
            addKinds(EntityTypeTags.BURN_IN_DAYLIGHT, kind -> kind.decaysInto() != null);

            // Empty: for modpacks to keep other mods' animals out of the amulets
            builder(HorseAmuletItem.EXCLUDED);
        }

        private void addKinds(TagKey<EntityType<?>> tag, Predicate<HorseKind> filter) {
            var appender = builder(tag);

            for (HorseKind kind : HorseKind.values()) {
                if (filter.test(kind))
                    appender.add(ResourceKey.create(Registries.ENTITY_TYPE, ModConstants.id(kind.id())));
            }
        }
    }

    private MoHorsesTagProviders() {}
}
