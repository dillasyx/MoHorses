package one.syx.mohorses.client.renderer;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import one.syx.mohorses.ModConstants;
import one.syx.mohorses.entity.MaggotEntity;

public class MaggotRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<MaggotEntity, R> {
    public MaggotRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ModConstants.id("maggot")));

        this.shadowRadius = 0.3f;
    }
}
