package one.syx.mohorses.client.model;

import net.minecraft.client.model.animal.equine.EquineSaddleModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.util.Mth;
import one.syx.mohorses.client.renderer.MoHorseRenderState;

/// The poses of this mod on top of the vanilla horse animation: ghosts and flying horses with their legs swept back, zebras shuffling,
/// unicorns charging head down. The horse model, its saddle and its armor all apply them, so the gear moves with the horse.
public final class HorsePoses {
    /// How fast zebras shuffle (radians of the dance per tick)
    public static final float SHUFFLE_TEMPO = 0.45F;
    /// How far a charging unicorn lowers its head
    private static final float CHARGE_HEAD_DOWN = 0.75F;

    public static void apply(MoHorseRenderState state, ModelPart head, ModelPart leftFrontLeg, ModelPart rightFrontLeg, ModelPart leftHindLeg, ModelPart rightHindLeg) {
        // Ghosts don't walk: like in Mo' Creatures they hover a little above the ground with their legs swept back,
        // drifting gently. Walking only makes the legs sway a bit further back. Winged horses do the same in flight.
        if (state.legSweep > 0.0F) {
            float sweep = (float)Mth.smoothstep(state.legSweep);
            float walk = Math.min(state.walkAnimationSpeed, 1.0F) * 0.25F;
            float leftFront, rightFront, leftHind, rightHind;

            if (state.floating) {
                float drift = Mth.sin(state.ageInTicks * 0.08F) * 0.06F;

                leftFront = drift;
                rightFront = -drift;
                leftHind = -drift;
                rightHind = drift;
            }
            else {
                // In flight they paddle along, one leg after the other, wider the harder the wings beat
                float phase = state.ageInTicks * 0.15F;
                float stroke = Mth.lerp(state.wingBeat, 0.16F, 0.3F);

                leftFront = Mth.sin(phase) * stroke;
                rightFront = Mth.sin(phase - 1.9F) * stroke;
                leftHind = Mth.sin(phase - 3.4F) * stroke;
                rightHind = Mth.sin(phase - 5.0F) * stroke;
            }

            leftFrontLeg.xRot = Mth.lerp(sweep, leftFrontLeg.xRot, 0.85F + walk + leftFront);
            rightFrontLeg.xRot = Mth.lerp(sweep, rightFrontLeg.xRot, 0.95F + walk + rightFront);
            leftHindLeg.xRot = Mth.lerp(sweep, leftHindLeg.xRot, 0.55F + walk + leftHind);
            rightHindLeg.xRot = Mth.lerp(sweep, rightHindLeg.xRot, 0.45F + walk + rightHind);
        }

        // The shuffle: stepping from one diagonal pair of legs to the other, the head nodding to the beat
        if (state.shuffling) {
            float beat = state.ageInTicks * SHUFFLE_TEMPO;
            float step = Mth.sin(beat) * 0.45F;

            leftFrontLeg.xRot = step;
            rightHindLeg.xRot = step;
            rightFrontLeg.xRot = -step;
            leftHindLeg.xRot = -step;
            head.xRot += Mth.sin(beat * 2.0F) * 0.12F;
        }

        // A charging unicorn lowers its head, horn pointing forward, for as long as the charge does damage
        if (state.charging)
            head.xRot += CHARGE_HEAD_DOWN;
    }

    /// The vanilla saddle (with bridle and reins), posed like the horse
    public static class Saddle extends EquineSaddleModel {
        public Saddle(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(EquineRenderState state) {
            super.setupAnim(state);

            if (state instanceof MoHorseRenderState horse)
                apply(horse, this.headParts, this.leftFrontLeg, this.rightFrontLeg, this.leftHindLeg, this.rightHindLeg);
        }
    }

    /// The vanilla horse armor, posed like the horse
    public static class Armor extends HorseModel {
        public Armor(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(EquineRenderState state) {
            super.setupAnim(state);

            if (state instanceof MoHorseRenderState horse)
                apply(horse, this.headParts, this.leftFrontLeg, this.rightFrontLeg, this.leftHindLeg, this.rightHindLeg);
        }
    }

    private HorsePoses() {}
}
