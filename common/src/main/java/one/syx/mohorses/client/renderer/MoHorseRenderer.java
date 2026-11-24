package one.syx.mohorses.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.equine.BabyDonkeyModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import one.syx.mohorses.ModConstants;
import one.syx.mohorses.client.model.HorsePoses;
import one.syx.mohorses.client.model.MoHorseModel;
import one.syx.mohorses.entity.EggNuzzle;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.entity.MoHorse;
import one.syx.mohorses.entity.TransformFlicker;
import one.syx.mohorses.entity.ZombieHorseDecay;

import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/// Renders a horse kind with the vanilla horse model plus its extra parts (see [MoHorseModel]).
/// Foals use the vanilla foal models and their own `_baby` textures.
/// Saddle and horse armor are drawn by the vanilla equipment layers, with the vanilla saddle and armor textures.
///
/// While a horse transforms after being given an essence, it flickers between its own look and the one of the kind it
/// turns into, slowly at first and faster and faster. That is why every renderer holds the adult model of every shape.
public class MoHorseRenderer extends MobRenderer<MoHorse, MoHorseRenderState, EntityModel<? super MoHorseRenderState>> {
    private static final Identifier ZOMBIE_HORSE_TEXTURE = Identifier.withDefaultNamespace("textures/entity/horse/horse_zombie.png");

    private final Map<HorseKind.Shape, MoHorseModel> adultModels = new EnumMap<>(HorseKind.Shape.class);
    private final EntityModel<? super MoHorseRenderState> foalModel;

    public MoHorseRenderer(EntityRendererProvider.Context context, HorseKind kind) {
        super(context, new MoHorseModel(context.bakeLayer(MoHorseModel.layer(kind.shape(), false)), false), 0.75F);

        for (HorseKind.Shape shape : HorseKind.Shape.values()) {
            this.adultModels.put(shape, new MoHorseModel(context.bakeLayer(MoHorseModel.layer(shape, false)), false));
        }

        this.foalModel = kind.shape() == HorseKind.Shape.DONKEY
                ? new BabyDonkeyModel(context.bakeLayer(ModelLayers.DONKEY_BABY))
                : new MoHorseModel(context.bakeLayer(MoHorseModel.layer(kind.shape(), true)), true);

        addLayer(new HorseGlowLayer(this));
        addLayer(new HorseEyesLayer(this));

        boolean donkey = kind.shape() == HorseKind.Shape.DONKEY;

        if (kind.armorRule() != HorseKind.ArmorRule.NONE) {
            addLayer(new HorseEquipmentLayer(this, context.getEquipmentRenderer(), context.getEquipmentAssets(), EquipmentClientInfo.LayerType.HORSE_BODY,
                    state -> state.bodyArmorItem, new HorsePoses.Armor(context.bakeLayer(ModelLayers.HORSE_ARMOR)), 2));
        }

        addLayer(new HorseEquipmentLayer(this, context.getEquipmentRenderer(), context.getEquipmentAssets(),
                donkey ? EquipmentClientInfo.LayerType.DONKEY_SADDLE : EquipmentClientInfo.LayerType.HORSE_SADDLE,
                state -> state.saddle,
                new HorsePoses.Saddle(context.bakeLayer(donkey ? ModelLayers.DONKEY_SADDLE : ModelLayers.HORSE_SADDLE)), 2));
    }

    @Override
    public void submit(MoHorseRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        // The flicker towards a vanilla zombie horse shows the plain horse body
        this.model = state.isBaby ? this.foalModel : this.adultModels.get(state.showsZombieHorse ? HorseKind.Shape.HORSE : state.displayedKind.shape());

        // Ghosts hover a little above the ground, bobbing gently
        poseStack.pushPose();

        if (state.floating)
            poseStack.translate(0.0F, (MoHorse.GHOST_HOVER + Mth.sin(state.ageInTicks * 0.08F) * 0.04F) * state.scale, 0.0F);

        super.submit(state, poseStack, submitNodeCollector, camera);
        HorseAura.submit(state, poseStack, submitNodeCollector, camera);
        poseStack.popPose();
    }

    /// Fairy Horses have see-through wings and ghosts are see-through altogether: they are drawn translucent, and with
    /// back-face culling, so the top and bottom face of the flat wings (painted with the same picture) don't flicker
    /// against each other
    @Override
    protected @Nullable RenderType getRenderType(MoHorseRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        if (isBodyVisible && !forceTransparent && (state.displayedKind.shape() == HorseKind.Shape.FAIRY || state.displayedKind.isGhost()))
            return RenderTypes.entityTranslucentCull(getTextureLocation(state));

        return super.getRenderType(state, isBodyVisible, forceTransparent, appearGlowing);
    }

