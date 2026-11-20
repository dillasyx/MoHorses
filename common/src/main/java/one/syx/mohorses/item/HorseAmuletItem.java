package one.syx.mohorses.item;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import one.syx.mohorses.ModConstants;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueOutput;
import one.syx.mohorses.entity.HorseKind;
import one.syx.mohorses.entity.MoHorse;
import one.syx.mohorses.registry.ItemRegistry;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

/// An amulet that holds a horse, like in Mo' Creatures.
///
/// Use an empty amulet on your own tamed horse of the right kind to put it in, saddle, armor and chest contents
/// included. Use the full amulet on a block to let the horse out again. Which amulet fits which horse is decided by
/// [HorseKind#amulet()]; undead tamed animals (zombie and skeleton horses, zombie nautiluses) go in the
/// Bone Amulet. The Pet Amulet also holds any other tamed animal of yours (wolves, cats, parrots, vanilla horses, ...).
/// Animals without a registered owner (vanilla camels and camel husks never get one) fit in none.
public class HorseAmuletItem extends Item {
    private static final Logger LOGGER = LogUtils.getLogger();

    /// Entity types no amulet takes, for modpacks: tamed animals of other mods that don't survive being stored (for
    /// example because other things refer to their UUID, which a released animal gets new)
    public static final TagKey<EntityType<?>> EXCLUDED = TagKey.create(Registries.ENTITY_TYPE, ModConstants.id("amulet_excluded"));

    private final HorseKind.Amulet type;

    public HorseAmuletItem(Properties properties, HorseKind.Amulet type) {
        super(properties);

        this.type = type;
    }

    public static boolean isFull(ItemStack stack) {
        return stack.has(DataComponents.ENTITY_DATA);
    }

