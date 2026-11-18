package one.syx.mohorses.entity;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import one.syx.mohorses.ModConstants;
import one.syx.mohorses.registry.ItemRegistry;
import org.jetbrains.annotations.Nullable;

/// Every horse this mod adds. All kinds share the [MoHorse] class, and each kind has its own entity type.
///
/// This enum is the single place that describes what a kind looks like and what it can do.
/// The breeding and essence rules live at the bottom of this file.
public enum HorseKind {
    //          id               shape          armor               fly    fire   food   donkey  textures
    ZEBRA(      "zebra",         Shape.HORSE,   ArmorRule.NONE,     false, false, true,  false, "zebra"),
    ZORSE(      "zorse",         Shape.HORSE,   ArmorRule.NORMAL,   false, false, false, false, "zorse"),
    ZONKEY(     "zonkey",        Shape.DONKEY,  ArmorRule.NONE,     false, false, false, true,  "zonkey"),
    NIGHTMARE(  "nightmare",     Shape.HORSE,   ArmorRule.NONE,     false, true,  true,  false, "nightmare/nightmare0", "nightmare/nightmare1", "nightmare/nightmare2", "nightmare/nightmare3", "nightmare/nightmare4"),
    BAT_HORSE(  "bat_horse",     Shape.WINGED,  ArmorRule.CRYSTAL,  true,  false, false, false, "bat_horse"),
    UNICORN(    "unicorn",       Shape.UNICORN, ArmorRule.CRYSTAL,  false, false, false, false, "unicorn"),
    PEGASUS(    "pegasus",       Shape.WINGED,  ArmorRule.CRYSTAL,  true,  false, false, false, "pegasus"),
    DARK_PEGASUS("dark_pegasus", Shape.WINGED,  ArmorRule.CRYSTAL,  true,  true,  false, false, "dark_pegasus"),
    FAIRY_HORSE("fairy_horse",   Shape.FAIRY,   ArmorRule.CRYSTAL,  true,  false, false, false, "fairy_horse/white"),
    // The undead rot away into skeletons, like vanilla zombie horses (see ZombieHorseDecay)
    UNDEAD_UNICORN("undead_unicorn",     Shape.UNICORN, ArmorRule.CRYSTAL, false, false, false, false, "undead_unicorn/base"),
    UNDEAD_PEGASUS("undead_pegasus",     Shape.WINGED,  ArmorRule.CRYSTAL, true,  false, false, false, "undead_pegasus/base"),
    SKELETON_UNICORN("skeleton_unicorn", Shape.UNICORN, ArmorRule.CRYSTAL, false, false, false, false, "skeleton_unicorn/skeleton"),
    SKELETON_PEGASUS("skeleton_pegasus", Shape.WINGED,  ArmorRule.CRYSTAL, true,  false, false, false, "skeleton_pegasus/skeleton"),
    // What may be left of a tamed horse when it dies (see GhostHorses). Translucent, and they flicker.
    GHOST_HORSE("ghost_horse",           Shape.HORSE,   ArmorRule.CRYSTAL, false, false, false, false, "ghost_horse/ghost"),
    GHOST_PEGASUS("ghost_pegasus",       Shape.WINGED,  ArmorRule.CRYSTAL, true,  false, false, false, "ghost_pegasus/ghost");

    /// Which horse armor a kind can wear: normal horses only the vanilla ones, the special horses only crystal armor
    public enum ArmorRule { NORMAL, CRYSTAL, NONE }

    /// Which body the kind is drawn with. All shapes are the vanilla horse model; the extra parts and the
    /// texture sheet size differ. Winged and horned shapes use a 128x128 sheet with the vanilla layout in its top-left quarter.
    public enum Shape {
        /// The plain vanilla horse, 64x64 texture
        HORSE(64),
        /// The vanilla donkey (long ears, slightly smaller), 64x64 texture
        DONKEY(64),
        /// Horse with feathered three-part wings
        WINGED(128),
        /// Horse with a horn
        UNICORN(128),
        /// Horse with a horn and butterfly wings
        FAIRY(128);

        private final int textureSize;

        Shape(int textureSize) {
            this.textureSize = textureSize;
        }

