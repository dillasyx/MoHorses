package one.syx.mohorses.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;

/// A Nightmare that roams the Nether as a hostile mob. It attacks players on sight, can't be tamed or ridden,
/// and despawns like other monsters. Tamed Nightmares still come from a Zorse and an Essence of Fire.
public class WildNightmare extends MoHorse {
    public WildNightmare(EntityType<? extends WildNightmare> type, Level level) {
        super(type, level, HorseKind.NIGHTMARE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractChestedHorse.createBaseChestedHorseAttributes()
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.FOLLOW_RANGE, 24.0);
    }

    /// Spawns on any solid block, in any light, but never on peaceful
    public static boolean checkSpawnRules(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.4, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /// No taming, riding, feeding or essences
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /// Animals prefer grass and light to walk to and spawn on; down in the dark Nether that would stop every natural spawn
    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return 0.0F;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return !hasCustomName();
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // A smouldering mane
        if (level().isClientSide() && this.random.nextInt(3) == 0)
            level().addParticle(ParticleTypes.FLAME, getRandomX(0.6), getY() + getBbHeight() * (0.6 + this.random.nextDouble() * 0.4), getRandomZ(0.6), 0.0, 0.02, 0.0);
    }
}
