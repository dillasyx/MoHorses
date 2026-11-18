package one.syx.mohorses.mixin;

import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import one.syx.mohorses.entity.ZombieHorseDecay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// A player who starts to see a rotting zombie horse is told how far it has rotted (later changes reach all watchers
/// as entity events, see [ZombieHorseDecay#EVENT_STAGE])
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "startSeenByPlayer", at = @At("TAIL"))
    private void mohorses$sendDecayStage(ServerPlayer player, CallbackInfo ci) {
        if (this instanceof ZombieHorseDecay.Decaying decaying && decaying.mohorses$getDecayStage() > 0)
            player.connection.send(new ClientboundEntityEventPacket((Entity)(Object)this, ZombieHorseDecay.stageEvent(decaying.mohorses$getDecayStage())));
    }
}