        public int textureSize() {
            return this.textureSize;
        }
    }

    private final String id;
    private final Shape shape;
    private final Identifier[] textures;
    private final Identifier[] foalTextures;
    private final ArmorRule armorRule;
    private final boolean canFly;
    private final boolean fireImmune;
    private final boolean breedsWithFood;
    private final boolean donkeyLike;

    HorseKind(String id, Shape shape, ArmorRule armorRule, boolean canFly, boolean fireImmune, boolean breedsWithFood, boolean donkeyLike, String... textures) {
        this.id = id;
        this.shape = shape;
        this.armorRule = armorRule;
        this.canFly = canFly;
        this.fireImmune = fireImmune;
        this.breedsWithFood = breedsWithFood;
        this.donkeyLike = donkeyLike;
        this.textures = new Identifier[textures.length];
        this.foalTextures = new Identifier[textures.length];

        for (int i = 0; i < textures.length; i++) {
            this.textures[i] = ModConstants.id("textures/entity/" + textures[i] + ".png");
            this.foalTextures[i] = ModConstants.id("textures/entity/" + textures[i] + "_baby.png");
        }
    }

    /// Registry path of the entity type, also used for the spawn egg and lang keys
    public String id() {
        return this.id;
    }

    /// The body this kind is drawn with
    public Shape shape() {
        return this.shape;
    }

    /// The texture to show. Kinds with several textures (Nightmare) cycle through them.
    /// Foals use the `_baby` variant, laid out for the vanilla foal model.
    public Identifier texture(double ageInTicks, boolean foal) {
        Identifier[] textures = foal ? this.foalTextures : this.textures;

        if (textures.length == 1)
            return textures[0];

        return textures[(int)(Math.abs(ageInTicks) / 3) % textures.length];
    }

    /// How big a foal of this kind is compared to an adult, the same as the vanilla horse (0.7) or donkey (0.5)
    public float foalScale() {
        return this.shape == Shape.DONKEY ? 0.5F : 0.7F;
    }

    public boolean canFly() {
        return this.canFly;
    }

    public boolean fireImmune() {
        return this.fireImmune;
    }

    /// Whether golden apples/carrots put this kind in love mode, like vanilla horses
    public boolean breedsWithFood() {
        return this.breedsWithFood;
    }

    public boolean donkeyLike() {
        return this.donkeyLike;
    }

    public ArmorRule armorRule() {
        return this.armorRule;
    }

    public boolean canWear(ItemStack armor) {
        return switch (this.armorRule) {
            case NORMAL -> !armor.is(ItemRegistry.CRYSTAL_HORSE_ARMOR.get());
            case CRYSTAL -> armor.is(ItemRegistry.CRYSTAL_HORSE_ARMOR.get());
            case NONE -> false;
        };
    }

    // ------------------------------------------------------------------------------------------------
    // Breeding
    // ------------------------------------------------------------------------------------------------

    /// What a horse of this kind produces with the given partner, or null if the two can't breed.
    ///
    /// - Zebra + Zebra = Zebra, Zebra + Horse = Zorse, Zebra + Donkey = Zonkey (Zorse and Zonkey are sterile)
    /// - Nightmare + Nightmare = Nightmare, Nightmare + Pegasus = Dark Pegasus
    /// - Bat Horse + Bat Horse = Bat Horse, Unicorn + Unicorn = Unicorn
    /// - Unicorn + Pegasus fed Essence of Light = Fairy Horse (the parents are used up, the Pegasus comes back in a Pegasus Amulet)
    /// - Unicorn + Pegasus fed a pumpkin = Nightmare
    /// - Fairy Horse + Fairy Horse = Fairy Horse (the parents vanish)
    ///
    /// @param pumpkinFed whether either parent was put in love mode with a pumpkin
    public @Nullable HorseKind offspringWith(Animal partner, boolean pumpkinFed) {
        if (partner instanceof MoHorse other) {
            HorseKind otherKind = other.kind();

            if (this == otherKind) {
                return switch (this) {
                    case ZEBRA, NIGHTMARE, BAT_HORSE, UNICORN, FAIRY_HORSE -> this;
                    default -> null;
                };
            }

            if (isPair(otherKind, UNICORN, PEGASUS))
                return pumpkinFed ? NIGHTMARE : FAIRY_HORSE;

            if (isPair(otherKind, NIGHTMARE, PEGASUS))
                return DARK_PEGASUS;

            return null;
        }

        if (this != ZEBRA)
            return null;

        if (partner instanceof Donkey)
            return ZONKEY;

        if (partner instanceof Horse)
            return ZORSE;

        return null;
    }

