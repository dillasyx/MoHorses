package one.syx.mohorses.client.renderer;

import net.minecraft.client.renderer.entity.state.EquineRenderState;

/// Render state access (EquineRenderStateMixin): how far along a horse's egg nuzzling is, or -1 (see EggNuzzle)
public interface NuzzlePose {
    float mohorses$getNuzzle();

    void mohorses$setNuzzle(float progress);

    /// Sets the progress, and while the horse nuzzles puts its other head animations (grazing, rearing, feeding,
    /// looking around) at rest, so the turn always starts from the normal pose
    static void set(EquineRenderState state, float progress) {
        ((NuzzlePose)state).mohorses$setNuzzle(progress);

        if (progress < 0.0F)
            return;

        state.eatAnimation = 0.0F;
        state.standAnimation = 0.0F;
        state.feedingAnimation = 0.0F;
        state.xRot = 0.0F;
        state.yRot = 0.0F;
    }
}
