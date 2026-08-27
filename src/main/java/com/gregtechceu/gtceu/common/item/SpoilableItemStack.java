package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.IMergeableDataComponent;
import com.gregtechceu.gtceu.api.item.ISpoilableItemStackExtension;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.api.item.spoilage.ISpoilableItem;
import com.gregtechceu.gtceu.api.item.spoilage.ItemSpoilBehaviour;
import com.gregtechceu.gtceu.api.item.spoilage.SpoilContext;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import lombok.Getter;
import lombok.With;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This class is a basic implementation of the {@link ISpoilableItem} capability.
 * It leaves some methods unimplemented, such as {@link ISpoilableItem#getSpoilTicks()} and
 * {@link ISpoilableItem#spoilResult(SpoilContext, boolean)}.
 *
 * @implNote this class uses a mixin in its {@link ISpoilableItem#updateFreshness} implementation
 *
 * @see ItemSpoilBehaviour
 */
public abstract class SpoilableItemStack implements ISpoilableItem, IAddInformation, IDurabilityBar {

    @With
    public record SpoilableData(
                                boolean initialized,
                                boolean frozen,
                                long frozenTicks,
                                long creationTick,
                                SpoilContext spoilContext)
            implements IMergeableDataComponent {

        public static final Codec<SpoilableData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("frozen").forGetter(SpoilableData::frozen),
                Codec.LONG.fieldOf("frozen_ticks").forGetter(SpoilableData::frozenTicks),
                Codec.LONG.fieldOf("creation_tick").forGetter(SpoilableData::creationTick),
                SpoilContext.CODEC.fieldOf("spoil_context").forGetter(SpoilableData::spoilContext))
                .apply(instance, SpoilableData::new));

        public static final StreamCodec<ByteBuf, SpoilableData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, SpoilableData::frozen,
                ByteBufCodecs.VAR_LONG, SpoilableData::frozenTicks,
                ByteBufCodecs.VAR_LONG, SpoilableData::creationTick,
                SpoilContext.STREAM_CODEC, SpoilableData::spoilContext,
                SpoilableData::new);

        public static final SpoilableData EMPTY = new SpoilableData(false, 0, -1, new SpoilContext());

        public SpoilableData(boolean frozen, long frozenTicks, long creationTick, SpoilContext spoilContext) {
            this(creationTick != -1, frozen, frozenTicks, creationTick, spoilContext);
        }

        /**
         * This method averages the spoil progress of the two stacks (or, more
         * accurately, their {@link ISpoilableItem#getCreationTick()}). If {@link SpoilableItemStack#FROZEN_EQUALITY}
         * is {@code true}, this method will ignore the frozen/not frozen status of stacks.
         *
         * @implNote This implementation may lead to spoil progress averaging in situations other
         *           than stack merging, though I don't think this will lead to any big user-facing bugs.
         */
        @Override
        public void prepareForComparisonWith(ItemStack stack, ItemStack other) {
            SpoilableData spoilable = other.get(GTDataComponents.SPOILABLE);
            if (spoilable == null) return;
            if (!initialized || !spoilable.initialized) return;
            if (frozen || spoilable.frozen) {
                return;
            }
            long tick1 = creationTick;
            long tick2 = spoilable.creationTick;
            if (tick1 != tick2) {
                long average;
                if (!stack.isEmpty() && !other.isEmpty()) {
                    average = (tick1 * stack.getCount() + tick2 * other.getCount()) /
                            (stack.getCount() + other.getCount());
                } else average = tick1;
                stack.set(GTDataComponents.SPOILABLE, this.withCreationTick(average));
                other.set(GTDataComponents.SPOILABLE, spoilable.withCreationTick(average));
            }
        }
    }

    /**
     * Consider frozen and non-frozen spoilables equal. This is done to allow filtering by ticks remaining until
     * spoiled.<br>
     * If you want the player to have frozen stacks in their inventory, set this to {@code false} to prevent players
     * from
     * entirely bypassing the spoilage system.
     */
    public static boolean FROZEN_EQUALITY = true;
    @Getter
    private final ItemStack stack;

    private final Item originalItem;

    public SpoilableItemStack(ItemStack stack) {
        this.stack = stack;
        this.originalItem = stack.getItem();
    }

    private SpoilableData getData() {
        return stack.getOrDefault(GTDataComponents.SPOILABLE, SpoilableData.EMPTY);
    }

    private void setData(SpoilableData data) {
        stack.set(GTDataComponents.SPOILABLE, data);
    }

    public void setCreationTick(long creationTick) {
        setData(getData()
                .withCreationTick(creationTick)
                .withInitialized(true));
    }

    public void setSpoilContext(SpoilContext spoilContext) {
        setData(getData().withSpoilContext(spoilContext));
    }

    public void setFrozenTicks(long frozenTicks) {
        setData(getData().withFrozenTicks(frozenTicks));
    }

    public void setFrozen(boolean frozen) {
        setData(getData().withFrozen(frozen));
    }

    @Override
    public long getCreationTick() {
        return getData().creationTick;
    }

    public long getFrozenTicks() {
        return getData().frozenTicks;
    }

    public boolean isInitialized() {
        return getData().initialized;
    }

    public boolean isFrozen() {
        return getData().frozen;
    }

    public SpoilContext getSpoilContext() {
        return getData().spoilContext;
    }

    /**
     * Checks if this item should've already spoiled, and calls
     * {@link ISpoilableItem#spoilResult(SpoilContext, boolean)}
     * with {@link SpoilableItemStack#getSpoilContext()}
     * and replaces this item with its return value if so.<br>
     * Also sets the {@link SpoilContext} stored in the mixin to the provided
     * context if it is non-empty (determined by {@link SpoilContext#isEmpty()}).<br>
     * <br>
     * If {@code createTag = true} and the spoilage tag did not exist, creates
     * the tag and sets the creation tick to this tick.<br>
     * If {@code createTag = false} and the spoilage tag isn't present, does nothing.
     *
     * @param createTag Whether to create a spoilage tag if it wasn't present.
     *                  Usually {@code true} for stacks that are present in-world,
     *                  and {@code false} for stacks in XEI, icons, quests, etc.
     *
     * @implNote This method is injected into all of {@link ItemStack}'s getters to
     *           be called with an empty {@link SpoilContext} and {@code createTag = false}.
     */
    public void updateFreshness(SpoilContext spoilContext, boolean createTag) {
        if (!stack.is(originalItem)) return;
        if (!createTag && !isInitialized() || isFrozen()) return;
        if (!spoilContext.isEmpty()) setSpoilContext(spoilContext);
        Level level = SpoilContext.getDefaultLevel();
        if (level != null && createTag && !isInitialized()) {
            setCreationTick(level.getGameTime());
        }
        if (level != null && this.shouldSpoil()) {
            long spoilTicks = this.getSpoilTicks();
            long timeDifference = level.getGameTime() - getCreationTick() - spoilTicks;
            if (timeDifference >= 0) {
                ItemStack newStack = this.spoilResult(getSpoilContext(), GTCEu.isClientThread());
                ((ISpoilableItemStackExtension) (Object) stack).gtceu$forceContentTo(newStack);
                onItemChanged();
                ISpoilableItem newSpoilable = GTCapabilityHelper.getSpoilable(stack);
                if (newSpoilable != null) {
                    newSpoilable.setCreationTick(level.getGameTime() - timeDifference);
                    try {
                        newSpoilable.updateFreshness(spoilContext, false);
                    } catch (StackOverflowError ignored) {
                        // if items spoil in a giant chain or a loop
                    }
                }
            }
        }
    }

    @Override
    public long getTicksUntilSpoiled() {
        updateFreshness(new SpoilContext(), false);
        if (!isInitialized()) return this.getSpoilTicks();
        if (isFrozen()) return getFrozenTicks();
        Level level = SpoilContext.getDefaultLevel();
        if (level != null) {
            return this.getSpoilTicks() - level.getGameTime() +
                    this.getCreationTick();
        }
        return this.getSpoilTicks();
    }

    @Override
    public void setTicksUntilSpoiled(long value) {
        updateFreshness(new SpoilContext(), false);
        Level level = SpoilContext.getDefaultLevel();
        if (level != null && isInitialized())
            setCreationTick(level.getGameTime() - this.getSpoilTicks() + value);
    }

    private void setFreezeSpoiling(boolean freeze) {
        if (freeze) {
            updateFreshness(new SpoilContext(), true);
            setFrozenTicks(getTicksUntilSpoiled());
            setFrozen(true);
        } else {
            if (isInitialized() && isFrozen()) {
                setTicksUntilSpoiled(getFrozenTicks());
                setFrozen(false);
            }
        }
    }

    @Override
    public void freezeSpoiling() {
        setFreezeSpoiling(true);
    }

    @Override
    public void unfreezeSpoiling() {
        setFreezeSpoiling(false);
    }

    @Override
    public boolean shouldSpoil() {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable(
                "gtceu.tooltip.spoil_time_remaining",
                Component.literal(FormattingUtil.formatTime(getTicksUntilSpoiled()))
                        .withStyle(ChatFormatting.DARK_AQUA)));
        tooltipComponents.add(Component.translatable(
                "gtceu.tooltip.spoils_into", getSpoilResultTooltip()));
        if (isAdvanced.isAdvanced()) {
            tooltipComponents.add(Component.translatable(
                    "gtceu.tooltip.spoil_time_total",
                    Component.literal(FormattingUtil.formatTime(getSpoilTicks()))
                            .withStyle(ChatFormatting.GREEN)));
            tooltipComponents.add(Component.translatable(
                    "gtceu.tooltip.creation_tick",
                    getCreationTick()));
            SpoilContext ctx = getSpoilContext();
            if (ctx.level() != null && ctx.pos() != null)
                tooltipComponents.add(Component.translatable("gtceu.tooltip.location",
                        ctx.level().dimension().location().toString(),
                        ctx.pos().getX(), ctx.pos().getY(), ctx.pos().getZ()));
            if (ctx.entity() != null) tooltipComponents.add(Component.translatable("gtceu.tooltip.location_entity",
                    ctx.entity().getType().getDescription()));
            if (ctx.itemHandlerSource() != null) tooltipComponents
                    .add(Component.translatable("gtceu.tooltip.item_handler_source", ctx.itemHandlerSource()));
            if (ctx.itemHandlerData() != null)
                tooltipComponents.add(
                        Component.translatable("gtceu.tooltip.item_handler_data", ctx.itemHandlerData().toString()));
            if (ctx.slot() != -1)
                tooltipComponents.add(Component.translatable("gtceu.tooltip.location_slot", ctx.slot()));
        }
    }

    protected Component getSpoilResultTooltip() {
        return spoilResult(new SpoilContext(), false).getDisplayName();
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return FastColor.ARGB32.color(255, 255, 255, 255);
    }

    @Override
    public boolean doDamagedStateColors(ItemStack itemStack) {
        return false;
    }

    @Override
    public @Nullable IntIntPair getDurabilityColorsForDisplay(ItemStack itemStack) {
        return IntIntPair.of(getBarColor(itemStack), getBarColor(itemStack));
    }

    @Override
    public float getDurabilityForDisplay(ItemStack stack) {
        return (float) getTicksUntilSpoiled() / getSpoilTicks();
    }

    /**
     * Called when the stack has spoiled and transformed into a different item.
     */
    protected void onItemChanged() {}
}
