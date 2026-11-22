package one.syx.mohorses.mixin.client;

import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.util.Mth;
import one.syx.mohorses.client.renderer.NuzzlePose;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// The egg nuzzling (see EggNuzzle), from the old GeckoLib horse animation: the head turns down and round under the
/// belly at its usual pivot and nudges there, the tail swishes. One second, played three times. On the base model, so
/// the horses of this mod, vanilla horses, their saddles and armor all do it.
@Mixin(AbstractEquineModel.class)
public abstract class AbstractEquineModelMixin {
    @Unique
    private static final int MOHORSES$RUNS = 3;
    /// The point the head turns round, in model space (y down from 24 pixels above the ground). The GeckoLib pivot was
    /// [0, 16, -7.5] on a body from z -11 to 11; the vanilla body sits one pixel further forward (z -12 to 10).
    @Unique
    private static final float MOHORSES$TURN_Y = 24.0F - 16.0F;
    @Unique
    private static final float MOHORSES$TURN_Z = -7.5F - 1.0F;

    @Shadow
    @Final
    protected ModelPart headParts;

    @Shadow
    @Final
    private ModelPart tail;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/EquineRenderState;)V", at = @At("TAIL"))
    private void mohorses$nuzzle(EquineRenderState state, CallbackInfo ci) {
        float progress = ((NuzzlePose)state).mohorses$getNuzzle();

        if (progress < 0.0F)
            return;

        // The time within the current run, in seconds of the one-second animation
        float time = progress * MOHORSES$RUNS;

        time -= Mth.floor(time);

        mohorses$turnHead(this.headParts, Mth.DEG_TO_RAD * mohorses$headDegrees(time));
        this.tail.yRot += Mth.DEG_TO_RAD * mohorses$tailDegrees(time, true);
        this.tail.zRot += Mth.DEG_TO_RAD * mohorses$tailDegrees(time, false);
    }

    /// Turns the head on top of its normal pose, but round the point the old GeckoLib model turned it at: low in the
    /// chest (16 pixels above the ground, 7.5 behind the front), not the vanilla pivot up at the top of the neck. The
    /// rest pose stays as it is; the head's pivot is moved along so that the extra turn happens round that point.
    @Unique
    private static void mohorses$turnHead(ModelPart head, float angle) {
        float dy = head.y - MOHORSES$TURN_Y;
        float dz = head.z - MOHORSES$TURN_Z;
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);

        head.y = MOHORSES$TURN_Y + dy * cos - dz * sin;
        head.z = MOHORSES$TURN_Z + dy * sin + dz * cos;
        head.xRot += angle;
    }

    /// head_parts x rotation: 0 → 190 (easeOutCirc) → 175 → 190 → 0 (easeInCirc), keyframes every quarter second
    @Unique
    private static float mohorses$headDegrees(float time) {
        if (time < 0.25F) {
            float t = time / 0.25F;

            return 190.0F * Mth.sqrt(1.0F - (t - 1.0F) * (t - 1.0F));
        }

        if (time < 0.5F)
            return Mth.lerp((time - 0.25F) / 0.25F, 190.0F, 175.0F);

        if (time < 0.75F)
            return Mth.lerp((time - 0.5F) / 0.25F, 175.0F, 190.0F);

        // 190 + (0 - 190) * easeInCirc(t), with easeInCirc(t) = 1 - sqrt(1 - t²)
        float t = (time - 0.75F) / 0.25F;

        return 190.0F * Mth.sqrt(1.0F - t * t);
    }

    /// tail y rotation (or z, a little): swishing to one side, to the other and back to the middle
    @Unique
    private static float mohorses$tailDegrees(float time, boolean yaw) {
        float[] times = {0.1667F, 0.2917F, 0.4583F, 0.5833F, 0.75F, 0.875F};
        float[] values = yaw ? new float[] {0.0F, 60.0F, 0.0F, -60.0F, 10.0F, 0.0F} : new float[] {0.0F, -5.0F, 0.0F, 5.0F, 0.0F, 0.0F};

        if (time <= times[0] || time >= times[times.length - 1])
            return 0.0F;

        for (int i = 1; i < times.length; i++) {
            if (time < times[i])
                return Mth.lerp((time - times[i - 1]) / (times[i] - times[i - 1]), values[i - 1], values[i]);
        }

        return 0.0F;
    }
}
