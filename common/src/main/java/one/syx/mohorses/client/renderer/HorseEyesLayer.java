package one.syx.mohorses.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import one.syx.mohorses.ModConstants;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.mixin.client.RenderTypesAccessor;
import org.jetbrains.annotations.Nullable;

/// The eyes of the magical horses: they shine in the colour of the horse and sparkle now and then. The texture is white,
/// with nothing on it but the eyes.
public class HorseEyesLayer extends RenderLayer<MoHorseRenderState, EntityModel<? super MoHorseRenderState>> {
    private static final Identifier ADULT = ModConstants.id("textures/entity/eyes.png");
    private static final Identifier BABY = ModConstants.id("textures/entity/eyes_baby.png");
    // The Ghost Horse's texture is only half the size, it has neither horn nor wings
    private static final Identifier ADULT_SMALL = ModConstants.id("textures/entity/eyes_small.png");
    private static final Identifier BABY_SMALL = ModConstants.id("textures/entity/eyes_small_baby.png");

    public HorseEyesLayer(MoHorseRenderer renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, MoHorseRenderState state, float yRot, float xRot) {
        if (state.isInvisible || state.showsZombieHorse)
            return;

        int color = color(state);

        if (color == 0)
            return;

        // Two sparkles that never meet the same way twice, short and bright
        float age = state.ageInTicks + state.sparkleOffset;
        float sparkle = Math.max(flash(age * 0.083F), flash(age * 0.131F + 2.0F) * 0.8F);
        float strength = (0.8F + Mth.sin(age * 0.1F) * 0.08F + sparkle * 0.3F) * (0.45F + 0.55F * state.darkness);

        if (state.displayedKind.isGhost())
            strength *= Math.min(1.0F, state.ghostOpacity * 2.0F);

        boolean small = state.displayedKind == HorseKind.GHOST_HORSE;
        Identifier texture = small ? state.isBaby ? BABY_SMALL : ADULT_SMALL : state.isBaby ? BABY : ADULT;
        int tint = ARGB.color(Mth.clamp(strength, 0.0F, 1.0F), ARGB.srgbLerp(sparkle * 0.75F, ARGB.opaque(color), 0xFFFFFFFF));

        submitNodeCollector.order(2).submitModel(getParentModel(), state, poseStack, RenderTypesAccessor.mohorses$eyes().apply(texture),
                LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, tint, null, state.outlineColor, null);
    }

    /// Mostly 0, with a short peak of 1 each turn
    private static float flash(float angle) {
        float wave = Math.max(0.0F, Mth.sin(angle));
        float squared = wave * wave;

        return squared * squared * squared * squared * squared * squared;
    }

    /// 0 if its eyes don't shine
    private static int color(MoHorseRenderState state) {
        return switch (state.displayedKind) {
            case UNICORN -> 0xFFB43C;
            case FAIRY_HORSE -> fairyColor(state.fairyColor);
            case PEGASUS -> 0x6F9BFF;
            case DARK_PEGASUS -> 0xFF2A1C;
            case GHOST_HORSE -> 0x6FFFE6;
            case GHOST_PEGASUS -> 0x9FC8FF;
            default -> 0;
        };
    }

    private static int fairyColor(@Nullable DyeColor color) {
        if (color == null || color == DyeColor.WHITE)
            return 0xFFD36A;

        if (color == DyeColor.BLACK)
            return 0xA060FF;

        return color.getTextureDiffuseColor() & 0xFFFFFF;
    }
}
