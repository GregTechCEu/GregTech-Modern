package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.gui.factory.GTHeldItemUIHolder;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class IntCircuitBehaviour implements IItemUIFactory, IAddInformation {

    public static final int CIRCUIT_MAX = 32;

    public static ItemStack stack(int configuration) {
        var stack = GTItems.PROGRAMMED_CIRCUIT.asStack();
        setCircuitConfiguration(stack, configuration);
        return stack;
    }

    public static void setCircuitConfiguration(GTHeldItemUIHolder holder, int configuration) {
        setCircuitConfiguration(holder.getHeld(), configuration);
        holder.markAsDirty();
    }

    public static void setCircuitConfiguration(ItemStack itemStack, int configuration) {
        if (configuration < 0 || configuration > CIRCUIT_MAX)
            throw new IllegalArgumentException("Given configuration number is out of range!");
        itemStack.set(GTDataComponents.CIRCUIT_CONFIG, configuration);
    }

    public static int getCircuitConfiguration(ItemStack itemStack) {
        return itemStack.getOrDefault(GTDataComponents.CIRCUIT_CONFIG, 0);
    }

    public static boolean isIntegratedCircuit(ItemStack itemStack) {
        return GTItems.PROGRAMMED_CIRCUIT.isIn(itemStack);
    }

    // deprecated, not needed (for now)
    @Deprecated
    public static void adjustConfiguration(GTHeldItemUIHolder holder, int amount) {
        adjustConfiguration(holder.getHeld(), amount);
        holder.markAsDirty();
    }

    // deprecated, not needed (for now)
    @Deprecated
    public static void adjustConfiguration(ItemStack stack, int amount) {
        if (!isIntegratedCircuit(stack)) return;
        int configuration = getCircuitConfiguration(stack);
        configuration += amount;
        configuration = Mth.clamp(configuration, 0, CIRCUIT_MAX);
        setCircuitConfiguration(stack, configuration);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        int configuration = getCircuitConfiguration(stack);
        tooltipComponents.add(Component.translatable("metaitem.int_circuit.configuration", configuration));
    }

    @Override
    public Object createUI(GTHeldItemUIHolder holder, Player entityPlayer) {
        return IntCircuitBehaviourUI.create(holder, entityPlayer);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var stack = context.getItemInHand();
        int circuitSetting = getCircuitConfiguration(stack);
        BlockEntity entity = context.getLevel().getBlockEntity(context.getClickedPos());
        if (entity instanceof MetaMachine machine && context.isSecondaryUseActive()) {
            if (machine instanceof IHasCircuitSlot circuitMachine &&
                    circuitMachine.getCircuitInventory().getSlots() > 0) {
                setCircuitConfig(circuitMachine.getCircuitInventory(), circuitSetting);
            }
            if (!ConfigHolder.INSTANCE.machines.ghostCircuit)
                stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return IItemUIFactory.super.useOn(context);
    }

    void setCircuitConfig(NotifiableItemStackHandler circuit, int value) {
        circuit.setStackInSlot(0, IntCircuitBehaviour.stack(value));
    }
}
