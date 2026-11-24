package one.syx.mohorses.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/// A horse that is fed an egg, or hit by a thrown one, turns its head down and round under its belly and nudges there,
/// three times over. The server only sends the start as an entity event; the client plays it (AbstractEquineModelMixin).
public final class EggNuzzle {
    /// The entity event that starts it. Clear of vanilla's events, the decay stages (90+) and the essence looks (99+).
    public static final byte EVENT = 89;
    /// How long it lasts, in ticks
    public static final int DURATION = 60;

    /// The client-side start tick, kept on every horse (AbstractHorseMixin)
    public interface Nuzzling {
        int mohorses$getNuzzleStart();

        void mohorses$setNuzzleStart(int start);
    }

    /// Grown horses, donkeys, mules and the horses of this mod; not llamas or camels (other models) and not the wild
    /// Nightmares and unicorns
    public static boolean canNuzzle(Entity entity) {
        return entity instanceof AbstractHorse horse && !(horse instanceof Llama) && !(horse instanceof Camel)
                && !(horse instanceof WildNightmare) && !(horse instanceof WildUnicorn) && !horse.isBaby();
    }

    /// Feeding an egg: uses it up and starts the nuzzling. Returns whether the egg was taken.
    public static boolean feed(AbstractHorse horse, Player player, ItemStack stack) {
        if (!stack.is(ItemTags.EGGS) || !canNuzzle(horse) || horse.isVehicle())
            return false;

        if (horse.level() instanceof ServerLevel level) {
            stack.consume(1, player);
            horse.playSound(SoundEvents.HORSE_EAT, 1.0F, 1.0F);
            start(horse, level);
        }

        return true;
    }

    /// A thrown egg hit the horse
    public static void hitBy(Entity target) {
        if (canNuzzle(target) && target.level() instanceof ServerLevel level)
            start((AbstractHorse)target, level);
    }

    /// Stops grazing and rearing (they would turn the head elsewhere) and tells the clients to play it
    private static void start(AbstractHorse horse, ServerLevel level) {
        horse.setEating(false);
        horse.clearStanding();
        level.broadcastEntityEvent(horse, EVENT);
    }

    /// Client side: remembers when it started. Returns whether the event was this one.
    public static boolean handleEvent(AbstractHorse horse, byte event) {
        if (event != EVENT)
            return false;

        ((Nuzzling)horse).mohorses$setNuzzleStart(horse.tickCount);

        return true;
    }

    /// How far along it is, from 0 to 1, or -1 while the horse isn't nuzzling
    public static float progress(AbstractHorse horse, float partialTicks) {
        int start = ((Nuzzling)horse).mohorses$getNuzzleStart();

        if (start < 0)
            return -1.0F;

        float progress = (horse.tickCount - start + partialTicks) / DURATION;

        return progress >= 0.0F && progress <= 1.0F ? progress : -1.0F;
    }

    private EggNuzzle() {}
}
