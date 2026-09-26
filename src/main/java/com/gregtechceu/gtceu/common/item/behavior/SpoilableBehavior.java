package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.events.RegisterSpoilablesEvent;
import com.gregtechceu.gtceu.api.item.component.ISpoilableItem;
import com.gregtechceu.gtceu.api.item.component.SpoilContext;
import com.gregtechceu.gtceu.api.item.component.SpoilUtils;
import com.gregtechceu.gtceu.common.item.SpoilableItemStack;
import com.gregtechceu.gtceu.integration.kjs.GTCEuStartupEvents;
import com.gregtechceu.gtceu.integration.kjs.events.RegisterSpoilablesEventJS;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
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
    private final List<ItemLike> attachedTo = new ArrayList<>();

    public static void init(IEventBus eventBus) {
        RegisterSpoilablesEvent event = new RegisterSpoilablesEvent(SpoilableBehavior::builder);
        eventBus.post(event);
        if (GTCEu.Mods.isKubeJSLoaded()) {
            KJSCallWrapper.fireKJSEvent(event);
        }
    }

    private static Builder builder() {
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
        attachedTo.add(item);
        return this;
    }

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (attachedTo.stream().map(ItemLike::asItem).anyMatch(stack::is)) {
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
        private final List<SpoilResultProvider> result = new ArrayList<>();
        private final List<Function<ItemStack, Component>> tooltip = new ArrayList<>();

        private Builder() {
            ticks(100);
            result(ItemStack.EMPTY);
        }

        public SpoilableBehavior build() {
            return new SpoilableBehavior(ticks, (stack, ctx, simulate) -> {
                ItemStack lastResult = ItemStack.EMPTY;
                for (SpoilResultProvider spoilResult : result) {
                    ItemStack tempResult = spoilResult.getSpoilResult(stack, ctx, simulate);
                    if (!tempResult.isEmpty()) lastResult = tempResult;
                }
                return lastResult;
            }, stack -> {
                MutableComponent component = Component.empty();
                this.tooltip.stream()
                        .map(func -> func.apply(stack))
                        .forEach(c -> {
                            if (!component.getString().isEmpty()) component.append(", ");
                            component.append(c);
                        });
                return component;
            });
        }

        public Builder ticks(Function<ItemStack, Long> ticks) {
            this.ticks = ticks;
            return this;
        }

        public Builder result(SpoilResultProvider result) {
            this.result.add(result);
            return this;
        }

        public Builder tooltip(Function<ItemStack, Component> tooltip) {
            this.tooltip.add(tooltip);
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
            return result((stack, spoilContext, simulate) -> {
                if (!simulate) {
                    EntityType<? extends Mob> type = entityType.get();
                    SpoilUtils.spawnEntity(spoilContext, type, stack.getCount());
                }
                return ItemStack.EMPTY;
            }).tooltip(stack -> {
                EntityType<? extends Mob> type = entityType.get();
                return type.getDescription().copy();
            });
        }

        public Builder multiplyLastResult(float mult) {
            if (!this.result.isEmpty()) {
                SpoilResultProvider lastResult = this.result.get(this.result.size() - 1);
                this.result.remove(this.result.size() - 1);

                this.result((stack, spoilContext, simulate) -> {
                    ItemStack total = ItemStack.EMPTY;
                    for (float multLeft = mult; multLeft > 0; multLeft--) {
                        if (multLeft < 1 && Math.random() > multLeft) continue;
                        ItemStack temp = lastResult.getSpoilResult(stack, spoilContext, simulate);
                        if (total.isEmpty()) total = temp;
                        else if (ItemStack.isSameItemSameTags(total, temp)) total.grow(temp.getCount());
                    }
                    return total;
                });
            }

            if (!this.tooltip.isEmpty()) {
                Function<ItemStack, Component> lastTooltip = this.tooltip.get(this.tooltip.size() - 1);
                this.tooltip.remove(this.tooltip.size() - 1);
                this.tooltip(stack -> {
                    MutableComponent component = Component.empty();
                    if (mult < 1) component.append("%.0f%% ".formatted(mult * 100));
                    else if (mult == (int) mult) component.append("%d x ".formatted((int) mult));
                    else component.append("%s x ".formatted(mult));
                    component.append(lastTooltip.apply(stack));
                    return component;
                });
            }

            return this;
        }

        public Builder tooltip(Component tooltip) {
            return tooltip(stack -> tooltip);
        }
    }

    private static class KJSCallWrapper {

        public static void fireKJSEvent(RegisterSpoilablesEvent event) {
            GTCEuStartupEvents.REGISTER_SPOILABLES.post(new RegisterSpoilablesEventJS(event));
        }
    }
}
