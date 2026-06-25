package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.IDataAccessMachine;
import com.gregtechceu.gtceu.api.capability.IOpticalDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IWorkLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpticalDataHatchMachine extends MultiblockPartMachine implements IOpticalDataAccessHatch {

    @Getter
    private final boolean isTransmitter;

    public OpticalDataHatchMachine(IMachineBlockEntity holder, boolean isTransmitter) {
        super(holder);
        this.isTransmitter = isTransmitter;
    }

    @Override
    public boolean isRecipeAvailable(@NotNull GTRecipe recipe) {
        if (!isFormed()) {
            return false;
        }
        if (isTransmitter()) {
            IMultiController controller = getControllers().first();
            return controller instanceof IDataAccessMachine dataAccessMachine && dataAccessMachine.isRecipeAvailable(recipe);

        } else {
            var dataHatch = getDataHatch();
            return dataHatch != null && dataHatch.isRecipeAvailable(recipe);
        }
    }

    @Override
    public void notifyListeners() {
        if(isTransmitter()) {
            var dataHatch = getDataHatch();
            if(dataHatch != null) dataHatch.notifyListeners();
        } else {
            for(var controller :getControllers()) {
                if(controller instanceof IDataAccessMachine dataAccessMachine) {
                    dataAccessMachine.notifyListeners();
                } else if(controller instanceof IWorkLogicMachine workLogicMachine) {
                    workLogicMachine.getWorkLogic().updateTickSubscription();
                }
            }
        }
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    protected @Nullable IDataAccessMachine getDataHatch() {
        BlockEntity tileEntity = getLevel().getBlockEntity(getPos().relative(getFrontFacing()));
        if (tileEntity == null) return null;

        if (tileEntity instanceof OpticalPipeBlockEntity blockEntity) {
            return blockEntity.getCapability(GTCapability.CAPABILITY_DATA_ACCESS, getFrontFacing().getOpposite()).orElse(null);
        }
        return null;
    }

}