    /// Ghosts fade in and flicker (see MoHorse#getGhostOpacity)
    @Override
    protected int getModelTint(MoHorseRenderState state) {
        return state.displayedKind.isGhost() ? ARGB.white(state.ghostOpacity) : super.getModelTint(state);
    }

    @Override
    public Identifier getTextureLocation(MoHorseRenderState state) {
        if (state.showsZombieHorse)
            return ZOMBIE_HORSE_TEXTURE;

        if (state.displayedKind == state.kind && state.decayStage > 0)
            return ModConstants.id("textures/entity/" + state.kind.id() + "/decay_" + Math.min(state.decayStage, ZombieHorseDecay.STAGES) + (state.isBaby ? "_baby" : "") + ".png");

        if (state.displayedKind == HorseKind.FAIRY_HORSE && state.fairyColor != null)
            return ModConstants.id("textures/entity/fairy_horse/" + state.fairyColor.getSerializedName() + (state.isBaby ? "_baby" : "") + ".png");

        return state.displayedKind.texture(state.ageInTicks, state.isBaby);
    }

    /// A shuffling zebra sways from side to side and bobs to the beat
    @Override
    protected void setupRotations(MoHorseRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);

        if (state.shuffling) {
            float beat = state.ageInTicks * HorsePoses.SHUFFLE_TEMPO;

            poseStack.translate(0.0F, Math.abs(Mth.sin(beat)) * 0.08F * entityScale, 0.0F);
            poseStack.rotateDegrees(Axis.ZP, Mth.sin(beat) * 5.0F);
        }
    }

    @Override
    public MoHorseRenderState createRenderState() {
        return new MoHorseRenderState();
    }

    @Override
    public void extractRenderState(MoHorse horse, MoHorseRenderState state, float partialTicks) {
        super.extractRenderState(horse, state, partialTicks);

        // What vanilla's AbstractHorseRenderer captures
        state.saddle = horse.getItemBySlot(EquipmentSlot.SADDLE).copy();
        state.bodyArmorItem = horse.getBodyArmorItem().copy();
        state.isRidden = horse.isVehicle();
        state.eatAnimation = horse.getEatAnim(partialTicks);
        state.standAnimation = horse.getStandAnim(partialTicks);
        state.feedingAnimation = horse.getMouthAnim(partialTicks);
        state.animateTail = horse.tailCounter > 0;

        state.kind = horse.kind();
        state.displayedKind = displayedKind(horse, partialTicks);
        state.showsZombieHorse = horse.isTurningIntoZombieHorse() && showsTarget(horse, partialTicks);
        state.fairyColor = horse.getFairyColor();
        state.decayStage = horse.getDecayStage();
        state.ghostOpacity = horse.kind().isGhost() ? horse.getGhostOpacity(partialTicks) : 1.0F;
        state.floating = horse.kind().isGhost();
        state.darkness = horse.getDarkness(partialTicks);
        state.groundY = horse.getGroundY(partialTicks);
        state.legSweep = state.floating ? 1.0F : horse.getLegSweep(partialTicks);
        state.charging = horse.isCharging();
        state.ablaze = horse.isAblaze();
        state.sparkleOffset = horse.getId() * 37 % 1000;
        state.shuffling = horse.isShuffling();
        state.hasChest = horse.hasChest();
        state.flying = horse.isFlying();
        state.flapping = state.flying && horse.getDeltaMovement().y > 0.0;
        state.wingSpread = horse.getWingSpread(partialTicks);
        state.wingBeat = horse.getWingBeat(partialTicks);

        // No rearing in mid-air (the rider's jump key flaps the wings up there), nor during a unicorn's charge
        // (rearing holds the hind legs still, the charge gallops on all four)
        if (state.flying || state.charging)
            state.standAnimation = 0.0F;

        // Last, as it puts the other head animations at rest while it plays
        NuzzlePose.set(state, EggNuzzle.progress(horse, partialTicks));
    }

    /// During a transformation: the old or the new kind, switching back and forth faster and faster
    private static HorseKind displayedKind(MoHorse horse, float partialTicks) {
        HorseKind target = horse.getTransformTarget();

        return target != null && showsTarget(horse, partialTicks) ? target : horse.kind();
    }

    /// Whether a transforming horse shows its new look right now
    private static boolean showsTarget(MoHorse horse, float partialTicks) {
        return horse.isTransforming() && TransformFlicker.showsNewLook(horse.getTransformProgress(partialTicks));
    }
}
