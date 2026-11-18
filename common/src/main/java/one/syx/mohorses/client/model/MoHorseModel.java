package one.syx.mohorses.client.model;

import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.animal.equine.BabyHorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import one.syx.mohorses.ModConstants;
import one.syx.mohorses.client.renderer.MoHorseRenderState;
import one.syx.mohorses.entity.HorseKind;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/// The vanilla horse model with the extra parts of the special horses.
///
/// Head, neck, legs and tail come straight from vanilla ([AbstractEquineModel]), so these horses look and move exactly
/// like vanilla ones. On top of that every shape gets saddle bags (chests), and depending on [HorseKind.Shape]
/// donkey ears, feathered wings, a horn or butterfly wings.
///
/// Foals use the vanilla foal model (`BabyHorseModel`) with the same extra parts, and the foal animation tweaks.
///
/// The wing poses and flapping are taken from the original Mo' Creatures horse model.
public class MoHorseModel extends AbstractEquineModel<MoHorseRenderState> {
    /// Vanilla draws living horses 10% larger than the base mesh, and donkeys at 87%
    private static final float HORSE_SCALE = 1.1F;
    private static final float DONKEY_SCALE = 0.87F;
    private static final float DEG_TO_RAD = Mth.DEG_TO_RAD;

    /// Wings on foals are this big compared to an adult's
    private static final float FOAL_WING_SCALE = 0.5F;

    private final boolean foal;
    private final @Nullable ModelPart leftChest;
    private final @Nullable ModelPart rightChest;
    private final @Nullable ModelPart leftWing;
    private final @Nullable ModelPart rightWing;
    private final @Nullable ModelPart leftWingMiddle;
    private final @Nullable ModelPart rightWingMiddle;
    private final @Nullable ModelPart leftWingOuter;
    private final @Nullable ModelPart rightWingOuter;
    private final @Nullable ModelPart leftButterflyWing;
    private final @Nullable ModelPart rightButterflyWing;

    public MoHorseModel(ModelPart root, boolean foal) {
        super(root);

        this.foal = foal;
        this.leftChest = optionalChild(this.body, "left_chest");
        this.rightChest = optionalChild(this.body, "right_chest");
        this.leftWing = optionalChild(this.body, "left_wing");
        this.rightWing = optionalChild(this.body, "right_wing");
        this.leftWingMiddle = this.leftWing == null ? null : this.leftWing.getChild("middle");
        this.rightWingMiddle = this.rightWing == null ? null : this.rightWing.getChild("middle");
        this.leftWingOuter = this.leftWingMiddle == null ? null : this.leftWingMiddle.getChild("outer");
        this.rightWingOuter = this.rightWingMiddle == null ? null : this.rightWingMiddle.getChild("outer");
        this.leftButterflyWing = optionalChild(this.body, "left_butterfly_wing");
        this.rightButterflyWing = optionalChild(this.body, "right_butterfly_wing");
    }

    private static @Nullable ModelPart optionalChild(ModelPart parent, String name) {
        return parent.hasChild(name) ? parent.getChild(name) : null;
    }

    @Override
    public void setupAnim(MoHorseRenderState state) {
        super.setupAnim(state);

        if (this.leftChest != null && this.rightChest != null) {
            this.leftChest.visible = state.hasChest;
            this.rightChest.visible = state.hasChest;
        }

        if (this.leftWing != null && this.rightWing != null && this.leftWingMiddle != null && this.rightWingMiddle != null
                && this.leftWingOuter != null && this.rightWingOuter != null)
            animateFeatheredWings(state);

        if (this.leftButterflyWing != null && this.rightButterflyWing != null)
            animateButterflyWings(state);

        HorsePoses.apply(state, this.headParts, this.leftFrontLeg, this.rightFrontLeg, this.leftHindLeg, this.rightHindLeg);
    }

    @Override
    protected float getLegStandingYOffset() {
        return this.foal ? 4.0F : super.getLegStandingYOffset();
    }

    @Override
    protected float getLegStandingZOffset() {
        return this.foal ? 0.0F : super.getLegStandingZOffset();
    }

    @Override
    protected float getTailXRotOffset() {
        return this.foal ? (float)(-Math.PI / 2) : super.getTailXRotOffset();
    }

    @Override
    protected void animateHeadPartsPlacement(float eating, float standing) {
        if (!this.foal) {
            super.animateHeadPartsPlacement(eating, standing);

            return;
        }

        this.headParts.y = this.headParts.y + Mth.lerp(eating, Mth.lerp(standing, 0.0F, -2.0F), 2.0F);
        this.headParts.z = Mth.lerp(standing, this.headParts.z, -4.0F);
    }

