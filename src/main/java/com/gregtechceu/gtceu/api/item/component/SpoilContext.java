package com.gregtechceu.gtceu.api.item.component;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.utils.EntitySlotsInvWrapper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This class represents the environment in which an item spoils, for example,
 * the level and the block's position, or the entity. It also may include a way to get an
 * {@link IItemHandler} (an instance of {@link ItemHandlerSource}) in which the item spoiled, and the slot number of the
 * item.
 * This info is used to, for example, spawn an entity when an item spoils.
 */
@With
public record SpoilContext(@Nullable Level level,
                           @Nullable BlockPos pos,
                           @Nullable Entity entity,
                           @Nullable ItemHandlerSource itemHandlerSource,
                           @Nullable CompoundTag itemHandlerData,
                           int slot) {

    // spotless:off
    public static final Codec<SpoilContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Level.RESOURCE_KEY_CODEC.optionalFieldOf("dimension").forGetter(ctx -> ctx.level == null ? Optional.empty() : Optional.of(ctx.level.dimension())),
            BlockPos.CODEC.optionalFieldOf("pos").forGetter(ctx -> Optional.ofNullable(ctx.pos)),
            UUIDUtil.CODEC.optionalFieldOf("entity").forGetter(ctx -> ctx.entity == null ? Optional.empty() : Optional.of(ctx.entity.getUUID())),
            ResourceLocation.CODEC.optionalFieldOf("item_handler").forGetter(ctx -> ctx.itemHandlerSource == null ? Optional.empty() : Optional.of(ctx.itemHandlerSource.getId())),
            CompoundTag.CODEC.optionalFieldOf("item_handler_data").forGetter(ctx -> Optional.ofNullable(ctx.itemHandlerData)),
            Codec.INT.optionalFieldOf("slot", -1).forGetter(SpoilContext::slot)
    ).apply(instance, SpoilContext::build));
    // spotless:on

    public static final StreamCodec<ByteBuf, SpoilContext> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG
            .map(SpoilContext::deserializeNBT, SpoilContext::serializeNBT);

    /**
     * @return the {@link Level} used to determine time to calculate spoilage progress (using
     *         {@link Level#getGameTime()}).
     *         This is usually the Overworld. If it is {@code null}, all spoilage updates are ignored.
     */
    public static @Nullable Level getDefaultLevel() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        return server.overworld();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static SpoilContext build(Optional<ResourceKey<Level>> dimension, Optional<BlockPos> pos,
                                      Optional<UUID> entityId,
                                      Optional<ResourceLocation> itemHandlerSourceId,
                                      Optional<CompoundTag> itemHandlerData, int slot) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerLevel level = server == null || dimension.isEmpty() ? null : server.getLevel(dimension.get());
        return new SpoilContext(level, pos.orElse(null),
                level != null && entityId.isPresent() ? level.getEntities().get(entityId.get()) : null,
                itemHandlerSourceId.map(ItemHandlerSource::getById).orElse(null), itemHandlerData.orElse(null), slot);
    }

    public SpoilContext() {
        this((Level) null);
    }

    public SpoilContext(@Nullable Level level) {
        this(level, null);
    }

    public SpoilContext(@Nullable Level level, @Nullable BlockPos pos) {
        this(level, pos, null, null, null, -1);
    }

    public SpoilContext(@NotNull Entity entity, int slot) {
        this(entity.level(), entity.blockPosition(), entity, ItemHandlerSource.ENTITY_INVENTORY, null, slot);
    }

    public SpoilContext(@NotNull Player player, int slot) {
        this(player.level(), player.blockPosition(), player, ItemHandlerSource.PLAYER_INVENTORY, null, slot);
    }

    public SpoilContext(@NotNull MetaMachine machine) {
        this(machine.getLevel(), machine.getBlockPos(), null, null, null, -1);
    }

    public boolean isEmpty() {
        return level == null;
    }

    public @Nullable IItemHandler itemHandler() {
        if (itemHandlerSource == null) return null;
        return itemHandlerSource.getHandler(this);
    }

    public SpoilContext withItemHandlerData(String key, Tag value) {
        CompoundTag tag = itemHandlerData == null ? new CompoundTag() : itemHandlerData.copy();
        tag.put(key, value);
        return this.withItemHandlerData(tag);
    }

    public SpoilContext withItemHandlerSide(Direction side) {
        SpoilContext self = this.withItemHandlerSource(ItemHandlerSource.BLOCK_CAPABILITY);
        if (side != null) {
            self = self.withItemHandlerData("side", StringTag.valueOf(side.getSerializedName()));
        }
        return self;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (level != null) tag.putString("level", level.dimension().location().toString());
        if (pos != null) tag.putLong("pos", pos.asLong());
        if (entity != null) tag.putInt("entity", entity.getId());
        if (slot != -1) tag.putInt("slot", slot);
        if (itemHandlerSource != null) tag.putString("handlerSource", itemHandlerSource.getId().toString());
        if (itemHandlerData != null) tag.put("handlerData", itemHandlerData);
        return tag;
    }

    public static SpoilContext deserializeNBT(CompoundTag tag) {
        SpoilContext ctx = new SpoilContext();
        if (tag.contains("level")) {
            ctx = ctx.withLevel(ServerLifecycleHooks.getCurrentServer().getLevel(ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.parse(tag.getString("level")))));
        }
        if (tag.contains("pos")) {
            ctx = ctx.withPos(BlockPos.of(tag.getLong("pos")));
        }
        if (tag.contains("entity") && ctx.level != null) {
            ctx = ctx.withEntity(ctx.level.getEntity(tag.getInt("entity")));
        }
        if (tag.contains("slot")) {
            ctx = ctx.withSlot(tag.getInt("slot"));
        }
        if (tag.contains("handlerSource")) {
            ctx = ctx.withItemHandlerSource(
                    ItemHandlerSource.getById(ResourceLocation.parse(tag.getString("handlerSource"))));
        }
        if (tag.contains("handlerData")) {
            ctx = ctx.withItemHandlerData(tag.getCompound("handlerData"));
        }
        return ctx;
    }

    /**
     * This class represents a way to get an {@link IItemHandler} from a {@link SpoilContext}, optionally
     * using {@link SpoilContext#itemHandlerData}. This is used instead of a normal supplier, due to the fact that
     * it is serializable. Note that new instances of this class should not be created dynamically.
     * <br>
     * This class is basically equivalent to a serializable {@code Function<SpoilContext, IItemHandler>}.
     */
    public static abstract class ItemHandlerSource {

        private static final Map<ResourceLocation, ItemHandlerSource> HANDLER_SOURCES = new HashMap<>();

        /**
         * Represents getting an item handler as a capability of a block, with an optional "side" key in
         * {@link SpoilContext#itemHandlerData}
         */
        public static final ItemHandlerSource BLOCK_CAPABILITY = new ItemHandlerSource(GTCEu.id("block_capability")) {

            @Override
            protected @Nullable IItemHandler getHandler(SpoilContext ctx) {
                if (ctx.level() == null || ctx.pos() == null) return null;
                CompoundTag tag = ctx.itemHandlerData();
                if (tag != null && tag.contains("side", Tag.TAG_STRING)) {
                    return ctx.level().getCapability(Capabilities.ItemHandler.BLOCK, ctx.pos(),
                            Direction.byName(tag.getString("side")));
                } else {
                    return ctx.level().getCapability(Capabilities.ItemHandler.BLOCK, ctx.pos(), null);
                }
            }
        };

        /**
         * Represents getting an item handler as a player's inventory (used if {@link SpoilContext#entity} is a
         * {@link Player})
         */
        public static final ItemHandlerSource PLAYER_INVENTORY = new ItemHandlerSource(GTCEu.id("player_inventory")) {

            @Override
            protected @Nullable IItemHandler getHandler(SpoilContext ctx) {
                if (ctx.entity instanceof Player player) {
                    return new CustomItemStackHandler(player.getInventory().items);
                } else return null;
            }
        };

        public static final ItemHandlerSource ENTITY_INVENTORY = new ItemHandlerSource(GTCEu.id("entity_inventory")) {

            @Override
            protected @Nullable IItemHandler getHandler(SpoilContext ctx) {
                if (ctx.entity != null) {
                    IItemHandler handler = ctx.entity.getCapability(Capabilities.ItemHandler.ENTITY);
                    if (handler != null) return handler;
                    return new EntitySlotsInvWrapper(ctx.entity);
                }
                return null;
            }
        };

        private static ItemHandlerSource getById(ResourceLocation id) {
            return HANDLER_SOURCES.get(id);
        }

        private final ResourceLocation id;

        public ItemHandlerSource(ResourceLocation id) {
            this.id = id;
            HANDLER_SOURCES.put(id, this);
        }

        private ResourceLocation getId() {
            return id;
        }

        @Override
        public String toString() {
            return id.toString();
        }

        abstract protected @Nullable IItemHandler getHandler(SpoilContext ctx);
    }
}
