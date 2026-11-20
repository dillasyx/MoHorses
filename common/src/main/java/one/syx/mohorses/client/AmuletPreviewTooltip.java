package one.syx.mohorses.client;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.component.TypedEntityData;
import one.syx.mohorses.entity.MoHorse;
import one.syx.mohorses.item.AmuletTooltip;
import one.syx.mohorses.item.HorseAmuletItem;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

/// The tooltip of a full amulet: what it holds ("Holds Rex (Wolf)") and whose it is, then the animal as a slowly turning model
/// (saddle, armor, chest, colours and all, as they are part of the stored data). When the model shows is up to the
/// `amuletPreview` setting in [ClientConfig]: by default while Alt is held, with a hint line otherwise.
public final class AmuletPreviewTooltip implements ClientTooltipComponent {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int BOX_WIDTH = 96;
    private static final int BOX_HEIGHT = 80;
    private static final int LINE_HEIGHT = 10;
    /// Space around the model: 2 above, 2 below
    private static final int BOX_PADDING = 4;

    /// The preview animals, made once per stored animal (a tooltip is rebuilt every frame). Null marks one that failed.
    private static final Map<TypedEntityData<EntityType<?>>, @Nullable LivingEntity> CACHE = new LinkedHashMap<>(16, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<TypedEntityData<EntityType<?>>, @Nullable LivingEntity> eldest) {
            return size() > 16;
        }
    };
    private static @Nullable ClientLevel cacheLevel;
    /// Ids for the preview animals, below anything a level hands out
    private static int nextPreviewId = -1000000;

    private final @Nullable LivingEntity animal;
    /// "Holds a Wolf", or "Holds Rex (Wolf)" for an animal with a name tag name
    private final Component holds;
    /// "Owner: Steve", or null when the owner isn't known
    private final @Nullable Component owner;

    public AmuletPreviewTooltip(AmuletTooltip tooltip) {
        this.animal = previewOf(tooltip.data());

        Component kind = tooltip.data().type().getDescription();
        Component holds = this.animal != null && this.animal.hasCustomName()
                ? Component.translatable("item.mohorses.amulet.holds_named", this.animal.getCustomName(), kind)
                : Component.translatable("item.mohorses.amulet.holds", kind);

        this.holds = holds.copy().withStyle(ChatFormatting.GRAY);

        String ownerName = ownerName(tooltip.data());

        this.owner = ownerName != null ? Component.translatable("item.mohorses.amulet.owner", ownerName).withStyle(ChatFormatting.DARK_GRAY) : null;
    }

    /// The owner's current name while they are online, otherwise the name they had when the animal went in
    private static @Nullable String ownerName(TypedEntityData<EntityType<?>> data) {
        CompoundTag tag = data.getUnsafe();
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        PlayerInfo online = connection == null ? null : tag.read("Owner", UUIDUtil.CODEC).map(connection::getPlayerInfo).orElse(null);

        if (online != null)
            return online.getProfile().name();

        return tag.getString(HorseAmuletItem.OWNER_NAME_KEY).orElse(null);
    }

    private int textLines() {
        return this.owner != null ? 2 : 1;
    }

    /// Whether the model is shown right now (by the setting, and with "alt" while Alt is held)
    private boolean showsModel() {
        return this.animal != null && switch (ClientConfig.amuletPreview()) {
            case ALWAYS -> true;
            case ALT -> Minecraft.getInstance().hasAltDown();
            case NEVER -> false;
        };
    }

    /// Whether the "hold Alt" hint is shown instead of the model
    private boolean showsHint() {
        return this.animal != null && ClientConfig.amuletPreview() == ClientConfig.AmuletPreview.ALT && !showsModel();
    }

    @Override
    public int getHeight(Font font) {
        return textLines() * LINE_HEIGHT + (showsModel() ? BOX_HEIGHT + BOX_PADDING : showsHint() ? LINE_HEIGHT : 0);
    }

    @Override
    public int getWidth(Font font) {
        int width = font.width(this.holds);

        if (this.owner != null)
            width = Math.max(width, font.width(this.owner));

        if (showsModel())
            width = Math.max(width, BOX_WIDTH);
        else if (showsHint())
            width = Math.max(width, font.width(hint()));

        return width;
    }

    @Override
    public boolean showTooltipWithItemInHand() {
        return true;
    }

    private static Component hint() {
        return Component.translatable("item.mohorses.amulet.preview_hint").withStyle(ChatFormatting.DARK_GRAY);
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        graphics.text(font, this.holds, x, y, 0xFFFFFFFF);
        y += LINE_HEIGHT;

        if (this.owner != null) {
            graphics.text(font, this.owner, x, y, 0xFFFFFFFF);
            y += LINE_HEIGHT;
        }

        if (showsHint())
            graphics.text(font, hint(), x, y, 0xFFFFFFFF);

        if (showsModel() && this.animal != null) {
            int x0 = x + (width - BOX_WIDTH) / 2;
            y += 2;

            try {
                drawAnimal(graphics, this.animal, x0, y, x0 + BOX_WIDTH, y + BOX_HEIGHT);
            }
            catch (Throwable e) {
                // A renderer that can't draw this animal outside the world: leave the box empty rather than crash
                LOGGER.debug("Amulet preview failed for {}", this.animal.getType(), e);
            }
        }
    }

    /// Draws the animal turning slowly, seen a little from above, scaled to fit the box
    private static void drawAnimal(GuiGraphicsExtractor graphics, LivingEntity animal, int x0, int y0, int x1, int y1) {
        EntityRenderState state = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(animal).createRenderState(animal, 1.0F);

        state.shadowPieces.clear();
        state.outlineColor = 0;
        state.nameTag = null;

        float spin = (Util.getMillis() % 9000L) / 9000.0F * 360.0F;

        if (state instanceof LivingEntityRenderState living) {
            living.bodyRot = 180.0F + spin;
            living.yRot = 0.0F;
            living.xRot = 0.0F;
            living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
            living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
            living.scale = 1.0F;
        }

        // Heads (raised, with horns or ears) reach well above the bounding box, raised wings even further, and a horse
        // is longer than its box is wide: leave room for all of it, the free space above the back
        boolean winged = animal instanceof MoHorse horse && horse.kind().canFly();
        float size = winged
                ? Math.max(state.boundingBoxHeight * 2.3F, state.boundingBoxWidth * 2.0F)
                : Math.max(state.boundingBoxHeight * 1.8F, state.boundingBoxWidth * 1.9F);
        float scale = Math.min((y1 - y0) / size, (x1 - x0) / size);

        Quaternionf tilt = new Quaternionf().rotateX((float)Math.toRadians(-12.0));
        Quaternionf rotation = new Quaternionf().rotateZ((float)Math.PI).mul(tilt);
        Vector3f translation = new Vector3f(0.0F, state.boundingBoxHeight * (winged ? 0.85F : 0.72F), 0.0F);

        graphics.entity(state, scale, translation, rotation, tilt, x0, y0, x1, y1);
    }

    /// The preview animal for the stored data, made from it on the client's level and kept for later frames
    private static @Nullable LivingEntity previewOf(TypedEntityData<EntityType<?>> data) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level == null)
            return null;

        if (level != cacheLevel) {
            CACHE.clear();
            cacheLevel = level;
        }

        if (CACHE.containsKey(data))
            return CACHE.get(data);

        LivingEntity animal = null;

        try {
            Entity entity = data.type().create(level, EntitySpawnReason.LOAD);

            if (entity instanceof LivingEntity living) {
                data.loadInto(living);
                // Never added to the level, so it gets no id there; the renderers still ask for one
                living.setId(nextPreviewId--);
                animal = living;
            }
        }
        catch (Throwable e) {
            // An animal of another mod that can't be made outside the world: no preview for it
            LOGGER.debug("No amulet preview for {}", data.type(), e);
        }

        CACHE.put(data, animal);

        return animal;
    }
}
