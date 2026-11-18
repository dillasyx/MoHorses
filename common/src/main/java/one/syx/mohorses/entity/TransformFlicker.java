package one.syx.mohorses.entity;

/// The rhythm of a transformation: the horse flickers between its old and its new look, slowly at first and faster
/// and faster, and at the very end it shows the new look. Used for the mod's horses and for vanilla horses alike.
public final class TransformFlicker {
    /// Whether the new look shows at this point of the transformation (0 = just started, 1 = done)
    public static boolean showsNewLook(float progress) {
        if (progress > 0.95F)
            return true;

        // The number of switches so far grows slowly at first, then quickly (about 22 switches in total)
        int switches = (int)(4.0F * progress + 18.0F * progress * progress * progress);

        return switches % 2 == 1;
    }

    private TransformFlicker() {}
}
