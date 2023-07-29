package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMaintenanceMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.machine.trait.optical.IOpticalDataAccessHatch;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.config.ConfigHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.*;

public class DataBankMachine extends WorkableElectricMultiblockMachine implements IControllable {

    private IEnergyContainer energyContainer;

    protected boolean hasNotEnoughEnergy;

    @Getter
    private int energyUsage = 0;

    @Nullable
    protected TickableSubscription powerSubs;

    public DataBankMachine(IMachineBlockEntity holder) {
        super(holder);
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        List<IEnergyContainer> energyContainers = new ArrayList<>();
        Set<IOpticalDataAccessHatch> opticalReceptors = new HashSet<>();
        Set<IOpticalDataAccessHatch> opticalTransmitters = new HashSet<>();
        Set<IDataAccessHatch> regulars = new HashSet<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if(io == IO.NONE) continue;
            for (var handler : part.getRecipeHandlers()) {
                // If IO not compatible
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH && io != handler.getHandlerIO()) continue;
                var handlerIO = io == IO.BOTH ? handler.getHandlerIO() : io;
                if (handlerIO == IO.IN && handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) {
                    energyContainers.add(container);
                    traitSubscriptions.add(handler.addChangedListener(this::updatePowerSubscription));
                } else if (handlerIO == IO.IN && handler instanceof IOpticalDataAccessHatch hatch) {
                    opticalReceptors.add(hatch);
                } else if (handlerIO == IO.OUT && handler instanceof IOpticalDataAccessHatch hatch) {
                    opticalTransmitters.add(hatch);
                } else if (handlerIO == IO.IN && handler instanceof IDataAccessHatch hatch) {
                    regulars.add(hatch);
                }
            }
        }
        this.energyContainer = new EnergyContainerList(energyContainers);
        this.energyUsage = calculateEnergyUsage(opticalReceptors, opticalTransmitters, regulars);

        updatePowerSubscription();
    }

    protected int calculateEnergyUsage(Set<IOpticalDataAccessHatch> opticalReceptors, Set<IOpticalDataAccessHatch> opticalTransmitters, Set<IDataAccessHatch> regularHatches) {
        int receivers = opticalReceptors.size();
        int transmitters = opticalTransmitters.size();
        int regulars = regularHatches.size();

        int dataHatches = receivers + transmitters + regulars;
        int tier = receivers > 0 ? GTValues.LuV : GTValues.EV;
        return GTValues.VA[tier] * dataHatches;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.energyContainer = new EnergyContainerList(new ArrayList<>());
        this.energyUsage = 0;
        updatePowerSubscription();
    }

    protected void updatePowerSubscription() {
        // do preheat logic for heat cool down and charge internal energy container
        if (energyUsage > 0 || (energyContainer.getEnergyStored() < energyContainer.getEnergyCapacity())) {
            powerSubs = subscribeServerTick(powerSubs, this::tickPower);
        } else if (powerSubs != null) {
            powerSubs.unsubscribe();
            powerSubs = null;
        }
    }

    protected void tickPower() {
        int energyToConsume = this.getEnergyUsage();
        boolean hasMaintenance = ConfigHolder.INSTANCE.machines.enableMaintenance;
        if (hasMaintenance) {
            // 10% more energy per maintenance problem
            IMaintenanceMachine maintenanceMachine = getTraits().stream().filter(IMaintenanceMachine.class::isInstance).map(IMaintenanceMachine.class::cast).findAny().orElse(null);
            energyToConsume += maintenanceMachine != null ? maintenanceMachine.getNumMaintenanceProblems() * energyToConsume / 10 : 6 * energyToConsume / 10;
        }

        if (this.hasNotEnoughEnergy && (energyContainer.getInputVoltage() * energyContainer.getInputAmperage() * 20) > 19L * energyToConsume) {
            this.hasNotEnoughEnergy = false;
        }

        if (this.energyContainer.getEnergyStored() >= energyToConsume) {
            if (!hasNotEnoughEnergy) {
                long consumed = this.energyContainer.removeEnergy(energyToConsume);
                if (consumed == -energyToConsume) {
                    getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
                } else {
                    this.hasNotEnoughEnergy = true;
                    getRecipeLogic().setStatus(RecipeLogic.Status.WAITING);
                }
            }
        } else {
            this.hasNotEnoughEnergy = true;
            getRecipeLogic().setStatus(RecipeLogic.Status.WAITING);
        }
        updatePowerSubscription();
    }
}
