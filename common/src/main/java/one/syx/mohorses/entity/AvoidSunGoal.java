package one.syx.mohorses.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/// Horses that burn in daylight (vanilla zombie horses and the undead of this mod, by the entity tag
/// minecraft:burn_in_daylight) look for shade as soon as the sun shines on them. Vanilla's FleeSunGoal only starts once
/// the mob is on fire, and never accepts grass as a place for an animal to hide.
///
/// [StayInShade] keeps them there: they take no path out into the sun.
public class AvoidSunGoal extends Goal {
    private static final int RANGE = 16;
    private static final int RANGE_Y = 4;
    /// How many of the nearest shady places the path finder may choose from
    private static final int CANDIDATES = 24;
    /// Ticks until the next look around if there was no shade to be found
    private static final int RETRY = 60;

    private final AbstractHorse horse;
    private final double speed;
    private @Nullable Path path;
    private int nextTry;

    public AvoidSunGoal(AbstractHorse horse, double speed) {
        this.horse = horse;
        this.speed = speed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    /// Adds both goals if the horse is one that burns
    public static void addTo(AbstractHorse horse, GoalSelector goals) {
        if (horse.is(EntityTypeTags.BURN_IN_DAYLIGHT)) {
            // Before the panic of a horse that burns (priority 1), which would only run about
            goals.addGoal(0, new StayInShade(horse));
            goals.addGoal(0, new AvoidSunGoal(horse, 1.5));
        }
    }

    /// Whether the sun could hurt this horse: not at night, in rain or water, nor with armor on
    private static boolean fearsSun(AbstractHorse horse) {
        return horse.level().environmentAttributes().getValue(EnvironmentAttributes.MONSTERS_BURN, horse.position())
                && horse.getBodyArmorItem().isEmpty() && !horse.isInWaterOrRain();
    }

    /// The sun reaches a horse at eye level, so both its feet and its head have to be covered
    private static boolean inSun(Level level, BlockPos pos) {
        return level.canSeeSky(pos) || level.canSeeSky(pos.above());
    }

    @Override
    public boolean canUse() {
        // Ridden or led by somebody, it goes where it has to
        if (this.horse.isVehicle() || this.horse.isLeashed() || this.horse.tickCount < this.nextTry)
            return false;

        if (!fearsSun(this.horse) || !inSun(this.horse.level(), this.horse.blockPosition()))
            return false;

        Set<BlockPos> shade = findShade();

        this.path = shade.isEmpty() ? null : this.horse.getNavigation().createPath(shade, 0);

        if (this.path == null || !this.path.canReach()) {
            this.nextTry = this.horse.tickCount + RETRY;

            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.horse.getNavigation().isDone() && !this.horse.isVehicle() && !this.horse.isLeashed();
    }

    @Override
    public void start() {
        this.horse.setEating(false);
        this.horse.getNavigation().moveTo(this.path, this.speed);
    }

    @Override
    public void stop() {
        this.path = null;
        this.horse.getNavigation().stop();
        // Not at once again if it got stuck on the way
        this.nextTry = this.horse.tickCount + 20;
    }

    /// The nearest places around that the sky can't be seen from and where the horse can stand
    private Set<BlockPos> findShade() {
        Level level = this.horse.level();
        BlockPos center = this.horse.blockPosition();
        List<BlockPos> found = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RANGE, -RANGE_Y, -RANGE), center.offset(RANGE, RANGE_Y, RANGE))) {
            if (inSun(level, pos) || !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty())
                continue;

            if (this.horse.getNavigation().isStableDestination(pos))
                found.add(pos.immutable());
        }

        found.sort(Comparator.comparingDouble(pos -> pos.distSqr(center)));

        // Spread out a little, so that one blocked corner doesn't use up all of them
        Set<BlockPos> chosen = new HashSet<>();

        for (BlockPos pos : found) {
            if (chosen.size() >= CANDIDATES)
                break;

            if (chosen.stream().noneMatch(other -> other.distSqr(pos) < 2.5))
                chosen.add(pos);
        }

        return chosen;
    }

    /// While the sun shines, paths that start in the shade end where the sun begins (a vanilla feature of the navigation)
    private static class StayInShade extends Goal {
        private final AbstractHorse horse;

        private StayInShade(AbstractHorse horse) {
            this.horse = horse;
        }

        @Override
        public boolean canUse() {
            return !this.horse.isVehicle() && fearsSun(this.horse);
        }

        @Override
        public void start() {
            setAvoidSun(true);
        }

        @Override
        public void stop() {
            setAvoidSun(false);
        }

        private void setAvoidSun(boolean avoid) {
            if (this.horse.getNavigation() instanceof GroundPathNavigation navigation)
                navigation.setAvoidSun(avoid);
        }
    }
}