    private boolean isPair(HorseKind other, HorseKind a, HorseKind b) {
        return (this == a && other == b) || (this == b && other == a);
    }

    /// Whether the parents disappear when this kind is born
    public boolean consumesParents() {
        return this == FAIRY_HORSE;
    }

    /// Whether a pumpkin puts this kind in love mode (for breeding Nightmares from a Unicorn and a Pegasus)
    public boolean lovesPumpkin() {
        return this == UNICORN || this == PEGASUS;
    }

    // ------------------------------------------------------------------------------------------------
    // Undead
    // ------------------------------------------------------------------------------------------------

    /// Undead and skeleton kinds: hurt by healing, healed by harming, and mobs leave
    /// their riders alone. The two with flesh on them (Undead Unicorn, Undead Pegasus) burn in daylight, through the
    /// vanilla `minecraft:burn_in_daylight` entity tag.
    public boolean isUndead() {
        return this == UNDEAD_UNICORN || this == UNDEAD_PEGASUS || this == SKELETON_UNICORN || this == SKELETON_PEGASUS;
    }

    /// The skeleton this kind rots into, or null if it doesn't rot. These still have flesh on them, so maggots
    /// crawl out when they die.
    public @Nullable HorseKind decaysInto() {
        return switch (this) {
            case UNDEAD_UNICORN -> SKELETON_UNICORN;
            case UNDEAD_PEGASUS -> SKELETON_PEGASUS;
            default -> null;
        };
    }

    /// The plain kinds, which Essence of Undead turns into vanilla zombie horses
    public boolean becomesZombieHorse() {
        return this == ZEBRA || this == ZORSE || this == ZONKEY || this == NIGHTMARE;
    }

    // ------------------------------------------------------------------------------------------------
    // Ghosts
    // ------------------------------------------------------------------------------------------------

    public boolean isGhost() {
        return this == GHOST_HORSE || this == GHOST_PEGASUS;
    }

    /// Kinds that fall slowly and take no fall damage: ghost horses without wings sink, unicorns (also undead and
    /// skeleton ones) glide down like in Mo' Creatures
    public boolean floats() {
        return this == GHOST_HORSE || this.shape == Shape.UNICORN;
    }

    /// How much a floating kind's fall is slowed down every tick (lower is slower)
    public float fallSlowdown() {
        return this == GHOST_HORSE ? 0.6F : 0.8F;
    }

    /// The ghost a tamed horse of this kind may leave behind when it dies, or null if it leaves none.
    /// Like in Mo' Creatures only the rare horses come back: the winged ones as a Ghost Pegasus, the bred and
    /// transformed ones without wings (Zorse, Zonkey, Nightmare, Unicorn) as a Ghost Horse. Zebras, vanilla horses,
    /// the undead and the ghosts themselves leave none.
    public @Nullable HorseKind ghost() {
        return switch (this) {
            case BAT_HORSE, PEGASUS, DARK_PEGASUS, FAIRY_HORSE -> GHOST_PEGASUS;
            case ZORSE, ZONKEY, NIGHTMARE, UNICORN -> GHOST_HORSE;
            default -> null;
        };
    }

    // ------------------------------------------------------------------------------------------------
    // Amulets
    // ------------------------------------------------------------------------------------------------

    public enum Amulet { PET, PEGASUS, FAIRY, BONE, GHOST }

    /// Which amulet can hold a horse of this kind. Like in Mo' Creatures, Pegasi and Fairy Horses have their own amulets;
    /// all the other kinds fit in a Pet Amulet.
    public Amulet amulet() {
        return switch (this) {
            case UNDEAD_UNICORN, UNDEAD_PEGASUS, SKELETON_UNICORN, SKELETON_PEGASUS -> Amulet.BONE;
            case GHOST_HORSE, GHOST_PEGASUS -> Amulet.GHOST;
            case PEGASUS, DARK_PEGASUS -> Amulet.PEGASUS;
            case FAIRY_HORSE -> Amulet.FAIRY;
            default -> Amulet.PET;
        };
    }

