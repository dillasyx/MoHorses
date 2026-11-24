package one.syx.mohorses.mixin.client;

import java.util.function.Function;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/// Forge lets RenderTypes#eyes return its render type for text, which drops everything fainter than a tenth: soft gradients
/// would end in a hard edge. This is the one the game means, on every loader.
@Mixin(RenderTypes.class)
public interface RenderTypesAccessor {
    @Accessor("EYES")
    static Function<Identifier, RenderType> mohorses$eyes() {
        throw new AssertionError();
    }
}
