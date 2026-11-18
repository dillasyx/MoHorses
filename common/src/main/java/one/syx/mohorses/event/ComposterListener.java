package one.syx.mohorses.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.component.DataComponents;
import one.syx.mohorses.entity.MaggotEntity;
import one.syx.mohorses.registry.EntityRegistry;

/// Platform-agnostic part of the "maggots crawl out of composters" mechanic.
/// Each loader calls [#onBlockUsed] from its own right-click-block event.
public final class ComposterListener {
    public static final float MAGGOT_SPAWN_CHANCE = 1F / 64F;

    public static void onBlockUsed(Level level, BlockPos pos, ItemStack usedStack) {
        if (level.isClientSide() || usedStack.isEmpty() || !level.getBlockState(pos).is(Blocks.COMPOSTER))
            return;

        if (!usedStack.has(DataComponents.COMPOSTABLE) || level.getRandom().nextFloat() >= MAGGOT_SPAWN_CHANCE)
            return;

        MaggotEntity maggot = EntityRegistry.MAGGOT.get().create(level, EntitySpawnReason.TRIGGERED);

        if (maggot != null) {
            maggot.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            level.addFreshEntity(maggot);
        }
    }
}
