package one.syx.mohorses.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import one.syx.mohorses.registry.SoundRegistry;

public class MaggotEntity extends Monster implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.maggot.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.maggot.walk");
    private static final RawAnimation WIGGLE = RawAnimation.begin().thenPlay("animation.maggot.wiggle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int wiggleTicks = 0;

    public MaggotEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);

        this.xpReward = 1 + getRandom().nextInt(3);
    }

    public boolean isWiggling() {
        return this.wiggleTicks > 0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.4D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.wiggleTicks > 0) {
            this.wiggleTicks--;
        }
        else if (getRandom().nextInt(20) == 0) {
            // On average once a second, wiggle for two seconds
            this.wiggleTicks = 40;
        }
    }

    /// Monsters sound like a hurt player by default; a maggot only squishes
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundRegistry.MAGGOT_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.MAGGOT_DEATH.get();
    }

    /// Crawling makes no sound
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {}

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<MaggotEntity>("move", 5, test -> test.setAndContinue(test.isMoving() ? WALK : IDLE)));
        controllers.add(new AnimationController<MaggotEntity>("wiggle", 0, test -> {
            if (test.animatable().isWiggling() && !test.isMoving())
                return test.setAndContinue(WIGGLE);

            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
