package com.gregtechceu.gtceu.common.item;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.IMergeableNBTSerializable;
import com.gregtechceu.gtceu.api.item.ISpoilableItemStackExtension;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.common.item.behavior.SpoilableBehavior;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This class is a basic implementation of the {@link ISpoilableItem} capability,
 * to be attached to an item in an {@link AttachCapabilitiesEvent<ItemStack>} listener.
 * It leaves some methods unimplemented, such as {@link ISpoilableItem#getSpoilTicks()} and
 * {@link ISpoilableItem#spoilResult(SpoilContext, boolean)}.
 *
 * @implNote this class uses a mixin in its {@link ISpoilableItem#updateFreshness} implementation
 *
 * @see SpoilableBehavior
 * @see SpoilableBehavior#attachTo(ItemLike)
 */
public abstract class SpoilableItemStack implements ISpoilableItem, IAddInformation, IDurabilityBar,
                                         IMergeableNBTSerializable, ICapabilityProvider {

    public static final String SPOIL_CONTEXT_KEY = "spoilContext";
    public static final String FROZEN_TICKS_KEY = "frozenRemainingTicks";
    public static final String CREATION_TICK_KEY = "creationTick";
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

    private boolean initialized;
    @Getter
    private boolean frozen = false;
    private long frozenTicks;
    @Getter
    private long creationTick = 0;
    @Getter
    @Setter
    private SpoilContext spoilContext = new SpoilContext();

    public SpoilableItemStack(ItemStack stack) {
        this.stack = stack;
        this.originalItem = stack.getItem();
    }

    public void setCreationTick(long creationTick) {
        this.initialized = true;
        this.creationTick = creationTick;
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
        if (!createTag && !initialized || frozen) return;
        if (!spoilContext.isEmpty()) setSpoilContext(spoilContext);
        Level level = SpoilContext.getDefaultLevel();
        if (level != null && createTag && !initialized) {
            setCreationTick(level.getGameTime());
        }
        if (level != null && this.shouldSpoil()) {
            long spoilTicks = this.getSpoilTicks();
            long timeDifference = level.getGameTime() - creationTick - spoilTicks;
            if (timeDifference >= 0) {
                ItemStack newStack = this.spoilResult(getSpoilContext(), GTCEu.isClientThread());
                ((ISpoilableItemStackExtension) (Object) stack).gtceu$setStack(newStack);
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
        if (!initialized) return this.getSpoilTicks();
        if (frozen) return frozenTicks;
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
        if (level != null && initialized)
            setCreationTick(level.getGameTime() - this.getSpoilTicks() + value);
    }

    private void setFreezeSpoiling(boolean freeze) {
        if (freeze) {
            updateFreshness(new SpoilContext(), true);
            frozenTicks = getTicksUntilSpoiled();
            frozen = true;
        } else {
            if (initialized && frozen) {
                setTicksUntilSpoiled(frozenTicks);
                frozen = false;
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
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
                        ctx.level().dimensionTypeId().location().toString(),
                        ctx.pos().getX(), ctx.pos().getY(), ctx.pos().getZ()));
            if (ctx.entity() != null) tooltipComponents.add(Component.translatable("gtceu.tooltip.location_entity",
                    ctx.entity().getType().getDescription()));
            if (ctx.itemHandlerSource() != null) tooltipComponents
                    .add(Component.translatable("gtceu.tooltip.item_handler_source", ctx.itemHandlerSource()));
            if (ctx.itemHandlerData() != null)
                tooltipComponents.add(Component.translatable("gtceu.tooltip.item_handler_data", ctx.itemHandlerData()));
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

    @Override
    public Tag serializeNBT() {
        if (!initialized) return new CompoundTag();
        CompoundTag tag = new CompoundTag();
        tag.put(SPOIL_CONTEXT_KEY, getSpoilContext().serializeNBT());
        tag.putLong(CREATION_TICK_KEY, getCreationTick());
        if (isFrozen())
            tag.putLong(FROZEN_TICKS_KEY, frozenTicks);
        return tag;
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if (nbt instanceof CompoundTag tag && !tag.isEmpty()) {
            initialized = true;
            spoilContext = SpoilContext.deserializeNBT(tag.getCompound(SPOIL_CONTEXT_KEY));
            creationTick = tag.getLong(CREATION_TICK_KEY);
            if (tag.contains(FROZEN_TICKS_KEY, Tag.TAG_LONG)) {
                frozenTicks = tag.getLong(FROZEN_TICKS_KEY);
                frozen = true;
            } else frozen = false;
        } else initialized = false;
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
    public void prepareForComparisonWith(INBTSerializable<Tag> other) {
        if (other instanceof SpoilableItemStack spoilable) {
            if (!initialized || !spoilable.initialized) return;
            if (isFrozen() || spoilable.isFrozen()) {
                return;
            }
            long tick1 = getCreationTick();
            long tick2 = spoilable.getCreationTick();
            if (tick1 != tick2) {
                long average;
                if (!stack.isEmpty() && !spoilable.stack.isEmpty()) {
                    average = (tick1 * stack.getCount() + tick2 * spoilable.stack.getCount()) /
                            (stack.getCount() + spoilable.stack.getCount());
                } else average = tick1;
                this.setCreationTick(average);
                spoilable.setCreationTick(average);
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return GTCapability.CAPABILITY_SPOILABLE_ITEM.orEmpty(cap, LazyOptional.of(() -> this));
    }

    /**
     * Called when the stack has spoiled and transformed into a different item.
     */
    protected void onItemChanged() {}
}
