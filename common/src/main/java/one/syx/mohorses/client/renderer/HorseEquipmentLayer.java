package one.syx.mohorses.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.Equippable;

import java.util.function.Function;

/// Saddle or horse armor. On most horses this is just the vanilla equipment layer; on a ghost the equipment is as
/// see-through as the ghost itself (vanilla only draws equipment opaque).
public class HorseEquipmentLayer extends RenderLayer<MoHorseRenderState, EntityModel<? super MoHorseRenderState>> {
    private final SimpleEquipmentLayer<MoHorseRenderState, EntityModel<? super MoHorseRenderState>, ? extends EntityModel<? super MoHorseRenderState>> vanilla;
    private final EquipmentAssetManager equipmentAssets;
    private final EquipmentClientInfo.LayerType layerType;
    private final Function<MoHorseRenderState, ItemStack> itemGetter;
    private final EntityModel<? super MoHorseRenderState> model;

    public HorseEquipmentLayer(RenderLayerParent<MoHorseRenderState, EntityModel<? super MoHorseRenderState>> renderer,
                               EquipmentLayerRenderer equipmentRenderer, EquipmentAssetManager equipmentAssets,
                               EquipmentClientInfo.LayerType layerType, Function<MoHorseRenderState, ItemStack> itemGetter,
                               EntityModel<? super MoHorseRenderState> model, int order) {
        super(renderer);
        this.vanilla = new SimpleEquipmentLayer<>(renderer, equipmentRenderer, layerType, itemGetter, model, null, order);
        this.equipmentAssets = equipmentAssets;
        this.layerType = layerType;
        this.itemGetter = itemGetter;
        this.model = model;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, MoHorseRenderState state, float yRot, float xRot) {
        if (!state.displayedKind.isGhost()) {
            this.vanilla.submit(poseStack, submitNodeCollector, lightCoords, state, yRot, xRot);

            return;
        }

        ItemStack stack = this.itemGetter.apply(state);
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);

        if (state.isBaby || equippable == null || equippable.assetId().isEmpty())
            return;

        int dyeColor = DyedItemColor.getOrDefault(stack, 0);

        for (EquipmentClientInfo.Layer layer : this.equipmentAssets.get(equippable.assetId().get()).getLayers(this.layerType)) {
            int color = colorFor(layer, dyeColor);

            if (color == 0)
                continue;

            submitNodeCollector.submitModel(this.model, state, poseStack,
                    RenderTypes.entityTranslucent(layer.getTextureLocation(this.layerType)), lightCoords, OverlayTexture.NO_OVERLAY,
                    ARGB.multiplyAlpha(color, state.ghostOpacity), null, state.outlineColor, null);
        }
    }

    /// The same colour vanilla gives an equipment layer: the dye for dyeable layers (or their undyed colour), white otherwise
    private static int colorFor(EquipmentClientInfo.Layer layer, int dyeColor) {
        return layer.dyeable()
                .map(dyeable -> dyeColor != 0 ? dyeColor : dyeable.colorWhenUndyed().map(ARGB::opaque).orElse(0))
                .orElse(-1);
    }
}
