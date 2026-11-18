package one.syx.mohorses.registry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TypedEntityData;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import one.syx.mohorses.ModConstants;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.item.HorseAmuletItem;
import one.syx.mohorses.item.SugarLumpItem;
import one.syx.mohorses.item.WhipItem;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/// The mod's registered items
public final class ItemRegistry {
    public static void init() {}

    // SPAWN EGGS
    public static final Supplier<SpawnEggItem> WILD_NIGHTMARE_SPAWN_EGG = register("wild_nightmare_spawn_egg", SpawnEggItem::new, properties -> properties.spawnEgg(EntityRegistry.WILD_NIGHTMARE.get()));
    public static final Supplier<SpawnEggItem> MAGGOT_SPAWN_EGG = register("maggot_spawn_egg", SpawnEggItem::new, properties -> properties.spawnEgg(EntityRegistry.MAGGOT.get()));

    private static final Map<HorseKind, Supplier<SpawnEggItem>> HORSE_SPAWN_EGGS = new EnumMap<>(HorseKind.class);

    static {
        for (HorseKind kind : HorseKind.values()) {
            HORSE_SPAWN_EGGS.put(kind, register(kind.id() + "_spawn_egg", SpawnEggItem::new, properties -> properties.spawnEgg(EntityRegistry.horse(kind).get())));
        }
    }

