package one.syx.mohorses.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import one.syx.mohorses.ModConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.Prediction;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.Vec3;
import one.syx.mohorses.item.HorseAmuletItem;
import one.syx.mohorses.registry.EntityRegistry;
import one.syx.mohorses.registry.ItemRegistry;
import one.syx.mohorses.registry.ParticleRegistry;
import one.syx.mohorses.registry.SoundRegistry;
import org.jetbrains.annotations.Nullable;

/// The one entity class behind every horse of this mod. What a particular horse is and can do
/// is decided by its [HorseKind], which is fixed per entity type.
///
/// It extends the vanilla chested horse, so chests, their inventory and saving work exactly like a donkey's.
public class MoHorse extends AbstractChestedHorse {

    private static final float ESSENCE_HEAL_AMOUNT = 20.0F;
    /// A charging unicorn (whipped at a creature) hits harder
    private static final float UNICORN_CHARGE_DAMAGE = 10.0F;
    /// How long a whipped unicorn keeps charging at its target, in ticks
    private static final int CHARGE_DURATION = 40;
    /// DATA_TRANSFORM_TARGET value for "turning into a vanilla zombie horse"
    private static final int ZOMBIE_HORSE_TARGET = -1;
    /// How long one piece of redstone keeps a Nightmare burning, in ticks
    private static final int FLAME_DURATION = 60 * 20;
    /// How long an essence transformation takes, in ticks
    public static final int TRANSFORM_DURATION = 80;

    /// The kind this horse is turning into (ordinal + 1), or 0 while it isn't transforming
    private static final EntityDataAccessor<Integer> DATA_TRANSFORM_TARGET = SynchedEntityData.defineId(MoHorse.class, EntityDataSerializers.INT);
    /// The entity a whipped unicorn charges at (its id), or -1 when it just charges ahead
    private static final EntityDataAccessor<Integer> DATA_CHARGE_TARGET = SynchedEntityData.defineId(MoHorse.class, EntityDataSerializers.INT);
    /// A zebra dancing to the Shuffling song
    private static final EntityDataAccessor<Boolean> DATA_SHUFFLING = SynchedEntityData.defineId(MoHorse.class, EntityDataSerializers.BOOLEAN);
    /// How close a jukebox playing the Shuffling song has to be for zebras to dance, in blocks
    private static final int SHUFFLE_RANGE = 16;
    /// Ticks left of a whipped unicorn's charge (synced: the rider's side moves the horse, and the head is lowered)
    private static final EntityDataAccessor<Integer> DATA_CHARGE_TICKS = SynchedEntityData.defineId(MoHorse.class, EntityDataSerializers.INT);
    /// Ticks left until the transformation completes
    private static final EntityDataAccessor<Integer> DATA_TRANSFORM_TICKS = SynchedEntityData.defineId(MoHorse.class, EntityDataSerializers.INT);
    /// How far an undead kind has rotted (0 to ZombieHorseDecay.STAGES)
    private static final EntityDataAccessor<Integer> DATA_DECAY_STAGE = SynchedEntityData.defineId(MoHorse.class, EntityDataSerializers.INT);
    /// The dye colour of a Fairy Horse (DyeColor id), or -1 for its natural white
    private static final EntityDataAccessor<Integer> DATA_FAIRY_COLOR = SynchedEntityData.defineId(MoHorse.class, EntityDataSerializers.INT);
    /// Ticks left until a ghost that just rose from its dead horse is fully visible
    private static final EntityDataAccessor<Integer> DATA_APPEAR_TICKS = SynchedEntityData.defineId(MoHorse.class, EntityDataSerializers.INT);
    private static final double FLAME_TRAIL_MIN_SPEED_SQR = 0.04;
    /// How long a ghost takes to fade in after its horse died, in ticks
    public static final int APPEAR_DURATION = 60;

    private final HorseKind kind;
    private int ramCooldown;
    /// Client side: how long the horse has been off the ground (stepping up a block doesn't count as a jump)
    private int airTicks;
    /// Maggots still to crawl out during an Essence of Undead transformation
    private int maggotsLeft;
    private int flameTicks;
    private int decayTicks;
    // Client-side wing animation state
    private float wingSpread;
    private float wingSpreadO;
    private float wingBeat;
    private float wingBeatO;
    // Client-side ghost flicker: the opacity drifts towards a new random value every few seconds
    private float ghostOpacity = 0.5F;
    private float ghostOpacityO = 0.5F;
    private float ghostOpacityTarget = 0.5F;
    private boolean lovedBySpecialFood;
    /// Put in love mode with a pumpkin (a Unicorn and a Pegasus then breed a Nightmare instead of a Fairy Horse)
    private boolean pumpkinFed;
    private Vec3 lastTickPosition = Vec3.ZERO;

    public MoHorse(EntityType<? extends MoHorse> entityType, Level level, HorseKind kind) {
        super(entityType, level);

        this.kind = kind;
        // Chested horses can't gallop by default (that only affects the step sounds)
        this.canGallop = true;
    }

    /// Blocks zebras spawn on: the usual grass, plus the sand and terracotta of deserts and badlands
    public static final TagKey<Block> ZEBRAS_SPAWNABLE_ON = TagKey.create(Registries.BLOCK, ModConstants.id("zebras_spawnable_on"));

    /// Natural spawning of zebras: like other animals, but also on sand and terracotta
    public static boolean checkZebraSpawnRules(EntityType<? extends Mob> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        boolean bright = EntitySpawnReason.ignoresLightRequirements(reason) || level.getRawBrightness(pos, 0) > 8;

        return bright && level.getBlockState(pos.below()).is(ZEBRAS_SPAWNABLE_ON);
    }

    /// Foals get the hitbox of a vanilla foal of the same shape
    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        if (!isBaby())
            return super.getDefaultDimensions(pose);

        EntityDimensions adult = getType().getDimensions();

