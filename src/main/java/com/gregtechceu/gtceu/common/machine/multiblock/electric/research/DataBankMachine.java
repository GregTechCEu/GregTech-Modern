package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IDataAccessMachine;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.WorkLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DataBankMachine extends WorkableElectricMultiblockMachine implements IDataAccessMachine {

    public static final int EUT_PER_HATCH = GTValues.VA[GTValues.EV];
    public static final int EUT_PER_HATCH_CHAINED = GTValues.VA[GTValues.LuV];

    private IMaintenanceMachine maintenance;

    public final List<IDataAccessMachine> dataAccesses = new ArrayList<>();
    public final List<IDataAccessMachine> transmitters = new ArrayList<>();
    public final List<IDataAccessMachine> receivers = new ArrayList<>();

    @Getter
    private int energyUsage = 0;

    // to prevent infinite recursion
    private boolean isQuerying;

    public DataBankMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        for (IMultiPart part : getParts()) {
            Block block = part.self().getBlockState().getBlock();
            if (part instanceof IDataAccessMachine hatch && PartAbility.DATA_ACCESS.isApplicable(block)) {
                dataAccesses.add(hatch);

            } else if (part instanceof IDataAccessMachine hatch &&
                    PartAbility.OPTICAL_DATA_RECEPTION.isApplicable(block)) {
                        receivers.add(hatch);
                    } else
                if (part instanceof IDataAccessMachine hatch &&
                        PartAbility.OPTICAL_DATA_TRANSMISSION.isApplicable(block)) {
                            transmitters.add(hatch);
                        } else
                    if (part instanceof IMaintenanceMachine maintenanceMachine) {
                        maintenance = maintenanceMachine;
                    }
        }
        energyUsage = calculateEnergyUsage();

        if (maintenance == null) {
            onStructureInvalid();
        }

        notifyListeners();
    }

    protected int calculateEnergyUsage() {
        int receivers = 0;
        int transmitters = 0;
        int regulars = 0;
        for (var part : this.getParts()) {
            Block block = part.self().getBlockState().getBlock();
            if (PartAbility.OPTICAL_DATA_RECEPTION.isApplicable(block)) {
                ++receivers;
            }
            if (PartAbility.OPTICAL_DATA_TRANSMISSION.isApplicable(block)) {
                ++transmitters;
            }
            if (PartAbility.DATA_ACCESS.isApplicable(block)) {
                ++regulars;
            }
        }

        int dataHatches = receivers + transmitters + regulars;
        int eutPerHatch = receivers > 0 ? EUT_PER_HATCH_CHAINED : EUT_PER_HATCH;
        return eutPerHatch * dataHatches;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        energyUsage = 0;
        dataAccesses.clear();
        transmitters.clear();
        receivers.clear();
    }

    @Override
    public boolean isRecipeAvailable(GTRecipe recipe) {
        if (isQuerying) return false;
        isQuerying = true;
        boolean result = queryRecipe(recipe);
        isQuerying = false;
        return result;
    }

    protected boolean queryRecipe(GTRecipe recipe) {
        if (!getWorkLogic().isWorking()) {
            return false;
        }
        for (IDataAccessMachine hatch : dataAccesses) {
            if (hatch.isRecipeAvailable(recipe)) {
                return true;
            }
        }
        for (IDataAccessMachine hatch : receivers) {
            if (hatch.isRecipeAvailable(recipe)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void notifyListeners() {
        if (isQuerying) return;
        isQuerying = true;
        for (IDataAccessMachine hatch : transmitters) {
            hatch.notifyListeners();
        }
        isQuerying = false;
    }

    @Override
    public void serverRunningTick() {
        int energyToConsume = getEnergyUsage();
        boolean hasMaintenance = ConfigHolder.INSTANCE.machines.enableMaintenance && this.maintenance != null;
        if (hasMaintenance) {
            // 10% more energy per maintenance problem
            energyToConsume += maintenance.getNumMaintenanceProblems() * energyToConsume / 10;
        }

        if (energyContainer.getEnergyStored() >= energyToConsume &&
                energyContainer.removeEnergy(energyToConsume) >= energyToConsume) {
            getWorkLogic().setStatus(WorkLogic.Status.WORKING);
        } else {
            setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_in")
                    .append(": ").append(EURecipeCapability.CAP.getName()));
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(true, isActive() && isWorkingEnabled())
                .setWorkingStatusKeys(
                        "gtceu.multiblock.idling",
                        "gtceu.multiblock.idling",
                        "gtceu.multiblock.data_bank.providing")
                .addEnergyUsageExactLine(getEnergyUsage())
                .addWorkingStatusLine();
    }
}