    /// On the ground the wings hang folded along the flanks. In the air they spread out and keep beating: slow and
    /// wide while gliding, much wider while climbing. Middle and outer feathers trail behind the inner part,
    /// so each beat whips through the wing instead of moving it as one stiff board.
    private void animateFeatheredWings(MoHorseRenderState state) {
        float spread = state.wingSpread;
        float beat = state.wingBeat;
        // A fixed beat rate: scaling ageInTicks by a changing factor would make the wings jump
        float phase = state.ageInTicks * 0.3F;
        float amplitude = Mth.lerp(beat, 0.55F, 1.1F) * spread;

        // Positive Z rotation lowers a left wing; the slight negative bias keeps the beat centred a bit above level
        float innerFlap = Mth.cos(phase) * amplitude - 0.15F * spread;
        float middleFlap = Mth.cos(phase - 0.7F) * amplitude * 0.45F;
        float outerFlap = Mth.cos(phase - 1.4F) * amplitude * 0.35F;

        float innerZ = Mth.lerp(spread, 60.0F * DEG_TO_RAD, innerFlap);
        float outerY = Mth.lerp(spread, -90.0F * DEG_TO_RAD, -0.3228859F);

        this.leftWing.zRot = innerZ;
        this.rightWing.zRot = -innerZ;
        this.leftWingMiddle.zRot = middleFlap;
        this.rightWingMiddle.zRot = -middleFlap;
        this.leftWingOuter.zRot = outerFlap;
        this.rightWingOuter.zRot = -outerFlap;
        this.leftWingOuter.yRot = outerY;
        this.rightWingOuter.yRot = -outerY;
    }

    /// Butterfly wings stand up in a V and flutter now and then, faster when flying
    private void animateButterflyWings(MoHorseRenderState state) {
        float age = state.ageInTicks;

        // The three flutters are blended by how far the horse is into flying/climbing, so switching between them is smooth
        float cycle = age % 100.0F;
        float restingEnvelope = cycle > 40.0F && cycle < 60.0F ? Mth.sin((cycle - 40.0F) / 20.0F * (float)Math.PI) : 0.0F;
        // On the ground the wings never stand quite still: a slow, soft breathing, a little bob with every step,
        // and every few seconds a short flutter
        float breathing = Mth.sin(age * 0.07F) * 0.12F;
        float walking = Mth.cos(state.walkAnimationPos * 0.6662F) * 0.25F * Math.min(1.0F, state.walkAnimationSpeed);
        float resting = breathing + walking + Mth.cos(age * 0.15F) * 1.2F * restingEnvelope;
        float gliding = Mth.cos(age * 0.6662F) * 0.5F;
        float climbing = Mth.cos(age * 0.9F) * 0.9F;
        float flutter = Mth.lerp(state.wingSpread, resting, Mth.lerp(state.wingBeat, gliding, climbing));

        float baseAngle = 30.0F * DEG_TO_RAD;

        this.leftButterflyWing.zRot = -baseAngle + flutter;
        this.rightButterflyWing.zRot = baseAngle - flutter;
    }

    // ------------------------------------------------------------------------------------------------
    // Model layers
    // ------------------------------------------------------------------------------------------------

    /// The model layer of a shape, for adults or foals
    public static ModelLayerLocation layer(HorseKind.Shape shape, boolean foal) {
        return new ModelLayerLocation(ModConstants.id("horse_" + shape.name().toLowerCase(Locale.ROOT) + (foal ? "_foal" : "")), "main");
    }

    /// Hands every shape's model layers to the loader's registration.
    /// Donkey-shaped foals use the vanilla donkey foal model, so there is no own layer for those.
    public static void registerLayers(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> registrar) {
        for (HorseKind.Shape shape : HorseKind.Shape.values()) {
            registrar.accept(layer(shape, false), () -> createLayer(shape));

            if (shape != HorseKind.Shape.DONKEY)
                registrar.accept(layer(shape, true), () -> createFoalLayer(shape));
        }
    }

    /// The vanilla foal with the extra parts of the shape, shrunk to foal size
    public static LayerDefinition createFoalLayer(HorseKind.Shape shape) {
        MeshDefinition mesh = BabyHorseModel.createBabyMesh(CubeDeformation.NONE);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        PartDefinition head = root.getChild("head_parts").getChild("head");

        switch (shape) {
            case WINGED -> addFeatheredWings(body, PartPose.offset(4.0F, -3.8F, -5.0F).withScale(FOAL_WING_SCALE), PartPose.offset(-4.0F, -3.8F, -5.0F).withScale(FOAL_WING_SCALE));
            case UNICORN -> addFoalHorn(head);
            case FAIRY -> {
                addFoalHorn(head);
                addButterflyWings(body, PartPose.offset(4.0F, -3.5F, -1.0F).withScale(FOAL_WING_SCALE), PartPose.offset(-4.0F, -3.5F, -1.0F).withScale(FOAL_WING_SCALE));
            }
            default -> {}
        }

        return LayerDefinition.create(mesh, shape.textureSize(), shape.textureSize());
    }

    /// A shorter horn, about in the same place on the longer foal head
    private static void addFoalHorn(PartDefinition head) {
        head.addOrReplaceChild("horn", CubeListBuilder.create().texOffs(0, 64).addBox(-0.5F, -8.95F, -2.1F, 1.0F, 5.0F, 1.0F), PartPose.ZERO);
    }

