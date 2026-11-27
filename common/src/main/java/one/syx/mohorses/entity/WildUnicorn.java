package one.syx.mohorses.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import one.syx.mohorses.ModConstants;
import org.jetbrains.annotations.Nullable;

/// A unicorn in the wild: very rare, alone, and peaceful until it is attacked. Then it lowers its horn and fights back
/// for a while. It can't be tamed or ridden; tamed unicorns still come from a Nightmare and an Essence of Light.
public class WildUnicorn extends MoHorse {
    /// Blocks they spawn on
    public static final TagKey<Block> SPAWNABLE_ON = TagKey.create(Registries.BLOCK, ModConstants.id("wild_unicorns_spawnable_on"));

    /// How often a group of animals is a wild unicorn, per point of weight a biome gives it
    private static final double CHANCE_PER_WEIGHT = 0.002;
    /// The game tries up to four places for an animal
    private static final int TRIES = 4;
    /// None spawns with another unicorn, wild or tamed, this close
    private static final double NO_OTHERS_WITHIN = 96.0;
    /// How long it stays angry after the last hit, in ticks
    private static final int ANGER = 20 * 20;
    private static final double CHARGE_DISTANCE_SQR = 12 * 12;

    /// It was just spawned by nature, and has yet to make sure that there is no other unicorn around
    private boolean justSpawned;

    public WildUnicorn(EntityType<? extends WildUnicorn> type, Level level) {
        super(type, level, HorseKind.UNICORN);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractChestedHorse.createBaseChestedHorseAttributes()
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.0);
    }

    // ------------------------------------------------------------------------------------------------
    // Spawning
    // ------------------------------------------------------------------------------------------------

    /// On grass, moss, snow and the like, in any light (there is little of it in Lush Caves).
    ///
    /// The weights of the biomes only say how common it is in one biome compared to another. How many other animals a biome
    /// has is taken out of it again: in a meadow, where so little else lives, the same weight would mean a lot more unicorns.
    public static boolean checkSpawnRules(EntityType<? extends Mob> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos.below()).is(SPAWNABLE_ON))
            return false;

        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION)
            return true;

        double groupChance = Math.min(1.0, CHANCE_PER_WEIGHT * totalWeight(level, pos));

        if (random.nextDouble() >= 1.0 - Math.pow(1.0 - groupChance, 1.0 / TRIES))
            return false;

        // Chunks are generated on other threads, which must not look at the entities of the level: those look when they appear
        return reason != EntitySpawnReason.NATURAL || !(level instanceof ServerLevel serverLevel) || !unicornNear(serverLevel, pos, null);
    }

    /// The weights of all animals together that could spawn there
    private static int totalWeight(ServerLevelAccessor level, BlockPos pos) {
        MobSpawnSettings spawns = level.getBiome(pos).value().getMobSettings();

        return spawns.getMobs(MobCategory.CREATURE).unwrap().stream().mapToInt(Weighted::weight).sum();
    }

    private static boolean unicornNear(ServerLevel level, BlockPos pos, @Nullable MoHorse except) {
        return !level.getEntitiesOfClass(MoHorse.class, new AABB(pos).inflate(NO_OTHERS_WITHIN),
                horse -> horse != except && horse.kind() == HorseKind.UNICORN && horse.isAlive()).isEmpty();
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        this.justSpawned = reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION;

        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected void randomizeAttributes(RandomSource random) {
        super.randomizeAttributes(random);

        AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);

        if (health != null)
            health.setBaseValue(30.0);
    }

    /// Animals prefer grass and light to walk to and spawn on; that would keep them out of Lush Caves and Groves
    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return Math.max(0.0F, super.getWalkTargetValue(pos, level));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.justSpawned && level() instanceof ServerLevel serverLevel) {
            this.justSpawned = false;

            if (unicornNear(serverLevel, blockPosition(), this))
                discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("JustSpawned", this.justSpawned);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.justSpawned = input.getBooleanOr("JustSpawned", false);
    }

    // ------------------------------------------------------------------------------------------------
    // Behaviour
    // ------------------------------------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.5, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (level().isClientSide() || getTarget() == null)
            return;

        if (this.tickCount - getLastHurtByMobTimestamp() > ANGER)
            setTarget(null);
        else if (!isCharging() && distanceToSqr(getTarget()) < CHARGE_DISTANCE_SQR)
            lowerHorn();
    }

    /// It charges for as long as it is after somebody
    @Override
    protected boolean keepsCharging() {
        return getTarget() != null;
    }

    /// No taming, riding, feeding or essences
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    /// No saddle and no armor, not even from a dispenser
    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return slot != EquipmentSlot.SADDLE && slot != EquipmentSlot.BODY && super.canUseSlot(slot);
    }
}
