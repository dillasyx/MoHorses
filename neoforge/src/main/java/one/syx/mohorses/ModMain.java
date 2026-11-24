package one.syx.mohorses;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.entity.MoHorse;
import one.syx.mohorses.entity.WildNightmare;
import one.syx.mohorses.entity.WildUnicorn;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import one.syx.mohorses.event.ComposterListener;
import one.syx.mohorses.registry.EntityRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/// Initializer class for NeoForge-specific setup tasks
@Mod(ModConstants.MODID)
public final class ModMain {
    public static final DeferredRegister<SoundEvent> SOUND_EVENT_REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, ModConstants.MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_REGISTRY = DeferredRegister.create(Registries.PARTICLE_TYPE, ModConstants.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, ModConstants.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TAB_REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModConstants.MODID);
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITION_REGISTRY = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, ModConstants.MODID);
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_REGISTRY = DeferredRegister.create(Registries.TRIGGER_TYPE, ModConstants.MODID);
    public static final DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(Registries.ITEM, ModConstants.MODID);

    /// Extra drops for vanilla loot tables, collected from the common code and applied when the tables load
    static final List<ExtraDrop> EXTRA_DROPS = new ArrayList<>();

    record ExtraDrop(Identifier lootTableId, Supplier<? extends Item> item, LootItemCondition condition) {}

    public ModMain(IEventBus modBus, ModContainer modContainer) {
        SOUND_EVENT_REGISTRY.register(modBus);
        PARTICLE_REGISTRY.register(modBus);
        ENTITY_REGISTRY.register(modBus);
        CREATIVE_TAB_REGISTRY.register(modBus);
        LOOT_CONDITION_REGISTRY.register(modBus);
        TRIGGER_REGISTRY.register(modBus);
        ITEM_REGISTRY.register(modBus);

        modBus.<EntityAttributeCreationEvent>addListener(event -> EntityRegistry.registerEntityAttributes(event::put));
        // Where zebras and wild Nightmares spawn is set in data/mohorses/neoforge/biome_modifier, these are their spawn rules
        modBus.<RegisterSpawnPlacementsEvent>addListener(event -> {
            event.register(EntityRegistry.horse(HorseKind.ZEBRA).get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    MoHorse::checkZebraSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(EntityRegistry.WILD_NIGHTMARE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    WildNightmare::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(EntityRegistry.WILD_UNICORN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    WildUnicorn::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        });
        NeoForge.EVENT_BUS.addListener(LootTableLoadEvent.class, ModMain::addExtraDrops);
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, event ->
                ComposterListener.onBlockUsed(event.getLevel(), event.getPos(), event.getItemStack()));

        ModCommon.init();
    }

    private static void addExtraDrops(LootTableLoadEvent event) {
        for (ExtraDrop drop : EXTRA_DROPS) {
            if (event.getName().equals(drop.lootTableId())) {
                event.getTable().addPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(drop.item().get())
                                .when(drop::condition))
                        .build());
            }
        }
    }
}
