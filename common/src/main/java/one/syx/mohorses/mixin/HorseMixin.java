package one.syx.mohorses.mixin;

import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Markings;
import net.minecraft.world.entity.animal.equine.Variant;
import one.syx.mohorses.entity.VanillaHorseEssences;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/// Lets a skeleton horse that gets its flesh back (Essence of Light) come back with the coat it showed while flickering
@Mixin(Horse.class)
public abstract class HorseMixin implements VanillaHorseEssences.HorseVariantAccess {
    @Shadow
    protected abstract void setVariantAndMarkings(Variant variant, Markings markings);

    @Override
    public void mohorses$setVariantAndMarkings(Variant variant, Markings markings) {
        setVariantAndMarkings(variant, markings);
    }
}
