package one.syx.mohorses.client.renderer;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import one.syx.mohorses.ModConstants;
import one.syx.mohorses.entity.ZombieHorseDecay;

/// The vanilla zombie horse renderer, showing how far the horse has rotted (see [ZombieHorseDecay])
public class DecayingZombieHorseRenderer extends UndeadHorseRenderer {
    public DecayingZombieHorseRenderer(EntityRendererProvider.Context context) {
        super(context, EquipmentClientInfo.LayerType.ZOMBIE_HORSE_SADDLE, ModelLayers.ZOMBIE_HORSE_SADDLE, Type.ZOMBIE, Type.ZOMBIE_BABY);
    }

    @Override
    public EquineRenderState createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AbstractHorse horse, EquineRenderState state, float partialTicks) {
        super.extractRenderState(horse, state, partialTicks);

        if (state instanceof State decayState && horse instanceof ZombieHorse zombieHorse)
            decayState.decayStage = ZombieHorseDecay.stageOf(zombieHorse);
    }

    @Override
    public Identifier getTextureLocation(EquineRenderState state) {
        int stage = state instanceof State decayState ? decayState.decayStage : 0;

        if (stage <= 0)
            return super.getTextureLocation(state);

        stage = Math.min(stage, ZombieHorseDecay.STAGES);

        return ModConstants.id("textures/entity/zombie_horse/decay_" + stage + (state.isBaby ? "_baby" : "") + ".png");
    }

    public static class State extends EquineRenderState {
        public int decayStage;
    }
}