    /// Amulets filled by 1.0.2 and older carry a "Holds a ..." lore line; the tooltip shows that line itself now
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        removeOldHoldsLine(stack);
    }

    private static void removeOldHoldsLine(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);

        if (lore != null && lore.lines().size() == 1 && lore.lines().getFirst().getContents() instanceof TranslatableContents line
                && line.getKey().startsWith("item.mohorses.amulet.holds"))
            stack.set(DataComponents.LORE, ItemLore.EMPTY);
    }

    /// A full amulet shows what it holds, and the animal itself (see AmuletPreviewTooltip)
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.ENTITY_DATA)).map(AmuletTooltip::new);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (isFull(stack) || amuletFor(target) != this.type || !belongsTo(target, player))
            return InteractionResult.PASS;

        // Only grown animals go in, vanilla ones too
        if (target.isBaby() || target instanceof MoHorse horse && horse.isTransforming())
            return InteractionResult.PASS;

        if (!(player.level() instanceof ServerLevel level))
            return InteractionResult.SUCCESS;

        ItemStack full = capture(target, level);

        if (full.isEmpty())
            return InteractionResult.PASS;

        if (stack.getCount() == 1) {
            player.setItemInHand(hand, full);
        }
        else {
            stack.shrink(1);
            player.getInventory().placeItemBackInInventory(full, Prediction.SERVER_ONLY);
        }

        return InteractionResult.SUCCESS;
    }

    /// Which amulet the entity fits in, or null if none
    private static HorseKind.@Nullable Amulet amuletFor(LivingEntity entity) {
        if (entity.is(EXCLUDED))
            return null;

        if (entity instanceof MoHorse horse)
            return horse.kind().amulet();

        // Undead tamed animals go in the Bone Amulet: zombie and skeleton horses, zombie nautiluses
        // (and those of other mods in the vanilla undead tag)
        if (entity.is(EntityTypeTags.UNDEAD) && entity instanceof OwnableEntity)
            return HorseKind.Amulet.BONE;

        return entity instanceof OwnableEntity ? HorseKind.Amulet.PET : null;
    }

    /// Whether the entity is tamed and the player is its registered owner. An animal without an owner goes in nobody's
    /// amulet, so on a server nobody can pack up another player's (or a wandering trader's) animal.
    private static boolean belongsTo(LivingEntity entity, Player player) {
        if (entity instanceof AbstractHorse horse && !horse.isTamed())
            return false;

        if (entity instanceof TamableAnimal animal && !animal.isTame())
            return false;

        if (!(entity instanceof OwnableEntity ownable))
            return false;

        EntityReference<LivingEntity> owner = ownable.getOwnerReference();

        return owner != null && owner.getUUID().equals(player.getUUID());
    }

    /// The owner's name as it was when the animal went in, for the tooltip (the client only knows the names of
    /// players who are online)
    public static final String OWNER_NAME_KEY = "MoHorsesOwnerName";

    /// Puts the animal into a fitting amulet and removes it from the world. Returns the full amulet,
    /// or an empty stack if it fits in no amulet.
    public static ItemStack capture(LivingEntity horse, ServerLevel level) {
        HorseKind.Amulet type = amuletFor(horse);

        if (type == null)
            return ItemStack.EMPTY;

        Item amulet = switch (type) {
            case PEGASUS -> ItemRegistry.PEGASUS_AMULET.get();
            case FAIRY -> ItemRegistry.FAIRY_AMULET.get();
            case PET -> ItemRegistry.PET_AMULET.get();
            case BONE -> ItemRegistry.BONE_AMULET.get();
            case GHOST -> ItemRegistry.GHOST_AMULET.get();
        };
        ItemStack stack = new ItemStack(amulet);

        horse.ejectPassengers();

        if (horse instanceof Leashable leashable && leashable.isLeashed())
            leashable.dropLeash();

        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(horse.problemPath(), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());

            horse.saveWithoutId(output);

            CompoundTag data = output.buildResult();

            // A released horse is a new entity: it gets a fresh UUID and the position it is let out at
            data.remove("UUID");
            data.remove("Pos");
            data.remove("Motion");

            if (horse instanceof OwnableEntity ownable && ownable.getOwner() instanceof Player owner)
                data.putString(OWNER_NAME_KEY, owner.getGameProfile().name());

            stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(horse.getType(), data));
        }

        sparkle(level, horse, type);
        level.playSound(null, horse.getX(), horse.getY(), horse.getZ(), SoundEvents.ENDER_EYE_DEATH, SoundSource.NEUTRAL, 1.0F, 1.2F);
        horse.discard();

        return stack;
    }

    /// A puff of sparkles in the amulet's colour: dark blue for the Pet Amulet, light gold for the Pegasus Amulet,
    /// purple for the Fairy Amulet, white for the Bone Amulet and a pale, see-through mist for the Ghost Amulet
    private static void sparkle(ServerLevel level, Entity horse, HorseKind.Amulet amulet) {
        double x = horse.getX(), y = horse.getY() + 1.0, z = horse.getZ();

        if (amulet == HorseKind.Amulet.GHOST) {
            level.sendParticles(ParticleTypes.WHITE_SMOKE, x, y, z, 40, 0.7, 0.7, 0.7, 0.01);
            level.sendParticles(new DustParticleOptions(0xE8F0FF, 1.0F), x, y, z, 15, 0.7, 0.7, 0.7, 0.0);

            return;
        }

        int color = switch (amulet) {
            case PET -> 0x1E2F8C;
            case PEGASUS -> 0xF2D27A;
            case FAIRY -> 0xA05AD8;
            default -> 0xFFFFFF;
        };

        level.sendParticles(new DustParticleOptions(color, 1.6F), x, y, z, 45, 0.7, 0.7, 0.7, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, x, y, z, 8, 0.5, 0.5, 0.5, 0.04);
    }

    /// Lets the horse out on the clicked block
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        TypedEntityData<EntityType<?>> data = stack.get(DataComponents.ENTITY_DATA);

        if (data == null)
            return InteractionResult.PASS;

        Level level = context.getLevel();

        if (!(level instanceof ServerLevel serverLevel))
            return InteractionResult.SUCCESS;

        BlockPos clicked = context.getClickedPos();
        BlockPos pos = level.getBlockState(clicked).getCollisionShape(level, clicked).isEmpty() ? clicked : clicked.relative(context.getClickedFace());
        Player player = context.getPlayer();
        float yaw = player != null ? player.getYRot() + 180.0F : 0.0F;

        Entity horse = EntityType.loadEntityRecursive(data.type(), data.copyTagWithoutId(), serverLevel, EntitySpawnReason.SPAWN_ITEM_USE, entity -> {
            entity.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0.0F);

            return entity;
        });

        if (horse == null || !serverLevel.addFreshEntity(horse))
            return InteractionResult.FAIL;

        if (horse instanceof MoHorse moHorse && moHorse.getOwner() == null && player != null)
            moHorse.tameWithName(player);

        stack.remove(DataComponents.ENTITY_DATA);
        removeOldHoldsLine(stack);

        sparkle(serverLevel, horse, this.type);
        level.playSound(null, horse.getX(), horse.getY(), horse.getZ(), SoundEvents.ENDER_EYE_DEATH, SoundSource.NEUTRAL, 1.0F, 0.8F);

        return InteractionResult.SUCCESS;
    }
}
