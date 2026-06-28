package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.capability.IObjectHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.computation.ComputationConsumer;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.RecipeElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NetworkedComputationContainer;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerList;


import com.gregtechceu.gtceu.common.computation.ComputationNetworkManager;
import com.gregtechceu.gtceu.common.machine.multiblock.part.OpticalComputationHatchMachine;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ResearchStationMachine extends RecipeElectricMultiblockMachine {

    private final NetworkedComputationContainer importComputation;

    public ResearchStationMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.importComputation = new NetworkedComputationContainer(this, IO.IN);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        for (IMultiPart part : getParts()) {
            if (part instanceof IObjectHolder iObjectHolder) {
                if (iObjectHolder.getFrontFacing() != getFrontFacing().getOpposite()) {
                    onStructureInvalid();
                    return;
                }
            }
        }
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    private int getMaxComputation() {
       for(var part: getParts()) {
           if(part instanceof OpticalComputationHatchMachine opticalMachine) {
               return ComputationNetworkManager.get((ServerLevel) getLevel())
                       .getNetWorkAvailableCWUt(opticalMachine.getComputationPort());
           }
       }
       return 0;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        var builder = MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(recipeLogic.isWorkingEnabled(), recipeLogic.isActive())
                .setWorkingStatusKeys("gtceu.multiblock.idling", "gtceu.multiblock.work_paused",
                        "gtceu.multiblock.research_station.researching")
                .addEnergyUsageLine(energyContainer)
                .addEnergyTierLine(tier)
                .addComputationUsageLine(getMaxComputation())
                .addWorkingStatusLine();

        builder.addProgressLineOnlyPercent(recipeLogic.getProgressPercent());
    }
}
