package one.syx.mohorses.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.BredAnimalsTrigger;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.KilledTrigger;
import net.minecraft.advancements.triggers.TameAnimalTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import one.syx.mohorses.ModConstants;
import one.syx.mohorses.advancement.HorseEventTrigger;
import one.syx.mohorses.advancement.HorseEvents;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.registry.EntityRegistry;
import one.syx.mohorses.registry.ItemRegistry;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/// The advancements of the Mo' Horses tab: along the breeding line of the breeding guide, the undead, ghosts and items
public class MoHorsesAdvancementProvider extends FabricAdvancementProvider {
    /// The kinds a player can own, for the Stable Master challenge (the wild Nightmare can't be tamed)
    private static final HorseKind[] OWNABLE = HorseKind.values();

    private HolderGetter<EntityType<?>> entityTypes;
    private Consumer<AdvancementHolder> output;

    public MoHorsesAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
        this.entityTypes = registries.lookupOrThrow(Registries.ENTITY_TYPE);
        this.output = output;

        AdvancementHolder root = Advancement.Builder.advancement()
                .rootDisplay(ItemRegistry.SUGAR_LUMP.get(), title("root"), description("root"),
                        Identifier.withDefaultNamespace("gui/advancements/backgrounds/husbandry"), AdvancementType.TASK, false, false, false)
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("horse", tamed(EntityTypes.HORSE))
                .addCriterion("donkey", tamed(EntityTypes.DONKEY))
                .addCriterion("mule", tamed(EntityTypes.MULE))
                .addCriterion("zebra", tamed(zebra()))
                .build(id("root"));
        output.accept(root);

        // The breeding line
        AdvancementHolder zebra = save("tame_zebra", root, egg(HorseKind.ZEBRA), AdvancementType.TASK, builder -> builder
                .addCriterion("zebra", tamed(zebra())));
        AdvancementHolder zorse = save("breed_zorse", zebra, egg(HorseKind.ZORSE), AdvancementType.TASK, builder -> builder
                .addCriterion("zorse", bred(EntityPredicate.Builder.entity().of(this.entityTypes, kind(HorseKind.ZORSE)))));
        save("breed_zonkey", zebra, egg(HorseKind.ZONKEY), AdvancementType.TASK, builder -> builder
                .addCriterion("zonkey", bred(EntityPredicate.Builder.entity().of(this.entityTypes, kind(HorseKind.ZONKEY)))));

        AdvancementHolder nightmare = save("nightmare", zorse, egg(HorseKind.NIGHTMARE), AdvancementType.TASK, builder -> builder
                .addCriterion("nightmare", owned(HorseKind.NIGHTMARE)));
        AdvancementHolder batHorse = save("bat_horse", zorse, egg(HorseKind.BAT_HORSE), AdvancementType.TASK, builder -> builder
                .addCriterion("bat_horse", owned(HorseKind.BAT_HORSE)));
        AdvancementHolder unicorn = save("unicorn", nightmare, egg(HorseKind.UNICORN), AdvancementType.TASK, builder -> builder
                .addCriterion("unicorn", owned(HorseKind.UNICORN)));
        AdvancementHolder pegasus = save("pegasus", batHorse, egg(HorseKind.PEGASUS), AdvancementType.TASK, builder -> builder
                .addCriterion("pegasus", owned(HorseKind.PEGASUS)));
        save("dark_pegasus", pegasus, egg(HorseKind.DARK_PEGASUS), AdvancementType.TASK, builder -> builder
                .addCriterion("dark_pegasus", owned(HorseKind.DARK_PEGASUS)));

        AdvancementHolder fairy = save("fairy_horse", unicorn, egg(HorseKind.FAIRY_HORSE), AdvancementType.GOAL, builder -> builder
                .addCriterion("fairy_horse", bred(EntityPredicate.Builder.entity().of(this.entityTypes, kind(HorseKind.FAIRY_HORSE)))));
        CompoundTag black = new CompoundTag();
        black.putString("FairyColor", "black");
        save("black_fairy_horse", fairy, Items.DYE.black(), AdvancementType.GOAL, builder -> builder
                .addCriterion("black_fairy_horse", bred(EntityPredicate.Builder.entity().of(this.entityTypes, kind(HorseKind.FAIRY_HORSE)).nbt(new NbtPredicate(black)))));
        save("dye_fairy_horse", fairy, Items.DYE.pink(), AdvancementType.TASK, builder -> builder
                .addCriterion("dyed", HorseEventTrigger.TriggerInstance.of(HorseEvents.FAIRY_DYED)));
        save("unicorn_charge", unicorn, ItemRegistry.WHIP.get(), AdvancementType.TASK, builder -> builder
                .addCriterion("charge", HorseEventTrigger.TriggerInstance.of(HorseEvents.UNICORN_CHARGE)));