    // ------------------------------------------------------------------------------------------------
    // Essences
    // ------------------------------------------------------------------------------------------------

    /// The kind this horse turns into when the given essence is used on it, or null if nothing happens.
    ///
    /// - Zorse + Essence of Darkness (standing on the ground above the clouds) = Bat Horse
    /// - Zorse + Essence of Fire = Nightmare
    /// - Nightmare + Essence of Light = Unicorn
    /// - Bat Horse + Essence of Light (standing on the ground above the clouds) = Pegasus
    /// - Pegasus + Essence of Darkness (standing on the ground above the clouds) = Dark Pegasus
    /// - Unicorn or Fairy Horse + Essence of Undead = Undead Unicorn
    /// - Pegasus, Dark Pegasus or Bat Horse + Essence of Undead = Undead Pegasus
    /// - Skeleton Unicorn / Skeleton Pegasus + Essence of Light = Unicorn / Pegasus (vanilla skeleton horses: VanillaHorseEssences)
    ///
    /// @param inTheSky whether the horse stands on the ground above the clouds
    public @Nullable HorseKind transformedBy(ItemStack essence, boolean inTheSky) {
        return switch (this) {
            case ZORSE -> essence.is(darkness()) && inTheSky ? BAT_HORSE : essence.is(fire()) ? NIGHTMARE : null;
            case NIGHTMARE -> essence.is(light()) ? UNICORN : null;
            case UNICORN, FAIRY_HORSE -> essence.is(undead()) ? UNDEAD_UNICORN : null;
            case BAT_HORSE -> essence.is(undead()) ? UNDEAD_PEGASUS : essence.is(light()) && inTheSky ? PEGASUS : null;
            case PEGASUS -> essence.is(undead()) ? UNDEAD_PEGASUS : essence.is(darkness()) && inTheSky ? DARK_PEGASUS : null;
            case DARK_PEGASUS -> essence.is(undead()) ? UNDEAD_PEGASUS : null;
            // Essence of Light gives the skeletons their flesh back
            case SKELETON_UNICORN -> essence.is(light()) ? UNICORN : null;
            case SKELETON_PEGASUS -> essence.is(light()) ? PEGASUS : null;
            default -> null;
        };
    }

    /// Whether the essence would transform this kind, if only the horse stood on the ground high enough
    public boolean needsSkyFor(ItemStack essence) {
        return (this == ZORSE && essence.is(darkness())) || (this == BAT_HORSE && essence.is(light())) || (this == PEGASUS && essence.is(darkness()));
    }

    /// Whether the essence heals this kind
    public boolean healedBy(ItemStack essence) {
        return switch (this) {
            case UNICORN, PEGASUS -> essence.is(light());
            case BAT_HORSE, DARK_PEGASUS -> essence.is(darkness());
            case NIGHTMARE -> essence.is(fire());
            // Like in Mo' Creatures, Essence of Undead mends the undead and the ghosts
            case UNDEAD_UNICORN, UNDEAD_PEGASUS, SKELETON_UNICORN, SKELETON_PEGASUS, GHOST_HORSE, GHOST_PEGASUS -> essence.is(undead());
            default -> false;
        };
    }

    /// Whether the essence puts this kind in love mode
    public boolean lovesEssence(ItemStack essence) {
        return switch (this) {
            case BAT_HORSE -> essence.is(darkness());
            case UNICORN, PEGASUS, FAIRY_HORSE -> essence.is(light());
            default -> false;
        };
    }

    private static Item light() {
        return ItemRegistry.ESSENCE_OF_LIGHT.get();
    }

    private static Item darkness() {
        return ItemRegistry.ESSENCE_OF_DARKNESS.get();
    }

    private static Item undead() {
        return ItemRegistry.ESSENCE_OF_UNDEAD.get();
    }

    private static Item fire() {
        return ItemRegistry.ESSENCE_OF_FIRE.get();
    }
}
