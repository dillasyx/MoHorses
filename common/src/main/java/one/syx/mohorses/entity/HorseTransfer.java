package one.syx.mohorses.entity;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// Carries a horse over into the one that replaces it (a transformation, rotting, a ghost): it stays the same horse,
/// with the same owner, tameness and stats.
public final class HorseTransfer {
    private static final List<Holder<Attribute>> STATS = List.of(Attributes.MAX_HEALTH, Attributes.MOVEMENT_SPEED, Attributes.JUMP_STRENGTH);

    /// Implemented by every horse through AbstractHorseMixin. The owner is handed on as a reference, so it is kept
    /// even while that player is offline (vanilla's setOwner needs the player to be loaded).
    public interface OwnerAccess {
        void mohorses$setOwnerReference(@Nullable EntityReference<LivingEntity> owner);
    }

    /// Copies owner, tameness and stats. The new horse keeps the share of its health the old one had, or is fully
    /// healed (a ghost rising from a dead horse).
    public static void copy(AbstractHorse from, AbstractHorse to, boolean fullHealth) {
        to.setTamed(from.isTamed());
        ((OwnerAccess)to).mohorses$setOwnerReference(from.getOwnerReference());

        for (Holder<Attribute> stat : STATS) {
            AttributeInstance instance = to.getAttribute(stat);

            if (instance != null)
                instance.setBaseValue(from.getAttributeBaseValue(stat));
        }

        to.setHealth(fullHealth ? to.getMaxHealth() : to.getMaxHealth() * from.getHealth() / from.getMaxHealth());
    }

    private HorseTransfer() {}
}
