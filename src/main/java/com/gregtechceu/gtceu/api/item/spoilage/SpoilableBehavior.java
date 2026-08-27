package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.events.RegisterSpoilablesEvent;
import com.gregtechceu.gtceu.common.item.SpoilableItemStack;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

/**
 * This object holds all spoilage-related data for specific item types,
 * NOT individual item stacks. This class handles attaching the {@link ISpoilableItem}
 * capability by registering its' instances to the {@link RegisterCapabilitiesEvent}
 */
public class SpoilableBehavior {

    private final ToLongFunction<ItemStack> ticks;
    private final SpoilResultProvider spoilResult;
    private final Function<ItemStack, Component> spoilsIntoTooltip;
    private final List<Item> attachedTo = new ArrayList<>();

    public static void init() {
        RegisterSpoilablesEvent event = new RegisterSpoilablesEvent(SpoilableBehavior::builder);
        ModLoader.postEvent(event);
    }

    private static Builder builder() {
        return new Builder();
    }

    private SpoilableBehavior(ToLongFunction<ItemStack> ticks, SpoilResultProvider spoilResult,
                              Function<ItemStack, Component> spoilsIntoTooltip) {
        this.ticks = ticks;
        this.spoilResult = spoilResult;
        this.spoilsIntoTooltip = spoilsIntoTooltip;
    }

    /**
     * Registers this object to the {@link RegisterCapabilitiesEvent} if it wasn't registered yet,
     * and adds the specified item to the list of items that this will attach to.
     * 
     * @param item the item to attach the behavior to
     * @return this
     */
    public SpoilableBehavior attachTo(ItemLike item) {
        if (attachedTo.isEmpty()) {
            GTCEu.gtModBus.addListener(this::registerCapabilities);
        }
        attachedTo.add(item.asItem());
        return this;
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(GTCapability.CAPABILITY_SPOILABLE_ITEM, (stack, ctx) -> new SpoilableBehaviourStack(stack),
                attachedTo.toArray(Item[]::new));
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
            return SpoilableBehavior.this.ticks.applyAsLong(getStack());
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

    @Accessors(fluent = true)
    @Setter
    public static class Builder {

        private ToLongFunction<ItemStack> ticks;
        private SpoilResultProvider result;
        private Function<ItemStack, Component> tooltip;

        private Builder() {
            ticks(100);
            result(ItemStack.EMPTY);
        }

        public SpoilableBehavior build() {
            return new SpoilableBehavior(ticks, result, tooltip);
        }

        @Tolerate
        public Builder ticks(long ticks) {
            return ticks(stack -> ticks);
        }

        @Tolerate
        public Builder result(ItemLike itemLike) {
            return result(stack -> itemLike.asItem().getDefaultInstance().copyWithCount(stack.getCount()));
        }

        @Tolerate
        public Builder result(ItemStack stack) {
            return result(stack1 -> stack.copyWithCount(stack1.getCount()));
        }

        @Tolerate
        public Builder result(Function<ItemStack, ItemStack> result) {
            return result((stack, spoilContext, simulate) -> result.apply(stack))
                    .tooltip(stack -> {
                        ItemStack resultStack = result.apply(stack);
                        if (resultStack.isEmpty()) return Component.empty();
                        return resultStack.getHoverName();
                    });
        }

        @Tolerate
        public Builder result(EntityType<? extends Mob> entityType) {
            return result(() -> entityType);
        }

        @Tolerate
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
                    if (ItemStack.isSameItemSameComponents(total, temp)) total.grow(temp.getCount());
                }
                return total;
            }).tooltip(stack -> {
                MutableComponent component = Component.literal("(");
                component.append(previousTooltip.apply(stack));
                component.append(") x").append(Integer.toString(mult));
                return component;
            });
        }

        @Tolerate
        public Builder tooltip(Component tooltip) {
            return tooltip(stack -> tooltip);
        }
    }
}
