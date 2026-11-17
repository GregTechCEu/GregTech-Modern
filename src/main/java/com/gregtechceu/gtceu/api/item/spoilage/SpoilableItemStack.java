package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This class is a basic implementation of the {@link ISpoilableItem} capability,
 * to be attached to an item in an {@link AttachCapabilitiesEvent<ItemStack>} listener.
 * It leaves some methods unimplemented, such as {@link ISpoilableItem#getSpoilTicks()} and
 * {@link ISpoilableItem#getSpoilResult(SpoilContext, long, boolean)}.
 *
 * @see SpoilableBehavior
 * @see SpoilableBehavior#attachTo(ItemLike)
 */
public abstract class SpoilableItemStack implements ISpoilableItem, IAddInformation, IDurabilityBar,
                                         ICapabilityProvider {

    public static final String FROZEN_KEY = "frozen";
    public static final String FROZEN_TICKS_KEY = "frozenRemainingTicks";
    public static final String CREATION_TICK_KEY = "creationTick";

    @Getter
    private final ItemStack stack;

    public SpoilableItemStack(ItemStack stack) {
        this.stack = stack;
    }

    public long getCreationTick() {
        if (!stack.hasTag()) return 0;
        return stack.getOrCreateTag().getLong(CREATION_TICK_KEY);
    }

    public void setCreationTick(long creationTick) {
        stack.getOrCreateTag().putLong(CREATION_TICK_KEY, creationTick);
    }

    public boolean isFrozen() {
        if (!stack.hasTag()) return false;
        return stack.getOrCreateTag().getBoolean(FROZEN_KEY);
    }

    public long getFrozenTicks() {
        if (!stack.hasTag()) return 0;
        return stack.getOrCreateTag().getLong(FROZEN_TICKS_KEY);
    }

    public void setFrozenTicks(long ticks) {
        stack.getOrCreateTag().putLong(FROZEN_TICKS_KEY, ticks);
    }

    /**
     * Checks if this item should've already spoiled, and calls
     * {@link ISpoilableItem#getSpoilResult(SpoilContext, boolean)}
     * and replaces this item with its return value if so.<br>
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
        if (isFrozen()) return;
        Level level = SpoilContext.getDefaultLevel();
        if (level != null && createTag && getCreationTick() == 0) {
            setCreationTick(level.getGameTime());
        }
        if (level != null && this.shouldSpoil()) {
            long spoilTicks = this.getSpoilTicks();
            long timeDifference = level.getGameTime() - getCreationTick() - spoilTicks;
            if (timeDifference >= 0) {
                ItemStack newStack = this.getSpoilResult(spoilContext, timeDifference, GTCEu.isClientThread());
                spoilContext.setResultStack(newStack);

                ISpoilableItem newSpoilable = GTCapabilityHelper.getSpoilable(newStack);
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
        if (isFrozen()) return getFrozenTicks();

        Level level = SpoilContext.getDefaultLevel();
        if (level == null) {
            return this.getSpoilTicks();
        }
        return this.getSpoilTicks() - level.getGameTime() + this.getCreationTick();
    }

    @Override
    public void setTicksUntilSpoiled(long value) {
        Level level = SpoilContext.getDefaultLevel();
        if (level != null) {
            setCreationTick(level.getGameTime() - this.getSpoilTicks() + value);
        }
    }

    private void setFreezeSpoiling(boolean freeze) {
        if (freeze) {
            setFrozenTicks(getTicksUntilSpoiled());
            stack.getOrCreateTag().putBoolean(FROZEN_KEY, true);
        } else if (isFrozen()) {
            setTicksUntilSpoiled(getFrozenTicks());
            stack.getOrCreateTag().putBoolean(FROZEN_KEY, false);
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
        var time = getTicksUntilSpoiled();
        MutableComponent timeTooltip;
        if (time <= 0) {
            timeTooltip = Component.translatable("gtceu.tooltip.spoil_imminent")
                    .withStyle(ChatFormatting.DARK_GREEN);
        } else {
            timeTooltip = Component.literal(FormattingUtil.formatTime(getTicksUntilSpoiled()))
                    .withStyle(ChatFormatting.DARK_AQUA);
        }
        tooltipComponents.add(Component.translatable(
                "gtceu.tooltip.spoil_time_remaining",
                timeTooltip));
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
        }
    }

    protected Component getSpoilResultTooltip() {
        return getSpoilResult(SpoilContext.EMPTY, -1, false).getDisplayName();
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
        return getCreationTick() == 0 ? 1 : Mth.clamp((float) getTicksUntilSpoiled() / getSpoilTicks(), 0, 1);
    }

    private final LazyOptional<ISpoilableItem> capability = LazyOptional.of(() -> this);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return GTCapability.CAPABILITY_SPOILABLE_ITEM.orEmpty(cap, capability);
    }
}
