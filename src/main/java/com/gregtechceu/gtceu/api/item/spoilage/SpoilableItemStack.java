package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This class is a basic implementation of the {@link ISpoilableItem} capability,
 * to be attached to an item in the {@link RegisterCapabilitiesEvent <ItemStack>} listener.
 * It leaves some methods unimplemented, such as {@link ISpoilableItem#getSpoilTicks()} and
 * {@link ISpoilableItem#getSpoilResult(SpoilContext, long, boolean)}.
 *
 * @see SpoilableBehavior
 * @see SpoilableBehavior#attachTo(ItemLike)
 */
public abstract class SpoilableItemStack implements ISpoilableItem, IAddInformation, IDurabilityBar {

    @Getter
    private final ItemStack stack;

    public SpoilableItemStack(ItemStack stack) {
        this.stack = stack;
    }

    public long getCreationTick() {
        return stack.getOrDefault(GTDataComponents.SPOILAGE_CREATION_TICK, Long.MAX_VALUE);
    }

    public void setCreationTick(long creationTick) {
        stack.set(GTDataComponents.SPOILAGE_CREATION_TICK, creationTick);
    }

    public boolean isFrozen() {
        return stack.has(GTDataComponents.SPOILAGE_FREEZE_TICK);
    }

    public long getFrozenTicks() {
        return stack.getOrDefault(GTDataComponents.SPOILAGE_FREEZE_TICK, Long.MAX_VALUE);
    }

    public void setFrozenTicks(long ticks) {
        stack.set(GTDataComponents.SPOILAGE_FREEZE_TICK, ticks);
    }

    /**
     * Checks if this item should've already spoiled, and calls
     * {@link ISpoilableItem#getSpoilResult(SpoilContext, long, boolean)}
     * and replaces this item with its return value if so.<br>
     * <br>
     * If {@code createTag = true} and the spoilage tag did not exist, creates
     * the tag and sets the creation tick to this tick.<br>
     * If {@code createTag = false} and the spoilage tag isn't present, does nothing.
     *
     * @param createTag Whether to create a spoilage tag if it wasn't present.
     *                  Usually {@code true} for stacks that are present in-world,
     *                  and {@code false} for stacks in recipe viewers, icons, quests, etc.
     *
     * @implNote This method is injected into all of {@link ItemStack}'s getters to
     *           be called with an empty {@link SpoilContext} and {@code createTag = false}.
     */
    public void updateFreshness(SpoilContext spoilContext, boolean createTag) {
        if (isFrozen()) return;
        if (!createTag && getCreationTick() == Long.MAX_VALUE) return;

        Level level = SpoilContext.getDefaultLevel();

        if (level == null) return;

        if (createTag && getCreationTick() == Long.MAX_VALUE) {
            setCreationTick(level.getGameTime());
        }

        if (!this.shouldSpoil()) return;

        long spoilTicks = this.getSpoilTicks();
        long timeDifference = level.getGameTime() - getCreationTick() - spoilTicks;
        if (timeDifference < 0) return;

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
        if (freeze == this.isFrozen()) return;
        Level level = SpoilContext.getDefaultLevel();

        if (!stack.has(GTDataComponents.SPOILAGE_CREATION_TICK) && level != null) {
            setCreationTick(level.getGameTime());
        }

        if (freeze) {
            setFrozenTicks(getTicksUntilSpoiled());
        } else {
            setTicksUntilSpoiled(getFrozenTicks());
            stack.remove(GTDataComponents.SPOILAGE_FREEZE_TICK);
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
        long time = getTicksUntilSpoiled();
        MutableComponent timeTooltip;

        if (getCreationTick() == Long.MAX_VALUE) {
            time = getSpoilTicks();
        }

        if (time <= 0) {
            timeTooltip = Component.translatable("gtceu.tooltip.spoil_imminent")
                    .withStyle(ChatFormatting.DARK_GREEN);
        } else {
            timeTooltip = Component.literal(FormattingUtil.formatTime(time))
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
            if (getCreationTick() != Long.MAX_VALUE) tooltipComponents.add(Component.translatable(
                    "gtceu.tooltip.creation_tick",
                    getCreationTick()));
        }
    }

    protected Component getSpoilResultTooltip() {
        return getSpoilResult(SpoilContext.EMPTY, -1, false).getDisplayName();
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFFFFFFFF;
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
        return getCreationTick() == Long.MAX_VALUE ? 1 : Mth.clamp((float) getTicksUntilSpoiled() / getSpoilTicks(), 0, 1);
    }
}