        // The undead
        AdvancementHolder undead = save("undead", root, ItemRegistry.ESSENCE_OF_UNDEAD.get(), AdvancementType.TASK, builder -> builder
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("zombie_horse", HorseEventTrigger.TriggerInstance.of(HorseEvents.ZOMBIE_HORSE))
                .addCriterion("undead_unicorn", owned(HorseKind.UNDEAD_UNICORN))
                .addCriterion("undead_pegasus", owned(HorseKind.UNDEAD_PEGASUS)));
        AdvancementHolder skeleton = save("skeleton", undead, Items.BONE, AdvancementType.TASK, builder -> builder
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("skeleton_horse", HorseEventTrigger.TriggerInstance.of(HorseEvents.SKELETON_HORSE))
                .addCriterion("skeleton_unicorn", owned(HorseKind.SKELETON_UNICORN))
                .addCriterion("skeleton_pegasus", owned(HorseKind.SKELETON_PEGASUS)));
        save("revive", skeleton, ItemRegistry.ESSENCE_OF_LIGHT.get(), AdvancementType.GOAL, builder -> builder
                .addCriterion("revived", HorseEventTrigger.TriggerInstance.of(HorseEvents.REVIVED)));
        save("ghost", root, ItemRegistry.GHOST_AMULET.get(), AdvancementType.GOAL, builder -> builder
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("ghost_horse", owned(HorseKind.GHOST_HORSE))
                .addCriterion("ghost_pegasus", owned(HorseKind.GHOST_PEGASUS)));

        // Items and the rest
        save("amulet", root, ItemRegistry.PET_AMULET.get(), AdvancementType.TASK, builder -> builder
                .addCriterion("amulet", HorseEventTrigger.TriggerInstance.of(HorseEvents.AMULET)));
        save("crystal_horse_armor", root, ItemRegistry.CRYSTAL_HORSE_ARMOR.get(), AdvancementType.TASK, builder -> builder
                .addCriterion("crystal_horse_armor", InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.CRYSTAL_HORSE_ARMOR.get())));
        save("hearts", root, ItemRegistry.HEART_OF_FIRE.get(), AdvancementType.TASK, builder -> builder
                .addCriterion("heart_of_darkness", InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.HEART_OF_DARKNESS.get()))
                .addCriterion("heart_of_fire", InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.HEART_OF_FIRE.get()))
                .addCriterion("heart_of_undead", InventoryChangeTrigger.TriggerInstance.hasItems(ItemRegistry.HEART_OF_UNDEAD.get())));
        save("brute_rider", root, Items.GOLDEN_SPEAR, AdvancementType.GOAL, builder -> builder
                .addCriterion("brute", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity()
                        .of(this.entityTypes, EntityTypes.PIGLIN_BRUTE)
                        .vehicle(EntityPredicate.Builder.entity().of(this.entityTypes, EntityRegistry.WILD_NIGHTMARE.get())))));
        save("shuffle", zebra, ItemRegistry.SHUFFLING_MUSIC_DISC.get(), AdvancementType.TASK, builder -> builder
                .addCriterion("shuffle", HorseEventTrigger.TriggerInstance.of(HorseEvents.SHUFFLE)));

        // Every kind once
        save("stable_master", fairy, Items.GOLDEN_CARROT, AdvancementType.CHALLENGE, builder -> {
            builder.rewards(AdvancementRewards.Builder.experience(200));

            for (HorseKind kind : OWNABLE)
                builder.addCriterion(kind.id(), owned(kind));
        });
    }

    private AdvancementHolder save(String name, AdvancementHolder parent, Item icon, AdvancementType type, Consumer<Advancement.Builder> criteria) {
        Advancement.Builder builder = Advancement.Builder.advancement()
                .parent(parent)
                .display(icon, title(name), description(name), type, true, true, false);

        criteria.accept(builder);

        AdvancementHolder holder = builder.build(id(name));

        this.output.accept(holder);

        return holder;
    }

    private static Identifier id(String name) {
        return ModConstants.id("main/" + name);
    }

    private static Component title(String name) {
        return Component.translatable("advancements.mohorses." + name + ".title");
    }

    private static Component description(String name) {
        return Component.translatable("advancements.mohorses." + name + ".description");
    }

    private static Item egg(HorseKind kind) {
        return ItemRegistry.horseSpawnEgg(kind).get();
    }

    private static EntityType<?> kind(HorseKind kind) {
        return EntityRegistry.horse(kind).get();
    }

    private static EntityType<?> zebra() {
        return kind(HorseKind.ZEBRA);
    }

    private Criterion<TameAnimalTrigger.TriggerInstance> tamed(EntityType<?> type) {
        return TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(this.entityTypes, type));
    }

    private static Criterion<BredAnimalsTrigger.TriggerInstance> bred(EntityPredicate.Builder child) {
        return BredAnimalsTrigger.TriggerInstance.bredAnimals(Optional.empty(), Optional.empty(), Optional.of(child.build()));
    }

    private static Criterion<HorseEventTrigger.TriggerInstance> owned(HorseKind kind) {
        return HorseEventTrigger.TriggerInstance.of(HorseEvents.owned(kind));
    }
}
