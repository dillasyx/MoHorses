package one.syx.mohorses.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import one.syx.mohorses.mixin.client.RenderTypesAccessor;

/// The parts of the magical horses that shine in the dark, like a spider's eyes: the unicorn's horn and gold, the Fairy
/// Horse's horn and wings in their colour, the embers of the Nightmare, and the red eyes of the
/// Dark Pegasus. Ghosts shimmer faintly, only where it is dark. All of it is drawn from a `_glow` texture next to the
/// normal one. Nothing around them gets lighter.
public class HorseGlowLayer extends RenderLayer<MoHorseRenderState, EntityModel<? super MoHorseRenderState>> {
    private final MoHorseRenderer renderer;

    public HorseGlowLayer(MoHorseRenderer renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, MoHorseRenderState state, float yRot, float xRot) {
        if (state.isInvisible || state.showsZombieHorse)
            return;

        float strength = strength(state);

        if (strength <= 0.0F)
            return;

        submitNodeCollector.order(1).submitModel(getParentModel(), state, poseStack,
                RenderTypesAccessor.mohorses$eyes().apply(this.renderer.getTextureLocation(state).withPath(path -> path.replace(".png", "_glow.png"))),
                LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ARGB.white(Math.min(strength, 1.0F)), null, state.outlineColor, null);
    }

    /// How bright it shines right now, from 0 (not at all) to 1
    private static float strength(MoHorseRenderState state) {
        float age = state.ageInTicks;

        return switch (state.displayedKind) {
            // A slow pulse; at full power while charging
            case UNICORN -> state.charging ? 1.0F : 0.7F + Mth.sin(age * 0.08F) * 0.2F;
            case FAIRY_HORSE -> 0.9F + Mth.sin(age * 0.06F) * 0.1F;
            // Flickering like embers; blazing after it was fed redstone
            case NIGHTMARE -> (state.ablaze ? 0.95F : 0.65F) + Mth.sin(age * 0.37F) * 0.1F + Mth.sin(age * 0.91F + 1.7F) * 0.08F;
            // Smouldering, the eyes burning red
            case DARK_PEGASUS -> 0.8F + Mth.sin(age * 0.07F) * 0.15F;
            // As faint as the ghost itself, and by day not at all
            case GHOST_HORSE, GHOST_PEGASUS -> state.ghostOpacity * state.darkness * (0.75F + Mth.sin(age * 0.05F) * 0.15F + Mth.sin(age * 0.13F + 0.8F) * 0.1F);
            default -> 0.0F;
        };
    }
}
