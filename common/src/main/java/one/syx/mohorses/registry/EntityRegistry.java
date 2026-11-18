package one.syx.mohorses.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.monster.Monster;
import one.syx.mohorses.ModConstants;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.entity.MaggotEntity;
import one.syx.mohorses.entity.MoHorse;
import one.syx.mohorses.entity.WildNightmare;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class EntityRegistry {
    public static void init() {}

    public static final Supplier<EntityType<MaggotEntity>> MAGGOT = register("maggot", () -> EntityType.Builder.of(MaggotEntity::new, MobCategory.MONSTER).sized(0.7f, 0.4f));

    /// Hostile Nightmares of the Nether. They use the Nightmare look, but are monsters: no taming, and they despawn.
    public static final Supplier<EntityType<WildNightmare>> WILD_NIGHTMARE = register("wild_nightmare", () -> EntityType.Builder.of(WildNightmare::new, MobCategory.MONSTER)
            .sized(1.3964844F, 1.6F)
            .eyeHeight(1.52F)
            .passengerAttachments(1.44375F)
            .clientTrackingRange(10)
            .fireImmune()
            .notInPeaceful());

    private static final Map<HorseKind, Supplier<EntityType<MoHorse>>> HORSES = new EnumMap<>(HorseKind.class);

    static {
        for (HorseKind kind : HorseKind.values()) {
            HORSES.put(kind, register(kind.id(), () -> horseBuilder(kind)));
        }
    }

    /// The entity type of the given horse kind
    public static Supplier<EntityType<MoHorse>> horse(HorseKind kind) {
        return HORSES.get(kind);
    }

    private static EntityType.Builder<MoHorse> horseBuilder(HorseKind kind) {
        EntityType.EntityFactory<MoHorse> factory = (type, level) -> new MoHorse(type, level, kind);
        // Same hitbox as the vanilla horse, or the vanilla donkey for donkey-like kinds
        EntityType.Builder<MoHorse> builder = EntityType.Builder.of(factory, MobCategory.CREATURE)
                .sized(1.3964844F, kind.donkeyLike() ? 1.5F : 1.6F)
                .eyeHeight(kind.donkeyLike() ? 1.425F : 1.52F)
                .passengerAttachments(kind.donkeyLike() ? 1.1125F : 1.44375F)
                .clientTrackingRange(10);

        return kind.fireImmune() ? builder.fireImmune() : builder;
    }

    public static void registerEntityAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier> registrar) {
        registrar.accept(MAGGOT.get(), Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 16)
                .add(Attributes.MAX_HEALTH, 4)
                .add(Attributes.MOVEMENT_SPEED, 0.25f)
                .build());

        registrar.accept(WILD_NIGHTMARE.get(), WildNightmare.createAttributes().build());

        for (HorseKind kind : HorseKind.values()) {
            AttributeSupplier.Builder attributes = AbstractChestedHorse.createBaseChestedHorseAttributes();

            // Winged and floating horses never take fall damage
            if (kind.canFly() || kind.floats())
                attributes.add(Attributes.FALL_DAMAGE_MULTIPLIER, 0);

            registrar.accept(horse(kind).get(), attributes.build());
        }
    }

    private static <T extends Entity> Supplier<EntityType<T>> register(String id, Supplier<EntityType.Builder<T>> builder) {
        return ModConstants.PLATFORM.registerEntity(id, () -> builder.get().build(ResourceKey.create(Registries.ENTITY_TYPE, ModConstants.id(id))));
    }
}
