package one.syx.mohorses.mixin.client;

import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// A tooltip pushed up against the bottom of the screen keeps only 3 pixels below its content, but its frame reaches
/// further down, so the bottom line of the frame gets cut off. Tall tooltips such as the amulet preview hit this
/// near the hotbar. Such a tooltip is moved up a little more, so its whole frame stays on screen.
@Mixin(DefaultTooltipPositioner.class)
public abstract class DefaultTooltipPositionerMixin {
    private static final int FRAME_ROOM = 5;

    @Inject(method = "positionTooltip(IIIIII)Lorg/joml/Vector2ic;", at = @At("RETURN"))
    private void mohorses$keepFrameOnScreen(int screenWidth, int screenHeight, int x, int y, int tooltipWidth, int tooltipHeight,
                                           CallbackInfoReturnable<Vector2ic> cir) {
        if (cir.getReturnValue() instanceof Vector2i position && position.y + tooltipHeight + FRAME_ROOM > screenHeight)
            position.y = screenHeight - tooltipHeight - FRAME_ROOM;
    }
}
