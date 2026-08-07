package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.component.ISpoilableItem;
import com.gregtechceu.gtceu.api.item.component.SpoilContext;
import com.gregtechceu.gtceu.api.item.component.SpoilUtils;
import com.gregtechceu.gtceu.common.item.SpoilableItemStack;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This object holds all spoilage-related data for specific item types,
 * NOT individual item stacks. This class handles attaching the {@link ISpoilableItem}
 * capability by registering its' instances to the {@link AttachCapabilitiesEvent}
 */
public class SpoilableBehavior {

    private final Function<ItemStack, Long> ticks;
    private final SpoilResultProvider spoilResult;
    private final Function<ItemStack, Component> spoilsIntoTooltip;
    private final List<Item> attachedTo = new ArrayList<>();

    public static Builder builder() {
        return new Builder();
    }

    private SpoilableBehavior(Function<ItemStack, Long> ticks, SpoilResultProvider spoilResult,
                              Function<ItemStack, Component> spoilsIntoTooltip) {
        this.ticks = ticks;
        this.spoilResult = spoilResult;
        this.spoilsIntoTooltip = spoilsIntoTooltip;
    }

    /**
     * Registers this object to the {@link AttachCapabilitiesEvent} if it wasn't registered yet,
     * and adds the specified item to the list of items that this will attach to.
     * 
     * @param item the item to attach the behavior to
     * @return this
     */
    public SpoilableBehavior attachTo(ItemLike item) {
        if (attachedTo.isEmpty()) {
            MinecraftForge.EVENT_BUS.register(this);
        }
        attachedTo.add(item.asItem());
        return this;
    }

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (attachedTo.stream().anyMatch(stack::is)) {
            event.addCapability(GTCEu.id("spoilable"), new SpoilableBehaviourStack(stack));
        }
    }

    /**
     * An implementation of {@link ISpoilableItem} which returns spoil information from its'
     * {@code SpoilableBehavior} object.
     */
    public class SpoilableBehaviourStack extends SpoilableItemStack {

        private SpoilableBehaviourStack(ItemStack stack) {
            super(stack);
        }

        @Override
        public long getSpoilTicks() {
            return ticks.apply(getStack());
        }

        @Override
        public ItemStack spoilResult(SpoilContext spoilContext, boolean simulate) {
            return spoilResult.getSpoilResult(getStack(), spoilContext, simulate);
        }

        @Override
        protected @NotNull Component getSpoilResultTooltip() {
            return spoilsIntoTooltip.apply(getStack());
        }
    }

    @FunctionalInterface
    public interface SpoilResultProvider {

        ItemStack getSpoilResult(@NotNull ItemStack stack, @NotNull SpoilContext spoilContext, boolean simulate);
    }

    public static class Builder {

        private Function<ItemStack, Long> ticks;
        private SpoilResultProvider result;
        private Function<ItemStack, Component> tooltip;

        private Builder() {
            ticks(100);
            result(ItemStack.EMPTY);
        }

        public SpoilableBehavior build() {
            return new SpoilableBehavior(ticks, result, tooltip);
        }

        public Builder ticks(Function<ItemStack, Long> ticks) {
            this.ticks = ticks;
            return this;
        }

        public Builder result(SpoilResultProvider result) {
            this.result = result;
            return this;
        }

        public Builder tooltip(Function<ItemStack, Component> tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder ticks(long ticks) {
            return ticks(stack -> ticks);
        }

        public Builder result(ItemLike itemLike) {
            return result(stack -> itemLike.asItem().getDefaultInstance().copyWithCount(stack.getCount()));
        }

        public Builder result(ItemStack stack) {
            return result(stack1 -> stack.copyWithCount(stack1.getCount()));
        }

        public Builder result(Function<ItemStack, ItemStack> result) {
            return result((stack, spoilContext, simulate) -> result.apply(stack))
                    .tooltip(stack -> {
                        ItemStack resultStack = result.apply(stack);
                        if (resultStack.isEmpty()) return Component.empty();
                        return resultStack.getHoverName();
                    });
        }

        public Builder result(EntityType<? extends Mob> entityType) {
            return result(() -> entityType);
        }

        public Builder result(Supplier<? extends EntityType<? extends Mob>> entityType) {
            SpoilResultProvider previousResult = result;
            Function<ItemStack, Component> previousTooltip = tooltip;
            return result((stack, spoilContext, simulate) -> {
                if (!simulate) {
                    EntityType<? extends Mob> type = entityType.get();
                    SpoilUtils.spawnEntity(spoilContext, type, stack.getCount());
                }
                return previousResult.getSpoilResult(stack, spoilContext, simulate);
            }).tooltip(stack -> {
                EntityType<? extends Mob> type = entityType.get();
                MutableComponent component = type.getDescription().copy();
                Component previous = previousTooltip.apply(stack);
                if (!previous.getString().isEmpty()) component.append(", ").append(previous);
                return component;
            });
        }

        public Builder multiplyResult(int mult) {
            SpoilResultProvider prevResult = result;
            Function<ItemStack, Component> previousTooltip = tooltip;
            return result((stack, spoilContext, simulate) -> {
                ItemStack total = prevResult.getSpoilResult(stack, spoilContext, simulate);
                for (int i = 1; i < mult; i++) {
                    ItemStack temp = prevResult.getSpoilResult(stack, spoilContext, simulate);
                    if (ItemStack.isSameItemSameTags(total, temp)) total.grow(temp.getCount());
                }
                return total;
            }).tooltip(stack -> {
                MutableComponent component = Component.literal("(");
                component.append(previousTooltip.apply(stack));
                component.append(") x").append(Integer.toString(mult));
                return component;
            });
        }

        public Builder tooltip(Component tooltip) {
            return tooltip(stack -> tooltip);
        }
    }
}
