package one.syx.mohorses.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.TypedEntityData;

/// Tooltip image of a full amulet: the stored animal, drawn by the client as a small preview
public record AmuletTooltip(TypedEntityData<EntityType<?>> data) implements TooltipComponent {
}
