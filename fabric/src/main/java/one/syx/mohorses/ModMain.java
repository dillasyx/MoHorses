package one.syx.mohorses;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.entity.MoHorse;
import one.syx.mohorses.entity.WildNightmare;
import one.syx.mohorses.event.ComposterListener;
import one.syx.mohorses.registry.EntityRegistry;

/// Initializer class for Fabric-specific setup tasks
public final class ModMain implements ModInitializer {
    @Override
    public void onInitialize() {
        ModCommon.init();
        EntityRegistry.registerEntityAttributes(FabricDefaultAttributeRegistry::register);

        // Zebras spawn in the biomes of the mohorses:spawns_zebras tag (savannas, badlands and the desert by default)
        EntityType<MoHorse> zebra = EntityRegistry.horse(HorseKind.ZEBRA).get();

        SpawnPlacements.register(zebra, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MoHorse::checkZebraSpawnRules);
        BiomeModifications.addSpawn(BiomeSelectors.tag(TagKey.create(Registries.BIOME, ModConstants.id("spawns_zebras"))), MobCategory.CREATURE, zebra, 6, 2, 5);

        // Hostile Nightmares roam the biomes of the mohorses:spawns_wild_nightmares tag (the whole Nether by default)
        SpawnPlacements.register(EntityRegistry.WILD_NIGHTMARE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WildNightmare::checkSpawnRules);
        BiomeModifications.addSpawn(BiomeSelectors.tag(TagKey.create(Registries.BIOME, ModConstants.id("spawns_wild_nightmares"))), MobCategory.MONSTER, EntityRegistry.WILD_NIGHTMARE.get(), 10, 1, 2);

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            ComposterListener.onBlockUsed(level, hitResult.getBlockPos(), player.getItemInHand(hand));

            return InteractionResult.PASS;
        });
    }
}
