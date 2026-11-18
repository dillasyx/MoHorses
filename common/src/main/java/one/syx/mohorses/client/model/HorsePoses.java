package one.syx.mohorses.client.model;

import net.minecraft.client.model.animal.equine.EquineSaddleModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.util.Mth;
import one.syx.mohorses.client.renderer.MoHorseRenderState;

/// The poses of this mod on top of the vanilla horse animation: ghosts hovering with folded legs, zebras shuffling,
/// unicorns charging head down. The horse model, its saddle and its armor all apply them, so the gear moves with the horse.
public final class HorsePoses {
    /// How fast zebras shuffle (radians of the dance per tick)
    public static final float SHUFFLE_TEMPO = 0.45F;
    /// How far a charging unicorn lowers its head
    private static final float CHARGE_HEAD_DOWN = 0.75F;

    public static void apply(MoHorseRenderState state, ModelPart head, ModelPart leftFrontLeg, ModelPart rightFrontLeg, ModelPart leftHindLeg, ModelPart rightHindLeg) {
        // Ghosts don't walk: like in Mo' Creatures they hover a little above the ground with their legs swept back,
        // drifting gently. Walking only makes the legs sway a bit further back.
        if (state.floating) {
            float drift = Mth.sin(state.ageInTicks * 0.08F) * 0.06F;
            float walk = Math.min(state.walkAnimationSpeed, 1.0F) * 0.25F;

            leftFrontLeg.xRot = 0.85F + walk + drift;
            rightFrontLeg.xRot = 0.95F + walk - drift;
            leftHindLeg.xRot = 0.55F + walk - drift;
            rightHindLeg.xRot = 0.45F + walk + drift;
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
