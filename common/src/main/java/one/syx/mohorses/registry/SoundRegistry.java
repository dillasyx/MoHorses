package one.syx.mohorses.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import one.syx.mohorses.ModConstants;

import java.util.function.Supplier;

public final class SoundRegistry {
    public static void init() {}

    // SOUNDS
    public static final Supplier<SoundEvent> FEEDING_SUGAR_LUMP = registerSound("feeding_sugar_lump");
    public static final Supplier<SoundEvent> WHIP = registerSound("item.whip.crack");

    // Zebras (the Mo' Creatures zebra sounds)
    public static final Supplier<SoundEvent> ZEBRA_AMBIENT = registerSound("entity.zebra.ambient");
    public static final Supplier<SoundEvent> ZEBRA_HURT = registerSound("entity.zebra.hurt");
    public static final Supplier<SoundEvent> ZEBRA_DEATH = registerSound("entity.zebra.death");
    public static final Supplier<SoundEvent> ZEBRA_ANGRY = registerSound("entity.zebra.angry");

    // Ghost horses (the Mo' Creatures ghost horse sounds)
    public static final Supplier<SoundEvent> GHOST_HORSE_AMBIENT = registerSound("entity.ghost_horse.ambient");
    public static final Supplier<SoundEvent> GHOST_HORSE_HURT = registerSound("entity.ghost_horse.hurt");
    public static final Supplier<SoundEvent> GHOST_HORSE_DEATH = registerSound("entity.ghost_horse.death");
    public static final Supplier<SoundEvent> GHOST_HORSE_ANGRY = registerSound("entity.ghost_horse.angry");

    // MUSIC DISC
    public static final Identifier SHUFFLING_ID = ModConstants.id("shuffling");
    public static final Supplier<SoundEvent> SHUFFLING = registerSound("shuffling");
    public static final ResourceKey<JukeboxSong> SHUFFLING_SONG_KEY = ResourceKey.create(Registries.JUKEBOX_SONG, ModConstants.id("shuffling"));

    private static Supplier<SoundEvent> registerSound(String id) {
        return ModConstants.PLATFORM.registerSound(id, () -> SoundEvent.createVariableRangeEvent(ModConstants.id(id)));
    }
}
