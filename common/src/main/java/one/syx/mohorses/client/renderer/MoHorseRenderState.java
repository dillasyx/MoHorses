package one.syx.mohorses.client.renderer;

import net.minecraft.client.renderer.entity.state.DonkeyRenderState;
import net.minecraft.world.item.DyeColor;
import one.syx.mohorses.entity.HorseKind;
import org.jetbrains.annotations.Nullable;

/// What the horse renderer and model need to know about a [one.syx.mohorses.entity.MoHorse] for one frame
public class MoHorseRenderState extends DonkeyRenderState {
    public HorseKind kind = HorseKind.ZEBRA;
    /// The kind whose model and texture are shown: the own kind, or while transforming sometimes the target kind
    public HorseKind displayedKind = HorseKind.ZEBRA;
    /// The colour a Fairy Horse was dyed, or null for its natural white
    public @Nullable DyeColor fairyColor;
    /// How far an undead kind has rotted
    public int decayStage;
    /// How opaque a ghost is drawn (1 for every other kind)
    public float ghostOpacity = 1.0F;
    /// Drawn hovering (ghosts)
    public boolean floating;
    /// How far the legs are swept back, 0 (normal) to 1 (ghosts always, winged horses in flight)
    public float legSweep;
    /// Flickering to the look of a vanilla zombie horse (Essence of Undead on a plain kind)
    public boolean showsZombieHorse;
    /// A whipped unicorn charging, head down
    public boolean charging;
    /// A zebra dancing to the Shuffling song
    public boolean shuffling;
    /// In the air with wings out
    public boolean flying;
    /// Flying and gaining height, so the wings beat hard
    public boolean flapping;
    /// How far the feathered wings are spread, 0 (folded) to 1 (out)
    public float wingSpread;
    /// How hard the feathered wings beat, 0 (gliding) to 1 (climbing)
    public float wingBeat;
}
