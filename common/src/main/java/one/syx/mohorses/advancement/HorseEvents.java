package one.syx.mohorses.advancement;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.registry.AdvancementRegistry;
import org.jetbrains.annotations.Nullable;

/// The events of [HorseEventTrigger], and the places that grant them
public final class HorseEvents {
    /// A tamed horse of this mod belongs to the player (checked now and then while it is around, see MoHorse)
    public static String owned(HorseKind kind) {
        return "owned_" + kind.id();
    }

    /// Essence of Undead turned a vanilla horse, donkey or mule (or a Zebra, Zorse, Zonkey, Nightmare) into a zombie horse
    public static final String ZOMBIE_HORSE = "zombie_horse";
    /// A zombie horse rotted away into a skeleton horse
    public static final String SKELETON_HORSE = "skeleton_horse";
    /// Essence of Light gave a skeleton horse, unicorn or pegasus its flesh back
    public static final String REVIVED = "revived";
    /// A Fairy Horse was dyed
    public static final String FAIRY_DYED = "fairy_dyed";
    /// A whip sent the ridden unicorn charging
    public static final String UNICORN_CHARGE = "unicorn_charge";
    /// An animal went into an amulet
    public static final String AMULET = "amulet";
    /// A zebra started shuffling to the music nearby
    public static final String SHUFFLE = "shuffle";

    public static void award(@Nullable Entity player, String event) {
        if (player instanceof ServerPlayer serverPlayer)
            AdvancementRegistry.HORSE_EVENT.get().trigger(serverPlayer, event);
    }

    /// For the owner of an animal, if they are online
    public static void awardOwner(Entity animal, String event) {
        if (animal instanceof OwnableEntity ownable && animal.level() instanceof ServerLevel)
            award(ownable.getOwner(), event);
    }

    /// For everyone close by (within the given distance)
    public static void awardNearby(Entity source, double distance, String event) {
        if (source.level() instanceof ServerLevel level) {
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(source) <= distance * distance)
                    award(player, event);
            }
        }
    }

    private HorseEvents() {}
}
