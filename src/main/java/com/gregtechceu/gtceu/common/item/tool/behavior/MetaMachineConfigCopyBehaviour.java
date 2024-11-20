package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;

import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaMachineConfigCopyBehaviour implements IInteractionItem, IAddInformation {

    public static final String CONFIG_DATA = "config_data";
    public static final String ORIGINAL_FRONT = "front";
    public static final String ITEM_CONFIG = "item";
    public static final String FLUID_CONFIG = "fluid";
    public static final String DIRECTION = "direction";
    public static final String AUTO = "auto";
    public static final String INPUT_FROM_OUTPUT_SIDE = "in_from_out";
    public static final String MUFFLED = "muffled";
    public static final String CIRCUIT = "circuit";

    public static int directionToInt(@Nullable Direction direction) {
        return direction == null ? 0 : direction.ordinal() + 1;
    }

    public static Direction intToDirection(int ordinal) {
        return ordinal <= 0 || ordinal > Direction.values().length ? null : Direction.values()[ordinal - 1];
    }

    public static Direction getRelativeDirection(Direction originalFront, Direction currentFacing, Direction face) {
        if ((currentFacing == null || originalFront == null) || (currentFacing == originalFront) ||
                (face == Direction.UP || face == Direction.DOWN))
            return face;

        Direction newFace = originalFront;
        int i;
        for (i = 0; i < 4 && newFace != currentFacing; i++) newFace = newFace.getClockWise();

        newFace = face;
        for (int j = 0; j < i; j++) newFace = newFace.getClockWise();
        return newFace;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MetaMachineBlockEntity blockEntity) {
            if (blockEntity.getOwner() != null && !blockEntity.getOwner().isPlayerInTeam(context.getPlayer()))
                return InteractionResult.FAIL;
            MetaMachine machine = blockEntity.getMetaMachine();
            if (machine instanceof MultiblockControllerMachine) return InteractionResult.PASS;
            if (context.isSecondaryUseActive())
                return handleCopy(stack, machine);
            return handlePaste(stack, machine);
        }
        if (context.isSecondaryUseActive() && context.getLevel().getBlockState(context.getClickedPos()).isAir()) {
            stack.getOrCreateTag().remove(CONFIG_DATA);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult handleCopy(ItemStack stack, MetaMachine machine) {
        CompoundTag configData = new CompoundTag();
        configData.putInt(ORIGINAL_FRONT, directionToInt(machine.getFrontFacing()));
        if (machine instanceof IAutoOutputItem autoOutputItem) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putInt(DIRECTION, directionToInt(autoOutputItem.getOutputFacingItems()));
            itemTag.putBoolean(AUTO, autoOutputItem.isAutoOutputItems());
            itemTag.putBoolean(INPUT_FROM_OUTPUT_SIDE, autoOutputItem.isAllowInputFromOutputSideItems());
            configData.put(ITEM_CONFIG, itemTag);
        }
        if (machine instanceof IAutoOutputFluid autoOutputFluid) {
            CompoundTag fluidTag = new CompoundTag();
            fluidTag.putInt(DIRECTION, directionToInt(autoOutputFluid.getOutputFacingFluids()));
            fluidTag.putBoolean(AUTO, autoOutputFluid.isAutoOutputFluids());
            fluidTag.putBoolean(INPUT_FROM_OUTPUT_SIDE, autoOutputFluid.isAllowInputFromOutputSideFluids());
            configData.put(FLUID_CONFIG, fluidTag);
        }
        if (machine instanceof IMufflableMachine mufflableMachine) {
            configData.putBoolean(MUFFLED, mufflableMachine.isMuffled());
        }
        if (machine instanceof SimpleTieredMachine stm) {
			configData.putInt(CIRCUIT, IntCircuitBehaviour.getCircuitConfiguration(stm.getCircuitInventory().getStackInSlot(0)));
        }
        stack.getOrCreateTag().put(CONFIG_DATA, configData);
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult handlePaste(ItemStack stack, MetaMachine machine) {
        CompoundTag root = stack.getOrCreateTag();
        CompoundTag configData = root.getCompound(CONFIG_DATA);
        Direction originalFront = intToDirection(configData.getInt(ORIGINAL_FRONT));
        if (machine instanceof IAutoOutputItem autoOutputItem) {
            CompoundTag itemData = configData.getCompound(ITEM_CONFIG);
            autoOutputItem.setOutputFacingItems(getRelativeDirection(originalFront, machine.getFrontFacing(),
                    intToDirection(itemData.getInt(DIRECTION))));
            autoOutputItem.setAutoOutputItems(itemData.getBoolean(AUTO));
            autoOutputItem.setAllowInputFromOutputSideItems(itemData.getBoolean(INPUT_FROM_OUTPUT_SIDE));
        }
        if (machine instanceof IAutoOutputFluid autoOutputFluid) {
            CompoundTag fluidData = configData.getCompound(FLUID_CONFIG);
            autoOutputFluid.setOutputFacingFluids(getRelativeDirection(originalFront, machine.getFrontFacing(),
                    intToDirection(fluidData.getInt(DIRECTION))));
            autoOutputFluid.setAutoOutputFluids(fluidData.getBoolean(AUTO));
            autoOutputFluid.setAllowInputFromOutputSideFluids(fluidData.getBoolean(INPUT_FROM_OUTPUT_SIDE));
        }
        if (machine instanceof IMufflableMachine mufflableMachine) {
            mufflableMachine.setMuffled(configData.getBoolean(MUFFLED));
        }
		if (machine instanceof SimpleTieredMachine stm && ConfigHolder.INSTANCE.machines.ghostCircuit) {
			stm.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(configData.getInt(CIRCUIT)));
		}
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("behaviour.meta.machine.config.copy.tooltip"));
        tooltipComponents.add(Component.translatable("behaviour.meta.machine.config.paste.tooltip"));
    }
}