    /// A Fairy Horse spawn egg of the given colour: the horse it spawns is dyed, and the egg's wings are tinted to match
    public static ItemStack fairySpawnEgg(DyeColor color) {
        ItemStack stack = new ItemStack(HORSE_SPAWN_EGGS.get(HorseKind.FAIRY_HORSE).get());
        CompoundTag data = new CompoundTag();

        data.putString("FairyColor", color.getSerializedName());
        stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityRegistry.horse(HorseKind.FAIRY_HORSE).get(), data));
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(), List.of(color.getTextureDiffuseColor())));
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.mohorses.fairy_horse_spawn_egg.colored", Component.translatable("color.minecraft." + color.getName())));

        return stack;
    }

    /// The spawn egg of a horse kind
    public static Supplier<SpawnEggItem> horseSpawnEgg(HorseKind kind) {
        return HORSE_SPAWN_EGGS.get(kind);
    }

    // FOOD ITEMS
    public static final Supplier<Item> SUGAR_LUMP = register("sugar_lump", SugarLumpItem::new, properties -> properties
            .stacksTo(32)
            .food(new FoodProperties.Builder()
                    .nutrition(2)
                    .saturationModifier(0.9f)
                    .alwaysEdible()
                    .build()));

    // MUSIC DISC ITEMS
    public static final Supplier<Item> SHUFFLING_MUSIC_DISC = register("shuffling_music_disc", Item::new, properties -> properties
            .stacksTo(1)
            .rarity(Rarity.RARE)
            .jukeboxPlayable(SoundRegistry.SHUFFLING_SONG_KEY));

    // HORSE ARMOR ITEMS
    public static final Supplier<Item> CRYSTAL_HORSE_ARMOR = register("crystal_horse_armor", Item::new, properties -> properties
            .stacksTo(1)
            .horseArmor(ArmorMaterialRegistry.CRYSTAL));

    // MATERIAL ITEMS
    public static final Supplier<Item> UNICORN_HORN = register("unicorn_horn", Item::new);

    // TOOLS
    /// Lash a creature while riding a unicorn and the unicorn charges at it
    public static final Supplier<Item> WHIP = register("whip", WhipItem::new, properties -> properties.durability(384));

    // HEART ITEMS
    public static final Supplier<Item> HEART_OF_DARKNESS = register("heart_of_darkness", Item::new);
    public static final Supplier<Item> HEART_OF_FIRE = register("heart_of_fire", Item::new);
    public static final Supplier<Item> HEART_OF_UNDEAD = register("heart_of_undead", Item::new);

    // ESSENCE ITEMS
    public static final Supplier<Item> ESSENCE_OF_DARKNESS = register("essence_of_darkness", Item::new);
    public static final Supplier<Item> ESSENCE_OF_FIRE = register("essence_of_fire", Item::new);
    public static final Supplier<Item> ESSENCE_OF_LIGHT = register("essence_of_light", Item::new);
    public static final Supplier<Item> ESSENCE_OF_UNDEAD = register("essence_of_undead", Item::new);

    // AMULET ITEMS
    public static final Supplier<Item> BONE_AMULET = register("bone_amulet", properties -> new HorseAmuletItem(properties, HorseKind.Amulet.BONE), properties -> properties.stacksTo(1));
    public static final Supplier<Item> FAIRY_AMULET = register("fairy_amulet", properties -> new HorseAmuletItem(properties, HorseKind.Amulet.FAIRY), properties -> properties.stacksTo(1));
    public static final Supplier<Item> GHOST_AMULET = register("ghost_amulet", properties -> new HorseAmuletItem(properties, HorseKind.Amulet.GHOST), properties -> properties.stacksTo(1));
    public static final Supplier<Item> PEGASUS_AMULET = register("pegasus_amulet", properties -> new HorseAmuletItem(properties, HorseKind.Amulet.PEGASUS), properties -> properties.stacksTo(1));
    public static final Supplier<Item> PET_AMULET = register("pet_amulet", properties -> new HorseAmuletItem(properties, HorseKind.Amulet.PET), properties -> properties.stacksTo(1));

    public static final Supplier<CreativeModeTab> MOHORSES_ITEM_TAB = ModConstants.PLATFORM.registerCreativeModeTab("mohorses_items", () -> ModConstants.PLATFORM.newCreativeTabBuilder()
            .title(Component.translatable("itemGroup." + ModConstants.MODID + ".mohorses_items"))
            .icon(() -> new ItemStack(SUGAR_LUMP.get()))
            .displayItems((enabledFeatures, entries) -> {
                entries.accept(SUGAR_LUMP.get());
                entries.accept(SHUFFLING_MUSIC_DISC.get());
                entries.accept(CRYSTAL_HORSE_ARMOR.get());

                entries.accept(HEART_OF_DARKNESS.get());
                entries.accept(HEART_OF_FIRE.get());
                entries.accept(HEART_OF_UNDEAD.get());

                entries.accept(ESSENCE_OF_DARKNESS.get());
                entries.accept(ESSENCE_OF_FIRE.get());
                entries.accept(ESSENCE_OF_UNDEAD.get());
                entries.accept(ESSENCE_OF_LIGHT.get());

                entries.accept(UNICORN_HORN.get());
                entries.accept(WHIP.get());

                entries.accept(BONE_AMULET.get());
                entries.accept(FAIRY_AMULET.get());
                entries.accept(GHOST_AMULET.get());
                entries.accept(PEGASUS_AMULET.get());
                entries.accept(PET_AMULET.get());
            })
            .build());

    public static final Supplier<CreativeModeTab> MOHORSES_SPAWN_EGG_TAB = ModConstants.PLATFORM.registerCreativeModeTab("mohorses_spawn_eggs", () -> ModConstants.PLATFORM.newCreativeTabBuilder()
            .title(Component.translatable("itemGroup." + ModConstants.MODID + ".mohorses_spawn_eggs"))
            .icon(() -> new ItemStack(MAGGOT_SPAWN_EGG.get()))
            .displayItems((enabledFeatures, entries) -> {
                for (HorseKind kind : HorseKind.values()) {
                    entries.accept(HORSE_SPAWN_EGGS.get(kind).get());

                    // A Fairy Horse egg for every colour (white is the plain egg)
                    if (kind == HorseKind.FAIRY_HORSE) {
                        for (DyeColor color : DyeColor.values()) {
                            if (color != DyeColor.WHITE)
                                entries.accept(fairySpawnEgg(color));
                        }
                    }
                }

                entries.accept(WILD_NIGHTMARE_SPAWN_EGG.get());
                entries.accept(MAGGOT_SPAWN_EGG.get());
            })
            .build());

    /// Register an item
    private static <T extends Item> Supplier<T> register(String id, Function<Item.Properties, T> itemFactory) {
        return register(id, itemFactory, UnaryOperator.identity());
    }

    /// Register an item with custom [Item.Properties]
    private static <T extends Item> Supplier<T> register(String id, Function<Item.Properties, T> itemFactory, UnaryOperator<Item.Properties> properties) {
        return ModConstants.PLATFORM.registerItem(id, () -> itemFactory.apply(properties.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ModConstants.id(id))))));
    }
}