    public static LayerDefinition createLayer(HorseKind.Shape shape) {
        MeshDefinition mesh = createBodyMesh(CubeDeformation.NONE);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        PartDefinition head = root.getChild("head_parts").getChild("head");

        addChests(body);

        switch (shape) {
            case DONKEY -> addDonkeyEars(head);
            case WINGED -> addFeatheredWings(body, PartPose.offset(5.0F, -7.5F, -13.0F), PartPose.offset(-5.0F, -7.5F, -13.0F));
            case UNICORN -> addHorn(head);
            case FAIRY -> {
                addHorn(head);
                addButterflyWings(body, PartPose.offset(5.0F, -8.0F, -6.0F), PartPose.offset(-5.0F, -8.0F, -6.0F));
            }
            default -> {}
        }

        LayerDefinition layer = LayerDefinition.create(mesh, shape.textureSize(), shape.textureSize());

        // Same sizes as vanilla, so the vanilla saddle and armor layers fit
        return layer.apply(MeshTransformer.scaling(shape == HorseKind.Shape.DONKEY ? DONKEY_SCALE : HORSE_SCALE));
    }

    /// The vanilla donkey's saddle bags
    private static void addChests(PartDefinition body) {
        CubeListBuilder chest = CubeListBuilder.create().texOffs(26, 21).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 8.0F, 3.0F);

        body.addOrReplaceChild("left_chest", chest, PartPose.offsetAndRotation(6.0F, -8.0F, 0.0F, 0.0F, (float)(-Math.PI / 2), 0.0F));
        body.addOrReplaceChild("right_chest", chest, PartPose.offsetAndRotation(-6.0F, -8.0F, 0.0F, 0.0F, (float)(Math.PI / 2), 0.0F));
    }

    /// The vanilla donkey's long ears, replacing the horse ears
    private static void addDonkeyEars(PartDefinition head) {
        CubeListBuilder ear = CubeListBuilder.create().texOffs(0, 12).addBox(-1.0F, -7.0F, 0.0F, 2.0F, 7.0F, 1.0F);

        head.addOrReplaceChild("left_ear", ear, PartPose.offsetAndRotation(1.25F, -10.0F, 4.0F, (float)(Math.PI / 12), 0.0F, (float)(Math.PI / 12)));
        head.addOrReplaceChild("right_ear", ear, PartPose.offsetAndRotation(-1.25F, -10.0F, 4.0F, (float)(Math.PI / 12), 0.0F, (float)(-Math.PI / 12)));
    }

    private static void addHorn(PartDefinition head) {
        head.addOrReplaceChild("horn", CubeListBuilder.create().texOffs(0, 64).addBox(-0.5F, -19.0F, 1.5F, 1.0F, 8.0F, 1.0F), PartPose.ZERO);
    }

    /// Three-part wings on the shoulders: inner, middle and outer feathers, each hinged on the previous one
    private static void addFeatheredWings(PartDefinition body, PartPose leftShoulder, PartPose rightShoulder) {
        PartDefinition left = body.addOrReplaceChild("left_wing",
                CubeListBuilder.create().texOffs(36, 103).addBox(0.0F, -0.7F, 0.0F, 7.0F, 2.0F, 11.0F),
                leftShoulder);
        PartDefinition leftMiddle = left.addOrReplaceChild("middle",
                CubeListBuilder.create().texOffs(46, 90).addBox(0.0F, -0.7F, 0.0F, 12.0F, 2.0F, 11.0F),
                PartPose.offset(5.0F, 0.0F, 0.0F));
        leftMiddle.addOrReplaceChild("outer",
                CubeListBuilder.create().texOffs(0, 77).addBox(0.0F, -0.7F, 0.0F, 22.0F, 2.0F, 11.0F),
                PartPose.offset(12.0F, 0.0F, 0.0F));

        PartDefinition right = body.addOrReplaceChild("right_wing",
                CubeListBuilder.create().texOffs(0, 103).addBox(-7.0F, -0.7F, 0.0F, 7.0F, 2.0F, 11.0F),
                rightShoulder);
        PartDefinition rightMiddle = right.addOrReplaceChild("middle",
                CubeListBuilder.create().texOffs(0, 90).addBox(-12.0F, -0.7F, 0.0F, 12.0F, 2.0F, 11.0F),
                PartPose.offset(-5.0F, 0.0F, 0.0F));
        rightMiddle.addOrReplaceChild("outer",
                CubeListBuilder.create().texOffs(0, 64).addBox(-22.0F, -0.7F, 0.0F, 22.0F, 2.0F, 11.0F),
                PartPose.offset(-12.0F, 0.0F, 0.0F));
    }

    /// Two flat butterfly wings on the back
    private static void addButterflyWings(PartDefinition body, PartPose left, PartPose right) {
        body.addOrReplaceChild("left_butterfly_wing",
                CubeListBuilder.create().texOffs(27, 99).addBox(0.0F, 0.0F, -16.0F, 26.0F, 0.0F, 29.0F),
                left);
        body.addOrReplaceChild("right_butterfly_wing",
                CubeListBuilder.create().texOffs(27, 69).addBox(-26.0F, 0.0F, -16.0F, 26.0F, 0.0F, 29.0F),
                right);
    }
}
