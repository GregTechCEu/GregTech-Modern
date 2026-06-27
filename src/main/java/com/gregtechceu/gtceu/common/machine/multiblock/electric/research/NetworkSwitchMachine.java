package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.WorkLogic;
import com.gregtechceu.gtceu.common.computation.ComputationNetworkManager;

import com.gregtechceu.gtceu.common.machine.multiblock.part.OpticalComputationHatchMachine;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

import lombok.Getter;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkSwitchMachine extends WorkableElectricMultiblockMachine {

    public static final int EUT_PER_HATCH = GTValues.VA[GTValues.IV];

    @Getter
    private int energyUsage = 0;
    private boolean computationBridgeActive;

    public NetworkSwitchMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    protected int calculateEnergyUsage() {
        int receivers = 0;
        int transmitters = 0;
        for (var part : this.getParts()) {
            Block block = part.self().getBlockState().getBlock();
            if (PartAbility.COMPUTATION_DATA_RECEPTION.isApplicable(block)) {
                ++receivers;
            }
            if (PartAbility.COMPUTATION_DATA_TRANSMISSION.isApplicable(block)) {
                ++transmitters;
            }
        }
        return GTValues.VA[GTValues.IV] * (receivers + transmitters);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        energyUsage = calculateEnergyUsage();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        energyUsage = 0;
        updateComputationBridgeActive(false);
    }

    @Override
    public void serverRunningTick() {
        int energyToConsume = getEnergyUsage();
        if (energyContainer.getEnergyStored() >= energyToConsume &&
                energyContainer.removeEnergy(energyToConsume) >= energyToConsume) {
            getWorkLogic().setStatus(WorkLogic.Status.WORKING);
            updateComputationBridgeActive(true);
        } else {
            setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_in")
                    .append(": ").append(EURecipeCapability.CAP.getName()));
            updateComputationBridgeActive(false);
        }
    }

    private void updateComputationBridgeActive(boolean active) {
        if (computationBridgeActive == active) return;
        computationBridgeActive = active;
        if (getLevel() instanceof ServerLevel serverLevel) {
            ComputationNetworkManager.get(serverLevel).markTopologyDirty();
        }
    }

    public int getMaxCWUt() {
        for(IMultiPart part : getParts()) {
            if(part instanceof OpticalComputationHatchMachine opticalHatch) {
                return ComputationNetworkManager.get((ServerLevel) getLevel())
                        .getNetWorkMaxCWUt(opticalHatch.getComputationPort());
            }
        }
        return 0;
    }

    public int getUsedCWUt() {
        for(IMultiPart part : getParts()) {
            if(part instanceof OpticalComputationHatchMachine opticalHatch) {
                return ComputationNetworkManager.get((ServerLevel) getLevel())
                        .getNetWorkUsedCWUt(opticalHatch.getComputationPort());
            }
        }
        return 0;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(true, isActive() && isWorkingEnabled()) // transform into two-state system for display
                .setWorkingStatusKeys(
                        "gtceu.multiblock.idling",
                        "gtceu.multiblock.idling",
                        "gtceu.multiblock.data_bank.providing")
                .addEnergyUsageExactLine(getEnergyUsage())
                .addComputationUsageLine(getMaxCWUt())
                .addComputationUsageExactLine(getUsedCWUt())
                .addWorkingStatusLine();
    }
}
