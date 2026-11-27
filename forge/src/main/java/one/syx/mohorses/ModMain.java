package one.syx.mohorses;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.entity.MoHorse;
import one.syx.mohorses.entity.WildNightmare;
import one.syx.mohorses.entity.WildUnicorn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import one.syx.mohorses.event.ComposterListener;
import one.syx.mohorses.registry.EntityRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/// Initializer class for Forge-specific setup tasks
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

    public ModMain(FMLJavaModLoadingContext context) {
        final BusGroup busGroup = context.getModBusGroup();

        SOUND_EVENT_REGISTRY.register(busGroup);
        PARTICLE_REGISTRY.register(busGroup);
        ENTITY_REGISTRY.register(busGroup);
        CREATIVE_TAB_REGISTRY.register(busGroup);
        LOOT_CONDITION_REGISTRY.register(busGroup);
        TRIGGER_REGISTRY.register(busGroup);
        ITEM_REGISTRY.register(busGroup);

        EntityAttributeCreationEvent.BUS.addListener(event -> EntityRegistry.registerEntityAttributes(event::put));
        // Where zebras and wild Nightmares spawn is set in data/mohorses/forge/biome_modifier, these are their spawn rules
        SpawnPlacementRegisterEvent.BUS.addListener(event -> {
            event.register(EntityRegistry.horse(HorseKind.ZEBRA).get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    MoHorse::checkZebraSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(EntityRegistry.WILD_NIGHTMARE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    WildNightmare::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(EntityRegistry.WILD_UNICORN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    WildUnicorn::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        });
        LootTableLoadEvent.BUS.addListener(ModMain::addExtraDrops);
        PlayerInteractEvent.RightClickBlock.BUS.addListener((Consumer<PlayerInteractEvent.RightClickBlock>)event ->
                ComposterListener.onBlockUsed(event.getLevel(), event.getPos(), event.getItemStack()));

        ModCommon.init();
    }

    private static void addExtraDrops(LootTableLoadEvent event) {
        final LootTable table = event.getTable();

        if (table == null)
            return;

        for (ExtraDrop drop : EXTRA_DROPS) {
            if (event.getName().equals(drop.lootTableId())) {
                table.addPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(drop.item().get())
                                .when(drop::condition))
                        .build());
            }
        }
    }
}
