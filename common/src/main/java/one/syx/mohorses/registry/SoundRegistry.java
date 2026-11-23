package one.syx.mohorses.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import one.syx.mohorses.ModConstants;
import one.syx.mohorses.entity.HorseKind;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class SoundRegistry {
    public static void init() {}

    // SOUNDS
    public static final Supplier<SoundEvent> FEEDING_SUGAR_LUMP = registerSound("feeding_sugar_lump");
    public static final Supplier<SoundEvent> WHIP = registerSound("item.whip.crack");

    /// The voice of one kind
    public record HorseVoice(Supplier<SoundEvent> ambient, Supplier<SoundEvent> hurt, Supplier<SoundEvent> death, Supplier<SoundEvent> angry) {}

    /// Every kind has a voice of its own, made from the Mo' Creatures horse, donkey and zebra sounds, except the undead:
    /// they sound like vanilla zombie and skeleton horses
    private static final Map<HorseKind, HorseVoice> HORSE_VOICES = new EnumMap<>(HorseKind.class);

    static {
        for (HorseKind kind : HorseKind.values()) {
            if (kind.isUndead())
                continue;

            String id = "entity." + kind.id() + ".";

            HORSE_VOICES.put(kind, new HorseVoice(registerSound(id + "ambient"), registerSound(id + "hurt"), registerSound(id + "death"), registerSound(id + "angry")));
        }
    }

    /// The kind's own voice, or null for the undead
    public static @Nullable HorseVoice voice(HorseKind kind) {
        return HORSE_VOICES.get(kind);
    }

    // Maggots (vanilla slime sounds, higher and quieter)
    public static final Supplier<SoundEvent> MAGGOT_HURT = registerSound("entity.maggot.hurt");
    public static final Supplier<SoundEvent> MAGGOT_DEATH = registerSound("entity.maggot.death");

    // MUSIC DISC
    public static final Identifier SHUFFLING_ID = ModConstants.id("shuffling");
    public static final Supplier<SoundEvent> SHUFFLING = registerSound("shuffling");
    public static final ResourceKey<JukeboxSong> SHUFFLING_SONG_KEY = ResourceKey.create(Registries.JUKEBOX_SONG, ModConstants.id("shuffling"));

    private static Supplier<SoundEvent> registerSound(String id) {
        return ModConstants.PLATFORM.registerSound(id, () -> SoundEvent.createVariableRangeEvent(ModConstants.id(id)));
    }
}
