package one.syx.mohorses.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import one.syx.mohorses.registry.AdvancementRegistry;

import java.util.Optional;

/// The one advancement trigger of this mod: something happened to a player with their horses, named by a short event
/// string (see [HorseEvents])
public class HorseEventTrigger extends SimpleCriterionTrigger<HorseEventTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, String event) {
        trigger(player, instance -> instance.event().equals(event));
    }

    public record TriggerInstance(Optional<Holder<LootItemCondition>> player, String event) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.fieldOf("event").forGetter(TriggerInstance::event)
        ).apply(instance, TriggerInstance::new));

        /// The criterion for one event (used by the advancement datagen)
        public static Criterion<TriggerInstance> of(String event) {
            return AdvancementRegistry.HORSE_EVENT.get().createCriterion(new TriggerInstance(Optional.empty(), event));
        }
    }
}
