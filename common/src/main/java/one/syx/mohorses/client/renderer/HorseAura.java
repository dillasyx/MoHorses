package one.syx.mohorses.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import one.syx.mohorses.entity.MoHorse;
import one.syx.mohorses.ModConstants;
import one.syx.mohorses.mixin.client.RenderTypesAccessor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/// The soft shine around the magical horses in the dark. It looks like light, but nothing around gets lighter: it is made
/// of colour gradients, drawn like a spider's eyes (so they hide nothing behind them).
///
/// It is a ball of light around the horse, and on the ground some light on the floor as well.
public final class HorseAura {
    private static final Identifier WHITE = ModConstants.id("textures/misc/white.png");
    private static final int SEGMENTS = 40;
    private static final int RINGS = 12;

    /// Colour (rgb), strength at its centre from 0 to 1, and how far it reaches, in blocks
    private record Look(int color, float strength, float radius) {}

    public static void submit(MoHorseRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.isInvisible || state.showsZombieHorse || state.darkness <= 0.0F)
            return;

        Look look = look(state);

        if (look == null)
            return;

        float size = state.scale * (state.isBaby ? 0.55F : 1.0F);
        float height = 1.15F * size;
        // Ghosts are drawn above the ground
        float hover = state.floating ? MoHorse.GHOST_HOVER * state.scale : 0.0F;

        // Close to the camera (or around its rider) it would only be a haze
        Vec3 away = new Vec3(state.x, state.y + height, state.z).subtract(camera.pos);
        float distance = (float)away.length();
        float nearness = Mth.clamp((distance - 1.8F) / 2.2F, 0.0F, 1.0F);
        float strength = look.strength() * state.darkness * nearness;

        if (strength < 0.01F)
            return;

        float radius = look.radius() * size;
        int color = look.color();

        // Where the camera and the ground are, seen from the horse
        Vector3f eye = new Vector3f((float)(camera.pos.x - state.x), (float)(camera.pos.y - state.y) - hover, (float)(camera.pos.z - state.z));
        float altitude = (float)Math.max(0.0, state.y - state.groundY);
        float ground = 0.04F - hover - altitude;
        Ball ball = new Ball(color, strength, radius * 0.85F, new Vector3f(0.0F, height, 0.0F), eye, ground, 1.8F * size,
                camera.orientation.transform(new Vector3f(1.0F, 0.0F, 0.0F)), camera.orientation.transform(new Vector3f(0.0F, 1.0F, 0.0F)));

        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypesAccessor.mohorses$eyes().apply(WHITE), ball::draw);

        // And some light on the floor, the less the higher it flies
        float onFloor = strength * 0.4F * Mth.clamp(1.0F - altitude / 4.0F, 0.0F, 1.0F);

        if (onFloor > 0.01F) {
            poseStack.pushPose();
            poseStack.translate(0.0F, ground + 0.01F, 0.0F);
            poseStack.rotateDegrees(Axis.XP, -90.0F);
            submitNodeCollector.submitCustomGeometry(poseStack, RenderTypesAccessor.mohorses$eyes().apply(WHITE), (pose, buffer) -> floor(pose, buffer, color, onFloor, radius));
            poseStack.popPose();
        }
    }

    /// How the kind shines right now, or null if it doesn't
    private static @Nullable Look look(MoHorseRenderState state) {
        float age = state.ageInTicks;

        return switch (state.displayedKind) {
            case UNICORN -> new Look(0xFFE9B8, state.charging ? 0.6F : 0.33F + Mth.sin(age * 0.08F) * 0.05F, 2.9F);
            case DARK_PEGASUS -> new Look(0xA81414, 0.55F + Mth.sin(age * 0.05F) * 0.06F, 2.9F);
            case GHOST_HORSE -> new Look(0x4FD6C0, state.ghostOpacity * 0.65F, 2.6F);
            case GHOST_PEGASUS -> new Look(0xBFD8FF, state.ghostOpacity * 0.65F, 2.8F);
            default -> null;
        };
    }

    /// A ball of light round the middle of the horse: rings that face the camera and fade out to nothing.
    ///
    /// Every point is moved back along the line of sight until it lies behind the horse, but at most onto the ground. In the
    /// picture that changes nothing, but so the ball neither cuts through the body nor ends in an edge at the ground.
    private record Ball(int color, float strength, float radius, Vector3f center, Vector3f eye, float ground, float behind, Vector3f right, Vector3f up) {
        private void draw(PoseStack.Pose pose, VertexConsumer buffer) {
            rings((part, angle) -> {
                Vector3f point = new Vector3f(this.center)
                        .fma(Mth.cos(angle) * part * this.radius, this.right)
                        .fma(Mth.sin(angle) * part * this.radius, this.up);
                Vector3f sight = new Vector3f(point).sub(this.eye).normalize();

                point.fma(this.behind, sight);

                if (point.y < this.ground && sight.y < 0.0F && this.eye.y > this.ground)
                    point.set(this.eye).fma((this.ground - this.eye.y) / sight.y, sight);

                vertex(pose, buffer, point.x, point.y, point.z, this.color, this.strength, part);
            });
        }
    }

    private static void floor(PoseStack.Pose pose, VertexConsumer buffer, int color, float strength, float radius) {
        rings((part, angle) -> vertex(pose, buffer, Mth.cos(angle) * part * radius, Mth.sin(angle) * part * radius, 0.0F, color, strength, part));
    }

    private interface Point {
        /// @param part how far out it is, from 0 (the centre) to 1
        void at(float part, float angle);
    }

    private static void rings(Point point) {
        for (int ring = 0; ring < RINGS; ring++) {
            float inner = (float)ring / RINGS;
            float outer = (float)(ring + 1) / RINGS;

            for (int segment = 0; segment < SEGMENTS; segment++) {
                float from = segment * Mth.TWO_PI / SEGMENTS;
                float to = (segment + 1) * Mth.TWO_PI / SEGMENTS;

                point.at(inner, from);
                point.at(outer, from);
                point.at(outer, to);
                point.at(inner, to);
            }
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, int color, float strength, float part) {
        float fade = 1.0F - part;

        buffer.addVertex(pose, x, y, z)
                .setColor(ARGB.color(strength * fade * Mth.sqrt(fade), color))
                .setUv(0.5F, 0.5F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    private HorseAura() {}
}
