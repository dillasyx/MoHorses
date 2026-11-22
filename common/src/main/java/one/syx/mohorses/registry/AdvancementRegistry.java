package one.syx.mohorses.registry;

import one.syx.mohorses.ModConstants;
import one.syx.mohorses.advancement.HorseEventTrigger;

import java.util.function.Supplier;

/// The advancement trigger of this mod (the advancements themselves are generated data, see the Fabric datagen)
public final class AdvancementRegistry {
    public static void init() {}

    public static final Supplier<HorseEventTrigger> HORSE_EVENT = ModConstants.PLATFORM.registerTrigger("horse_event", HorseEventTrigger::new);
}