        return adult.withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0F, adult.height() - 0.125F, 0.0F))
                .scale(kind().foalScale());
    }

    /// The kind of this horse. Never null, even while the super constructor is still running.
    public HorseKind kind() {
        return this.kind != null ? this.kind : HorseKind.ZEBRA;
    }

    // ------------------------------------------------------------------------------------------------
    // Attributes & equipment
    // ------------------------------------------------------------------------------------------------

    /// The stats of a newly spawned horse, the same as its vanilla counterpart: horse-like kinds roll like a horse,
    /// the Zonkey is a donkey, the undead with flesh a zombie horse and the skeletons a skeleton horse.
    /// (Foals get the average of their parents, and transformations keep the stats, like in vanilla.)
    @Override
    protected void randomizeAttributes(RandomSource random) {
        HorseKind kind = kind();

        if (kind.donkeyLike()) {
            setBaseValue(Attributes.MAX_HEALTH, generateMaxHealth(random::nextInt));
            setBaseValue(Attributes.MOVEMENT_SPEED, 0.175F);
            setBaseValue(Attributes.JUMP_STRENGTH, 0.5);
        }
        else if (kind.decaysInto() != null) {
            setBaseValue(Attributes.MAX_HEALTH, 25.0);
            setBaseValue(Attributes.MOVEMENT_SPEED, (9.0 + random.nextDouble() + random.nextDouble() + random.nextDouble()) / 42.16F);
            setBaseValue(Attributes.JUMP_STRENGTH, 0.5 + (random.nextDouble() + random.nextDouble() + random.nextDouble()) / 15.0);
        }
        else if (kind.isUndead()) {
            setBaseValue(Attributes.MAX_HEALTH, 15.0);
            setBaseValue(Attributes.MOVEMENT_SPEED, 0.2F);
            setBaseValue(Attributes.JUMP_STRENGTH, generateJumpStrength(random::nextDouble));
        }
        else {
            setBaseValue(Attributes.MAX_HEALTH, generateMaxHealth(random::nextInt));
            setBaseValue(Attributes.MOVEMENT_SPEED, generateSpeed(random::nextDouble));
            setBaseValue(Attributes.JUMP_STRENGTH, generateJumpStrength(random::nextDouble));
        }
    }

    private void setBaseValue(Holder<Attribute> attribute, double value) {
        AttributeInstance instance = getAttribute(attribute);

        if (instance != null)
            instance.setBaseValue(value);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        if (slot == EquipmentSlot.BODY)
            return kind().armorRule() != HorseKind.ArmorRule.NONE;

        return super.canUseSlot(slot);
    }

    @Override
    public boolean isEquippableInSlot(ItemStack stack, EquipmentSlot slot) {
        if (slot == EquipmentSlot.BODY && !kind().canWear(stack))
            return false;

        return super.isEquippableInSlot(stack, slot);
    }

    @Override
    public void hurtArmor(DamageSource damageSource, float damageAmount) {
        doHurtEquipment(damageSource, damageAmount, EquipmentSlot.BODY);
    }

    // ------------------------------------------------------------------------------------------------
    // Interaction: essences, shears, then everything a donkey does (food, taming, chest, riding)
    // ------------------------------------------------------------------------------------------------

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean opensInventory = !isBaby() && isTamed() && player.isSecondaryUseActive();

        if (!isVehicle() && !opensInventory && !stack.isEmpty()) {
            if (kind() == HorseKind.NIGHTMARE && stack.is(Items.REDSTONE) && isTamed() && !isBaby()) {
                if (level() instanceof ServerLevel serverLevel) {
                    stack.consume(1, player);
                    ignite(serverLevel);
                }

                return InteractionResult.SUCCESS;
            }

            // Wild chested horses refuse every item before it gets a say, but sugar lumps are how zebras are tamed
            if (kind() == HorseKind.ZEBRA && stack.is(ItemRegistry.SUGAR_LUMP.get()))
                return stack.interactLivingEntity(player, this, hand);

            // Like in Mo' Creatures, a zebra trades any music disc for the Shuffling disc
            if (kind() == HorseKind.ZEBRA && stack.has(DataComponents.JUKEBOX_PLAYABLE) && !stack.is(ItemRegistry.SHUFFLING_MUSIC_DISC.get())) {
                if (level() instanceof ServerLevel serverLevel) {
                    // The disc itself turns into the Shuffling disc (in creative mode too)
                    ItemStack disc = new ItemStack(ItemRegistry.SHUFFLING_MUSIC_DISC.get());

                    if (stack.getCount() == 1) {
                        player.setItemInHand(hand, disc);
                    }
                    else {
                        stack.shrink(1);
                        player.getInventory().placeItemBackInInventory(disc, Prediction.SERVER_ONLY);
                    }

                    serverLevel.sendParticles(ParticleTypes.NOTE, getX(), getY() + getBbHeight() + 0.3, getZ(), 8, 0.5, 0.3, 0.5, 1.0);
                    playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1.0F, 1.2F);
                }

                return InteractionResult.SUCCESS;
            }

            if (kind().lovesPumpkin() && stack.is(Items.PUMPKIN) && isTamed() && !isBaby()) {
                if (level() instanceof ServerLevel serverLevel) {
                    if (getAge() == 0 && !isInLove()) {
                        stack.consume(1, player);
                        this.pumpkinFed = true;
                        fallInLoveWithSpecialFood(player);
                    }
                    else {
                        burst(serverLevel, ParticleTypes.SMOKE, 7);
                    }
                }

                return InteractionResult.SUCCESS;
            }

            DyeColor dye = stack.get(DataComponents.DYE);

            if (kind() == HorseKind.FAIRY_HORSE && dye != null && isTamed()) {
                // Fairy Horses are born white and can be dyed once
                if (getFairyColor() != null || dye == DyeColor.WHITE)
                    return InteractionResult.PASS;

                if (level() instanceof ServerLevel serverLevel) {
                    stack.consume(1, player);
                    setFairyColor(dye);
                    burst(serverLevel, ParticleTypes.END_ROD, 20);
                    playSound(SoundEvents.DYE_USE, 1.0F, 1.0F);
                }

                return InteractionResult.SUCCESS;
            }

            InteractionResult essenceResult = useEssence(player, stack);

            if (essenceResult != null)
                return essenceResult;

            if (hasChest() && stack.is(Items.SHEARS)) {
                if (level() instanceof ServerLevel serverLevel) {
                    removeChest(serverLevel);
                    playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
                    stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    /// Handles the three essences. Returns null if the stack isn't something this horse reacts to,
    /// so the normal horse interaction can take over.
    private @Nullable InteractionResult useEssence(Player player, ItemStack stack) {
        HorseKind kind = kind();
        // The sky transformations need the horse standing on the ground, above the clouds
        boolean inTheSky = onGround() && getY() >= level().environmentAttributes().getValue(EnvironmentAttributes.CLOUD_HEIGHT, position()) + 4.0F;
        HorseKind target = kind.transformedBy(stack, inTheSky);

        // The plain kinds become vanilla zombie horses
        if (stack.is(ItemRegistry.ESSENCE_OF_UNDEAD.get()) && kind.becomesZombieHorse()) {
            if (!isTamed() || isBaby() || isTransforming())
                return InteractionResult.PASS;

            if (level() instanceof ServerLevel) {
                stack.consume(1, player);
                startTransformation(ZOMBIE_HORSE_TARGET);
            }

            return InteractionResult.SUCCESS;
        }
        boolean heals = kind.healedBy(stack) && getHealth() < getMaxHealth();
        boolean loves = kind.lovesEssence(stack);

        if (target == null && !heals && !loves && !kind.needsSkyFor(stack))
            return null;

        // Essences only work on your own grown horses
        if (!isTamed() || isBaby())
            return InteractionResult.PASS;

        if (!(level() instanceof ServerLevel serverLevel))
            return InteractionResult.SUCCESS;

        if (target != null) {
            if (isTransforming())
                return InteractionResult.PASS;

            stack.consume(1, player);
            startTransformation(target.ordinal() + 1);

            return InteractionResult.SUCCESS;
        }

        if (heals) {
            stack.consume(1, player);
            heal(ESSENCE_HEAL_AMOUNT);
            burst(serverLevel, ParticleTypes.HEART, 7);

            return InteractionResult.SUCCESS;
        }

        if (loves && getAge() == 0 && !isInLove()) {
            stack.consume(1, player);
            this.pumpkinFed = false;
            fallInLoveWithSpecialFood(player);

            return InteractionResult.SUCCESS;
        }

        // Right essence, but nothing to do with it right now (too low, already in love, full health)
        burst(serverLevel, ParticleTypes.SMOKE, 7);

        return InteractionResult.PASS;
    }

    private void removeChest(ServerLevel level) {
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);

            if (!stack.isEmpty()) {
                spawnAtLocation(level, stack);
                this.inventory.setItem(i, ItemStack.EMPTY);
            }
        }

        spawnAtLocation(level, Items.CHEST);
        setChest(false);
        createInventory();
    }

    // ------------------------------------------------------------------------------------------------
    // Transformation
    // ------------------------------------------------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TRANSFORM_TARGET, 0);
        builder.define(DATA_TRANSFORM_TICKS, 0);
        builder.define(DATA_FAIRY_COLOR, -1);
        builder.define(DATA_DECAY_STAGE, 0);
        builder.define(DATA_APPEAR_TICKS, 0);
        builder.define(DATA_CHARGE_TARGET, -1);
        builder.define(DATA_CHARGE_TICKS, 0);
        builder.define(DATA_SHUFFLING, false);
    }

    /// The horse stands still for a few seconds while it flickers between its old and new look, then transforms.
    /// Turning undead, two or three maggots crawl out of it one after the other while it does.
    /// @param target the target kind's ordinal + 1, or ZOMBIE_HORSE_TARGET
    private void startTransformation(int target) {
        this.entityData.set(DATA_TRANSFORM_TARGET, target);
        this.entityData.set(DATA_TRANSFORM_TICKS, TRANSFORM_DURATION);

        if (isTurningUndead()) {
            this.maggotsLeft = 2 + this.random.nextInt(2);
            playSound(SoundEvents.ZOMBIE_INFECT, 1.0F, 0.8F);
        }
        else {
            playSound(SoundEvents.BEACON_POWER_SELECT, 1.0F, 0.8F);
        }
    }

    public boolean isTransforming() {
        return this.entityData.get(DATA_TRANSFORM_TARGET) != 0;
    }

    /// Whether this horse is turning into a vanilla zombie horse (Essence of Undead on a plain kind)
    public boolean isTurningIntoZombieHorse() {
        return this.entityData.get(DATA_TRANSFORM_TARGET) == ZOMBIE_HORSE_TARGET;
    }

    /// Whether the transformation going on is one into an undead
    private boolean isTurningUndead() {
        HorseKind target = getTransformTarget();

        return isTurningIntoZombieHorse() || (target != null && target.isUndead());
    }

    /// The kind this horse is turning into, or null
    public @Nullable HorseKind getTransformTarget() {
        int target = this.entityData.get(DATA_TRANSFORM_TARGET);

        return target > 0 && target <= HorseKind.values().length ? HorseKind.values()[target - 1] : null;
    }

    /// How far along the transformation is, from 0 (just started) to 1 (done)
    public float getTransformProgress(float partialTick) {
        float ticksLeft = this.entityData.get(DATA_TRANSFORM_TICKS) - partialTick;

        return Mth.clamp(1.0F - ticksLeft / TRANSFORM_DURATION, 0.0F, 1.0F);
    }

    private void tickTransformation(ServerLevel level) {
        if (!isTransforming())
            return;

        HorseKind target = getTransformTarget();
        boolean undead = isTurningUndead();
        int ticksLeft = this.entityData.get(DATA_TRANSFORM_TICKS) - 1;

        // The maggots come out one by one: after a second, halfway and near the end
        if (undead && this.maggotsLeft > 0 && ticksLeft % 25 == 5) {
            spawnMaggots(level, this, 1);
            this.maggotsLeft--;
        }

        if (ticksLeft > 0) {
            this.entityData.set(DATA_TRANSFORM_TICKS, ticksLeft);

            if (ticksLeft == TRANSFORM_DURATION / 2)
                playSound(undead ? SoundEvents.ZOMBIE_HORSE_HURT : SoundEvents.AMETHYST_BLOCK_CHIME, 1.5F, 0.7F);

            return;
        }

        this.entityData.set(DATA_TRANSFORM_TARGET, 0);
        this.entityData.set(DATA_TRANSFORM_TICKS, 0);

        // Whatever is left of the maggots
        if (this.maggotsLeft > 0)
            spawnMaggots(level, this, this.maggotsLeft);

        this.maggotsLeft = 0;

        if (target != null)
            transformTo(target, level);
        else
            turnIntoZombieHorse(level);
    }

    /// Client-side sparkle around a transforming horse, thicker towards the end
    private void tickTransformationParticles() {
        if (!isTransforming())
            return;

        HorseKind target = getTransformTarget();
        float progress = getTransformProgress(0.0F);

        // Turning undead: the flesh starts to rot and falls off, with a little smoke
        if (isTurningUndead()) {
            int count = 1 + (int)(progress * progress * 4);

            for (int i = 0; i < count; i++)
                level().addParticle(ParticleRegistry.UNDEAD_FLESH.get(), getRandomX(0.7), getY() + getBbHeight() * (0.4 + this.random.nextFloat() * 0.5), getRandomZ(0.7), 0.0, 0.0, 0.0);

            if (this.random.nextFloat() < 0.3F + progress * 0.5F)
                level().addParticle(ParticleTypes.SMOKE, getRandomX(0.8), getRandomY(), getRandomZ(0.8), 0.0, 0.02, 0.0);

            if (this.random.nextFloat() < 0.15F)
                level().addParticle(ParticleTypes.MYCELIUM, getRandomX(0.8), getRandomY(), getRandomZ(0.8), 0.0, 0.0, 0.0);

            return;
        }

        int count = 1 + (int)(progress * progress * 6);

        for (int i = 0; i < count; i++) {
            double x = getRandomX(0.8);
            double y = getRandomY();
            double z = getRandomZ(0.8);
            double rise = 0.02 + progress * 0.06;

            level().addParticle(particleFor(target), x, y, z, 0.0, rise, 0.0);

            if (this.random.nextFloat() < 0.5F)
                level().addParticle(ParticleTypes.ENCHANT, x, y + 0.5, z, (this.random.nextDouble() - 0.5) * 0.5, 0.2, (this.random.nextDouble() - 0.5) * 0.5);
        }
    }

    /// A transforming horse can't be ridden off or walk away
    @Override
    public boolean isImmobile() {
        return super.isImmobile() || isTransforming();
    }

    /// Replaces this horse with one of another kind, keeping owner, stats, name, saddle and chest contents
    private void transformTo(HorseKind target, ServerLevel level) {
        boolean chested = hasChest();
        ItemStack[] chestItems = new ItemStack[this.inventory.getContainerSize()];

        for (int i = 0; i < chestItems.length; i++) {
            chestItems[i] = this.inventory.getItem(i).copy();
            this.inventory.setItem(i, ItemStack.EMPTY);
        }

        MoHorse transformed = convertTo(EntityRegistry.horse(target).get(), ConversionParams.single(this, true, false), EntitySpawnReason.CONVERSION, next -> {
            HorseTransfer.copy(this, next, false);
            next.setAge(getAge());
            next.setTemper(getTemper());

            if (chested) {
                next.setChest(true);
                next.createInventory();

                for (int i = 0; i < chestItems.length && i < next.inventory.getContainerSize(); i++) {
                    next.inventory.setItem(i, chestItems[i]);
                }
            }

            // Armor the new kind can't wear is handed back
            ItemStack armor = next.getBodyArmorItem();

            if (!armor.isEmpty() && !target.canWear(armor)) {
                next.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
                next.spawnAtLocation(level, armor);
            }
        });

        if (transformed != null) {
            transformed.burst(level, target.isUndead() ? ParticleRegistry.UNDEAD_FLESH.get() : particleFor(target), 40);
            transformed.playSound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 1.0F, 1.2F);
        }
    }

    private static ParticleOptions particleFor(HorseKind kind) {
        return switch (kind) {
            case NIGHTMARE -> ParticleTypes.FLAME;
            case BAT_HORSE -> ParticleTypes.SQUID_INK;
            case DARK_PEGASUS -> ParticleTypes.REVERSE_PORTAL;
            case UNICORN -> ParticleTypes.END_ROD;
            case PEGASUS -> ParticleTypes.GLOW;
            default -> ParticleTypes.WITCH;
        };
    }

    private void burst(ServerLevel level, ParticleOptions particle, int count) {
        level.sendParticles(particle, getX(), getY() + getBbHeight() * 0.6, getZ(), count, 0.6, 0.6, 0.6, 0.02);
    }

    // ------------------------------------------------------------------------------------------------
    // Breeding
    // ------------------------------------------------------------------------------------------------

    /// Golden food only puts kinds in love mode that breed like vanilla horses. The others need their essence (or a pumpkin).
    @Override
    public void setInLove(@Nullable Player player) {
        if (kind().breedsWithFood() || this.lovedBySpecialFood)
            super.setInLove(player);
    }

    private void fallInLoveWithSpecialFood(Player player) {
        this.lovedBySpecialFood = true;
        setInLove(player);
        this.lovedBySpecialFood = false;
    }

    /// The kind of foal this horse has with the partner, or null
    private @Nullable HorseKind offspringKind(Animal partner) {
        boolean pumpkin = this.pumpkinFed || (partner instanceof MoHorse other && other.pumpkinFed);

        return kind().offspringWith(partner, pumpkin);
    }

    @Override
    public boolean canMate(Animal partner) {
        if (partner == this || !(partner instanceof AbstractHorse other))
            return false;

        return canParent() && isReadyToBreed(other) && offspringKind(partner) != null;
    }

    /// The same checks as the vanilla `canParent`, which can't be called on other horses from here
    private static boolean isReadyToBreed(AbstractHorse horse) {
        return !horse.isVehicle()
                && !horse.isPassenger()
                && horse.isTamed()
                && !horse.isBaby()
                && horse.getHealth() >= horse.getMaxHealth()
                && horse.isInLove();
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        HorseKind childKind = partner instanceof Animal animal ? offspringKind(animal) : null;

        if (childKind == null)
            return null;

        MoHorse child = EntityRegistry.horse(childKind).get().create(level, EntitySpawnReason.BREEDING);

        if (child != null) {
            setOffspringAttributes(partner, child);

            // A Fairy foal of two Fairy Horses takes after one of its parents
            if (childKind == HorseKind.FAIRY_HORSE && kind() == HorseKind.FAIRY_HORSE && partner instanceof MoHorse other)
                child.setFairyColor(this.random.nextBoolean() ? getFairyColor() : other.getFairyColor());
        }

        return child;
    }

    /// Fairy Horses cost their parents. Bred from a Unicorn and a Pegasus, the Unicorn is gone (its saddle, armor and
    /// chest drop) and the Pegasus is left behind in a Pegasus Amulet. Two Fairy Horses both vanish.
    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal partner) {
        HorseKind childKind = offspringKind(partner);

        super.spawnChildFromBreeding(level, partner);
        this.pumpkinFed = false;

        if (partner instanceof MoHorse other)
            other.pumpkinFed = false;

        if (childKind == null || !childKind.consumesParents())
            return;

        consumeAsParent(level);

        if (partner instanceof MoHorse other)
            other.consumeAsParent(level);
    }

    private void consumeAsParent(ServerLevel level) {
        if (kind() == HorseKind.PEGASUS) {
            ItemStack amulet = HorseAmuletItem.capture(this, level);

            if (!amulet.isEmpty()) {
                spawnAtLocation(level, amulet);
                burst(level, ParticleTypes.END_ROD, 40);

                return;
            }
        }

        vanish(level);
    }

    // ------------------------------------------------------------------------------------------------
    // Undead
    // ------------------------------------------------------------------------------------------------

    public int getDecayStage() {
        return this.entityData.get(DATA_DECAY_STAGE);
    }

    /// Undead kinds rot like zombie horses: a stage every few minutes, and at the end they are skeletons
    private void tickDecay(ServerLevel level) {
        HorseKind skeleton = kind().decaysInto();

        if (skeleton == null || isTransforming() || !isAlive())
            return;

        if (++this.decayTicks < ZombieHorseDecay.TICKS_PER_STAGE)
            return;

        this.decayTicks = 0;

        int stage = getDecayStage() + 1;

        // Every stage of rot lets a maggot out
        spawnMaggots(level, this, 1);

        if (stage <= ZombieHorseDecay.STAGES) {
            this.entityData.set(DATA_DECAY_STAGE, stage);
            level.sendParticles(ParticleRegistry.UNDEAD_FLESH.get(), getX(), getY() + getBbHeight() * 0.6, getZ(), 12, 0.5, 0.3, 0.5, 0.0);

            return;
        }

        transformTo(skeleton, level);
    }

    /// Essence of Undead on a plain kind: it becomes a vanilla zombie horse (which then rots like any other)
    private void turnIntoZombieHorse(ServerLevel level) {
        if (hasChest())
            removeChest(level);

        ZombieHorse zombie = convertTo(EntityTypes.ZOMBIE_HORSE, ConversionParams.single(this, true, false), next -> HorseTransfer.copy(this, next, false));

        if (zombie != null) {
            level.sendParticles(ParticleRegistry.UNDEAD_FLESH.get(), zombie.getX(), zombie.getY() + 1.0, zombie.getZ(), 30, 0.6, 0.5, 0.6, 0.0);
            zombie.playSound(SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 1.0F, 0.8F);
        }
    }

    private void spawnMaggots(ServerLevel level) {
        spawnMaggots(level, this);
    }

    /// Two or three maggots crawl out of an undead horse
    public static void spawnMaggots(ServerLevel level, LivingEntity horse) {
        spawnMaggots(level, horse, 2 + horse.getRandom().nextInt(2));
    }

    public static void spawnMaggots(ServerLevel level, LivingEntity horse, int count) {
        for (int i = 0; i < count; i++) {
            MaggotEntity maggot = EntityRegistry.MAGGOT.get().create(level, EntitySpawnReason.MOB_SUMMONED);

            if (maggot != null) {
                maggot.snapTo(horse.getRandomX(0.6), horse.getY(), horse.getRandomZ(0.6), horse.getRandom().nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(maggot);
            }
        }
    }

    /// Maggots crawl out of a dead undead horse that still has flesh on it, not out of the bare skeletons.
    /// (The chance of a ghost is handled for all horses in LivingEntityMixin.)
    @Override
    public void die(DamageSource source) {
        super.die(source);

        if (kind().decaysInto() != null && level() instanceof ServerLevel serverLevel)
            spawnMaggots(serverLevel);
    }

    // ------------------------------------------------------------------------------------------------
    // Ghosts
    // ------------------------------------------------------------------------------------------------

    /// How high a ghost hovers above the ground, in blocks. It is only drawn up there (see MoHorseRenderer), so the
    /// rider is lifted by the same amount.
    public static final float GHOST_HOVER = 0.3F;

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        Vec3 point = super.getPassengerAttachmentPoint(passenger, dimensions, scale);

        return kind().isGhost() ? point.add(0.0, GHOST_HOVER * scale, 0.0) : point;
    }

    /// A ghost that just rose from its dead horse fades in over a few seconds
    public void startAppearing() {
        this.entityData.set(DATA_APPEAR_TICKS, APPEAR_DURATION);
    }

    /// How opaque a ghost is right now: it fades in after rising, then flickers between faint and fainter
    public float getGhostOpacity(float partialTick) {
        float appear = 1.0F - Mth.clamp((this.entityData.get(DATA_APPEAR_TICKS) - partialTick) / APPEAR_DURATION, 0.0F, 1.0F);

        return Mth.lerp(partialTick, this.ghostOpacityO, this.ghostOpacity) * appear;
    }

    /// Client side: every three seconds a new opacity is picked (as in Mo' Creatures, 0.3 to 0.6, a little brighter
    /// here), and the ghost drifts towards it instead of jumping
    private void tickGhostFlicker() {
        this.ghostOpacityO = this.ghostOpacity;

        if ((this.tickCount + getId()) % 60 == 0)
            this.ghostOpacityTarget = 0.4F + this.random.nextFloat() * 0.35F;

        this.ghostOpacity = approach(this.ghostOpacity, this.ghostOpacityTarget, 0.01F);

        if (this.random.nextInt(12) == 0)
            level().addParticle(ParticleTypes.WHITE_ASH, getRandomX(0.7), getRandomY(), getRandomZ(0.7), 0.0, 0.0, 0.0);
    }

    // ------------------------------------------------------------------------------------------------
    // Fairy Horse colours
    // ------------------------------------------------------------------------------------------------

    /// The colour a Fairy Horse was dyed, or null while it still has its natural white
    public @Nullable DyeColor getFairyColor() {
        int id = this.entityData.get(DATA_FAIRY_COLOR);

        return id < 0 ? null : DyeColor.byId(id);
    }

    public void setFairyColor(@Nullable DyeColor color) {
        this.entityData.set(DATA_FAIRY_COLOR, color == null ? -1 : color.getId());
    }

    private void vanish(ServerLevel level) {
        if (hasChest())
            removeChest(level);

        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.SADDLE, EquipmentSlot.BODY}) {
            ItemStack stack = getItemBySlot(slot);

            if (!stack.isEmpty()) {
                spawnAtLocation(level, stack);
                setItemSlot(slot, ItemStack.EMPTY);
            }
        }

        burst(level, ParticleTypes.END_ROD, 40);
        discard();
    }

    // ------------------------------------------------------------------------------------------------
    // Flying & special abilities
    // ------------------------------------------------------------------------------------------------

    /// Winged kinds in the air climb while the rider holds the jump key, and flap hard when it is released
    /// (the charge of the jump bar decides how strong that flap is).
    @Override
    protected void tickRidden(Player player, Vec3 travelVector) {
        // A whipped unicorn charges: at the creature that was lashed, or straight ahead (the rider still steers).
        // The rider's side moves a ridden horse, so the charge is driven here.
        if (isCharging() && onGround()) {
            Vec3 towards = getChargeTarget() instanceof LivingEntity target
                    ? target.position().subtract(position()).multiply(1.0, 0.0, 1.0)
                    : Vec3.directionFromRotation(0.0F, getYRot());

            if (towards.lengthSqr() > 0.25 || getChargeTarget() == null) {
                Vec3 run = towards.normalize().scale(0.55);
                Vec3 movement = getDeltaMovement();

                setDeltaMovement(run.x, movement.y, run.z);
            }
        }

        if (kind().canFly() && !onGround() && player.isJumping()) {
            Vec3 movement = getDeltaMovement();

            setDeltaMovement(movement.x, Math.min(movement.y + 0.12, 0.42), movement.z);
        }

        if (kind().canFly() && !onGround() && this.playerJumpPendingScale > 0.0F) {
            Vec3 movement = getDeltaMovement();
            double lift = 0.4 + 0.5 * this.playerJumpPendingScale;

            setDeltaMovement(movement.x, Math.max(movement.y, 0.0) * 0.5 + lift, movement.z);
            this.playerJumpPendingScale = 0.0F;
            this.needsSync = true;
        }

        super.tickRidden(player, travelVector);
    }

    /// In the air, a ridden winged horse steers almost as well as on the ground
    @Override
    protected float getFlyingSpeed() {
        return kind().canFly() && isVehicle() ? getSpeed() * 0.25F : super.getFlyingSpeed();
    }

    @Override
    public void aiStep() {
        super.aiStep();

        // Wings turn a fall into a glide
        if (kind().canFly() && !onGround() && !isInWater()) {
            Vec3 movement = getDeltaMovement();

            if (movement.y < -0.1)
                setDeltaMovement(movement.x, movement.y * 0.7, movement.z);
        }

        // Ghost horses without wings sink slowly, unicorns glide down gently
        if (kind().floats() && !onGround() && !isInWater()) {
            Vec3 movement = getDeltaMovement();

            if (movement.y < 0.0)
                setDeltaMovement(movement.x, movement.y * kind().fallSlowdown(), movement.z);
        }

        // A ridden horse is moved by its rider's client, so its own velocity says nothing on the server.
        // The distance covered since the last tick does.
        Vec3 position = position();
        double movedSqr = position.subtract(this.lastTickPosition).horizontalDistanceSqr();
        this.lastTickPosition = position;

        if (this.ramCooldown > 0)
            this.ramCooldown--;

        if (!(level() instanceof ServerLevel serverLevel))
            return;

        tickTransformation(serverLevel);
        tickDecay(serverLevel);

        int appearTicks = this.entityData.get(DATA_APPEAR_TICKS);

        if (appearTicks > 0)
            this.entityData.set(DATA_APPEAR_TICKS, appearTicks - 1);

        tickCharge();

        if (kind() == HorseKind.ZEBRA) {
            if ((this.tickCount + getId()) % 20 == 0)
                this.entityData.set(DATA_SHUFFLING, hearsShufflingSong(serverLevel));

            // A dancing zebra stays where it is
            if (isShuffling() && !isVehicle())
                getNavigation().stop();
        }

        // The horn only hurts while the unicorn charges (whipped), head down
        if (kind() == HorseKind.UNICORN && this.ramCooldown == 0 && isCharging())
            ramWithHorn(serverLevel);

        if (this.flameTicks > 0) {
            this.flameTicks--;

            if (isVehicle() && movedSqr >= FLAME_TRAIL_MIN_SPEED_SQR)
                leaveFlameTrail(serverLevel);
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Nightmare flame trail
    // ------------------------------------------------------------------------------------------------

    /// Redstone sets a Nightmare ablaze: for a while it is faster and leaves fire behind when ridden at speed
    private void ignite(ServerLevel level) {
        this.flameTicks = FLAME_DURATION;

        addEffect(new MobEffectInstance(MobEffects.SPEED, FLAME_DURATION, 1));
        burst(level, ParticleTypes.FLAME, 30);
        playSound(SoundEvents.FIRECHARGE_USE, 1.0F, 1.0F);
    }

    private void leaveFlameTrail(ServerLevel level) {
        Vec3 behind = position().subtract(getLookAngle().multiply(1, 0, 1).normalize().scale(1.5));
        BlockPos pos = BlockPos.containing(behind.x, getY(), behind.z);

        // Without mob griefing no fire blocks are placed, so the trail is drawn as a denser, purely visual blaze instead
        if (!level.getGameRules().get(GameRules.MOB_GRIEFING)) {
            level.sendParticles(ParticleTypes.FLAME, behind.x, getY() + 0.15, behind.z, 12, 0.4, 0.15, 0.4, 0.015);
            level.sendParticles(ParticleTypes.SMALL_FLAME, behind.x, getY() + 0.5, behind.z, 6, 0.35, 0.3, 0.35, 0.01);
            level.sendParticles(ParticleTypes.SMOKE, behind.x, getY() + 0.8, behind.z, 2, 0.3, 0.2, 0.3, 0.01);

            return;
        }

        level.sendParticles(ParticleTypes.FLAME, behind.x, getY() + 0.3, behind.z, 4, 0.3, 0.2, 0.3, 0.01);

        if (this.tickCount % 2 == 0
                && level.getBlockState(pos).isAir()
                && BaseFireBlock.canBePlacedAt(level, pos, getDirection())) {
            level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("FlameTicks", this.flameTicks);
        output.putInt("DecayStage", getDecayStage());
        output.putInt("DecayTicks", this.decayTicks);

        DyeColor fairyColor = getFairyColor();

        if (fairyColor != null)
            output.putString("FairyColor", fairyColor.getSerializedName());

        HorseKind target = getTransformTarget();

        if (isTransforming()) {
            output.putString("TransformTarget", target != null ? target.id() : "zombie_horse");
            output.putInt("TransformTicks", this.entityData.get(DATA_TRANSFORM_TICKS));
            output.putInt("MaggotsLeft", this.maggotsLeft);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.flameTicks = input.getIntOr("FlameTicks", 0);
        this.entityData.set(DATA_DECAY_STAGE, input.getIntOr("DecayStage", 0));
        this.decayTicks = input.getIntOr("DecayTicks", 0);

        String fairyColor = input.getStringOr("FairyColor", "");

        for (DyeColor color : DyeColor.values()) {
            if (color.getSerializedName().equals(fairyColor))
                setFairyColor(color);
        }

        String target = input.getStringOr("TransformTarget", "");

        if (target.equals("zombie_horse")) {
            this.entityData.set(DATA_TRANSFORM_TARGET, ZOMBIE_HORSE_TARGET);
            this.entityData.set(DATA_TRANSFORM_TICKS, Math.max(1, input.getIntOr("TransformTicks", TRANSFORM_DURATION)));
        }

        this.maggotsLeft = input.getIntOr("MaggotsLeft", 0);

        for (HorseKind kind : HorseKind.values()) {
            if (kind.id().equals(target)) {
                this.entityData.set(DATA_TRANSFORM_TARGET, kind.ordinal() + 1);
                this.entityData.set(DATA_TRANSFORM_TICKS, Math.max(1, input.getIntOr("TransformTicks", TRANSFORM_DURATION)));
            }
        }
    }

    /// A charging unicorn spears whatever is in its way
    private void ramWithHorn(ServerLevel level) {
        LivingEntity rider = getControllingPassenger();

        if (rider == null)
            return;

        Vec3 ahead = getLookAngle().multiply(1, 0, 1).normalize().scale(1.2);

        // While charging, the horn finds its target even if the rider looks elsewhere
        if (getChargeTarget() instanceof LivingEntity chargeTarget && distanceToSqr(chargeTarget) < 2.5 * 2.5)
            ahead = chargeTarget.position().subtract(position()).multiply(1, 0, 1).normalize().scale(1.2);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().move(ahead).inflate(0.3), entity -> entity != this && !entity.isPassengerOfSameVehicle(this) && entity.isAlive() && !entity.isAlliedTo(rider))) {
            if (target.hurtServer(level, damageSources().mobAttack(this), UNICORN_CHARGE_DAMAGE)) {
                target.push(ahead.x * 1.1, 0.55, ahead.z * 1.1);
                level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(), 15, 0.3, 0.3, 0.3, 0.2);
                level.sendParticles(ParticleRegistry.UNICORN_STAR.get(), target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
                this.ramCooldown = 10;

                // The lashed creature hit: that charge is done
                if (target == getChargeTarget())
                    stopCharge();
            }
        }
    }

    // ------------------------------------------------------------------------------------------------
    // Unicorn charge (whip)
    // ------------------------------------------------------------------------------------------------

    /// Whipped while ridden, a unicorn lowers its head and charges for two seconds, horn first: at the creature the
    /// whip hit, or straight ahead when the whip only cracked. Everything the horn meets meanwhile takes charge damage.
    public void startCharge(@Nullable LivingEntity target) {
        if (target == this || target == getControllingPassenger())
            return;

        this.entityData.set(DATA_CHARGE_TARGET, target != null ? target.getId() : -1);
        this.entityData.set(DATA_CHARGE_TICKS, CHARGE_DURATION);
        this.ramCooldown = 0;
        // An angry neigh, but no rearing: it charges off at once
        clearStanding();
        playSound(getAngrySound(), 1.0F, 1.0F);

        if (level() instanceof ServerLevel level)
            level.sendParticles(ParticleRegistry.UNICORN_STAR.get(), getX(), getY() + 1.6, getZ(), 12, 0.3, 0.3, 0.3, 0.05);
    }

    /// The creature this unicorn is charging at, or null
    public @Nullable Entity getChargeTarget() {
        int id = this.entityData.get(DATA_CHARGE_TARGET);

        return id < 0 ? null : level().getEntity(id);
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_CHARGE_TICKS) > 0;
    }

    private void stopCharge() {
        this.entityData.set(DATA_CHARGE_TARGET, -1);
        this.entityData.set(DATA_CHARGE_TICKS, 0);
    }

    /// Server side: the charge ends after two seconds or when the rider is off. A lashed creature that is gone or
    /// out of reach is let go, and the unicorn charges on straight ahead.
    private void tickCharge() {
        int ticks = this.entityData.get(DATA_CHARGE_TICKS);

        if (ticks <= 0)
            return;

        if (ticks == 1 || getControllingPassenger() == null) {
            stopCharge();

            return;
        }

        this.entityData.set(DATA_CHARGE_TICKS, ticks - 1);

        Entity target = getChargeTarget();

        if (this.entityData.get(DATA_CHARGE_TARGET) >= 0 && (!(target instanceof LivingEntity living) || !living.isAlive() || distanceToSqr(target) > 24 * 24))
            this.entityData.set(DATA_CHARGE_TARGET, -1);
    }

    // ------------------------------------------------------------------------------------------------
    // Shuffling (zebras dance to the Shuffling disc)
    // ------------------------------------------------------------------------------------------------

    public boolean isShuffling() {
        return this.entityData.get(DATA_SHUFFLING);
    }

    /// Whether a jukebox close by plays the Shuffling song
    private boolean hearsShufflingSong(ServerLevel level) {
        BlockPos here = blockPosition();

        for (int chunkX = (here.getX() - SHUFFLE_RANGE) >> 4; chunkX <= (here.getX() + SHUFFLE_RANGE) >> 4; chunkX++) {
            for (int chunkZ = (here.getZ() - SHUFFLE_RANGE) >> 4; chunkZ <= (here.getZ() + SHUFFLE_RANGE) >> 4; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);

                if (chunk == null)
                    continue;

                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof JukeboxBlockEntity jukebox
                            && jukebox.getBlockPos().closerThan(here, SHUFFLE_RANGE)
                            && jukebox.getSongPlayer().isPlaying()
                            && jukebox.getSongPlayer().getSong() != null
                            && jukebox.getSongPlayer().getSong().soundEvent().is(SoundRegistry.SHUFFLING_ID))
                        return true;
                }
            }
        }

        return false;
    }

    /// Winged horses are flying mounts, like the happy ghast: without this, a dedicated server that doesn't allow
    /// flight kicks the rider for "flying" after a few seconds in the air
    @Override
    public boolean isFlyingVehicle() {
        return kind().canFly();
    }

    public boolean isFlying() {
        return kind().canFly() && !onGround() && !isInWater() && !isPassenger();
    }

    /// Wild zebras can't be broken in by riding them: they throw off anyone who tries and are tamed with sugar lumps
    /// instead (see SugarLumpItem). Once tamed they are ridden like any horse.
    @Override
    protected void doPlayerRide(Player player) {
        if (kind() == HorseKind.ZEBRA && !isTamed()) {
            makeMad();

            return;
        }

        super.doPlayerRide(player);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return (kind() != HorseKind.ZEBRA || isTamed()) && super.canAddPassenger(passenger);
    }

    /// Like on vanilla zombie horses, horse armor shields the undead from the sun (and wears down doing it)
    @Override
    protected EquipmentSlot sunProtectionSlot() {
        return EquipmentSlot.BODY;
    }

    /// Winged horses don't rear up in mid-air when the rider starts charging a jump (that is a flap up there)
    @Override
    public void standIfPossible() {
        if (kind().canFly() && !onGround())
            return;

        super.standIfPossible();
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            tickWings();
            tickTransformationParticles();

            if (kind().decaysInto() != null)
                ZombieHorseDecay.spawnFleshParticles(this, getDecayStage());

            if (kind().isGhost())
                tickGhostFlicker();

            // A shuffling zebra lets music notes fly
            if (isShuffling() && this.random.nextInt(8) == 0)
                level().addParticle(ParticleTypes.NOTE, getRandomX(0.6), getY() + getBbHeight() + 0.2, getRandomZ(0.6), this.random.nextInt(25) / 24.0, 0.0, 0.0);

            // A unicorn jumping or gliding leaves a trail of stars (not when it just steps up a block)
            this.airTicks = onGround() || isInWater() || isPassenger() ? 0 : this.airTicks + 1;

            if (kind() == HorseKind.UNICORN && this.airTicks > 4) {
                for (int i = 0; i < 2; i++)
                    level().addParticle(ParticleRegistry.UNICORN_STAR.get(), getRandomX(0.8), getY() + this.random.nextDouble() * getBbHeight() * 0.7, getRandomZ(0.8), 0.0, 0.0, 0.0);
            }

            if (kind() == HorseKind.UNICORN && isCharging())
                spawnChargeTrail();
        }
    }

    /// Client-side only: a dashing unicorn leaves stars behind it and kicks up dust at its hooves
    private void spawnChargeTrail() {
        Vec3 back = Vec3.directionFromRotation(0.0F, this.yBodyRot).scale(-0.9);

        for (int i = 0; i < 3; i++) {
            level().addParticle(ParticleRegistry.UNICORN_STAR.get(),
                    getX() + back.x + (this.random.nextDouble() - 0.5) * 0.6,
                    getY() + 0.4 + this.random.nextDouble() * getBbHeight() * 0.6,
                    getZ() + back.z + (this.random.nextDouble() - 0.5) * 0.6,
                    back.x * 0.05, 0.02, back.z * 0.05);
        }

        if (onGround() && this.random.nextInt(2) == 0)
            level().addParticle(ParticleTypes.POOF, getRandomX(0.6), getY() + 0.1, getRandomZ(0.6), back.x * 0.04, 0.02, back.z * 0.04);
    }

    /// Client-side only: eases the wings open and shut and the wingbeat stronger and weaker, instead of snapping
    private void tickWings() {
        this.wingSpreadO = this.wingSpread;
        this.wingBeatO = this.wingBeat;

        boolean flying = isFlying();
        boolean climbing = flying && getDeltaMovement().y > 0.0;

        this.wingSpread = approach(this.wingSpread, flying ? 1.0F : 0.0F, 0.12F);
        this.wingBeat = approach(this.wingBeat, climbing ? 1.0F : 0.0F, 0.1F);
    }

    private static float approach(float value, float target, float step) {
        return value < target ? Math.min(target, value + step) : Math.max(target, value - step);
    }

    /// How far the wings are spread, from 0 (folded on the flanks) to 1 (fully out)
    public float getWingSpread(float partialTick) {
        return Mth.lerp(partialTick, this.wingSpreadO, this.wingSpread);
    }

    /// How hard the wings beat, from 0 (gliding) to 1 (climbing)
    public float getWingBeat(float partialTick) {
        return Mth.lerp(partialTick, this.wingBeatO, this.wingBeat);
    }

    // ------------------------------------------------------------------------------------------------
    // Sounds
    // ------------------------------------------------------------------------------------------------

    @Override
    protected void playGallopSound(SoundType soundType) {
        super.playGallopSound(soundType);

        if (this.random.nextInt(10) == 0)
            playSound(SoundEvents.HORSE_BREATHE, soundType.getVolume() * 0.6F, soundType.getPitch());
    }

    /// Which voice a kind has: zebras and ghosts their Mo' Creatures sounds, the undead the vanilla zombie and
    /// skeleton horse sounds, and all the others the vanilla horse or donkey
    private enum Voice { HORSE, DONKEY, ZEBRA, ZOMBIE, SKELETON, GHOST }

    private Voice voice() {
        HorseKind kind = kind();

        if (kind.isGhost())
            return Voice.GHOST;

        if (kind.decaysInto() != null)
            return Voice.ZOMBIE;

        if (kind.isUndead())
            return Voice.SKELETON;

        if (kind == HorseKind.ZEBRA)
            return Voice.ZEBRA;

        return kind.donkeyLike() ? Voice.DONKEY : Voice.HORSE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (voice()) {
            case DONKEY -> SoundEvents.DONKEY_AMBIENT;
            case ZEBRA -> SoundRegistry.ZEBRA_AMBIENT.get();
            case ZOMBIE -> SoundEvents.ZOMBIE_HORSE_AMBIENT;
            case SKELETON -> SoundEvents.SKELETON_HORSE_AMBIENT;
            case GHOST -> SoundRegistry.GHOST_HORSE_AMBIENT.get();
            default -> SoundEvents.HORSE_AMBIENT;
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        return switch (voice()) {
            case DONKEY -> SoundEvents.DONKEY_DEATH;
            case ZEBRA -> SoundRegistry.ZEBRA_DEATH.get();
            case ZOMBIE -> SoundEvents.ZOMBIE_HORSE_DEATH;
            case SKELETON -> SoundEvents.SKELETON_HORSE_DEATH;
            case GHOST -> SoundRegistry.GHOST_HORSE_DEATH.get();
            default -> SoundEvents.HORSE_DEATH;
        };
    }

    @Override
    protected @Nullable SoundEvent getEatingSound() {
        return kind().donkeyLike() ? SoundEvents.DONKEY_EAT : SoundEvents.HORSE_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return switch (voice()) {
            case DONKEY -> SoundEvents.DONKEY_HURT;
            case ZEBRA -> SoundRegistry.ZEBRA_HURT.get();
            case ZOMBIE -> SoundEvents.ZOMBIE_HORSE_HURT;
            case SKELETON -> SoundEvents.SKELETON_HORSE_HURT;
            case GHOST -> SoundRegistry.GHOST_HORSE_HURT.get();
            default -> SoundEvents.HORSE_HURT;
        };
    }

    @Override
    protected SoundEvent getAngrySound() {
        return switch (voice()) {
            case DONKEY -> SoundEvents.DONKEY_ANGRY;
            case ZEBRA -> SoundRegistry.ZEBRA_ANGRY.get();
            case ZOMBIE -> SoundEvents.ZOMBIE_HORSE_ANGRY;
            case GHOST -> SoundRegistry.GHOST_HORSE_ANGRY.get();
            default -> SoundEvents.HORSE_ANGRY;
        };
    }
}
