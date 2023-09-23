package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.api.capability.impl.ActiveTransformerWrapper;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.LaserRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.common.data.GTMachines;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;
import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;

public class ActiveTransformerMachine extends WorkableElectricMultiblockMachine implements IControllable {

    private IEnergyContainer energyOutputContainer;
    private ActiveTransformerWrapper wrapper;
    private ILaserContainer laserInContainer;
    @Nullable
    protected TickableSubscription convertSubs;

    public ActiveTransformerMachine(IMachineBlockEntity holder) {
        super(holder);
        this.energyOutputContainer = new EnergyContainerList(new ArrayList<>());
        this.wrapper = null;
        this.laserInContainer = null;
    }
    
    public void convertEnergyTick() {
        getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
        if (wrapper == null || this.energyOutputContainer.getEnergyCapacity() == 0) {
            updateConverterSubscription();
            return;
        }

        if (isWorkingEnabled()) {
            wrapper.removeEnergy(energyOutputContainer.addEnergy(wrapper.getEnergyStored()));
        }
        updateConverterSubscription();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // capture all energy containers
        List<IEnergyContainer> inputEnergy = new ArrayList<>();
        List<IEnergyContainer> outputEnergy = new ArrayList<>();
        List<ILaserContainer> inputLaser = new ArrayList<>();
        List<ILaserContainer> outputLaser = new ArrayList<>();
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext().getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if(io == IO.NONE) continue;
            for (var handler : part.getRecipeHandlers()) {
                // If IO not compatible
                if (io != IO.BOTH && handler.getHandlerIO() != IO.BOTH && io != handler.getHandlerIO()) continue;
                if (handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) {
                    if (io == IO.IN) {
                        inputEnergy.add(container);
                    } else if (io == IO.OUT) {
                        outputEnergy.add(container);
                    }
                    traitSubscriptions.add(handler.addChangedListener(this::updateConverterSubscription));
                } else if (handler.getCapability() == LaserRecipeCapability.CAP && handler instanceof ILaserContainer container) {
                    if (io == IO.IN) {
                        inputLaser.add(container);
                    } else if (io == IO.OUT) {
                        outputLaser.add(container);
                    }
                }
            }
        }

        // Invalidate the structure if there is not at least one output and one input
        if (inputEnergy.size() + inputLaser.size() == 0 || outputEnergy.size() + outputLaser.size() == 0) {
            this.onStructureInvalid();
            return;
        }

        if (outputEnergy.size() == 0 && inputEnergy.size() == 0) {
            return;
        }

        energyOutputContainer = new EnergyContainerList(outputEnergy);
        if (inputLaser.size() == 1) {
            laserInContainer = inputLaser.get(0);
        }

        wrapper = new ActiveTransformerWrapper(new EnergyContainerList(inputEnergy), laserInContainer);
    }

    protected void updateConverterSubscription() {
        if (laserInContainer.getEnergyStored() > 0 || (energyOutputContainer != null && energyOutputContainer.getEnergyStored() > 0 && energyOutputContainer.getEnergyStored() < energyOutputContainer.getEnergyCapacity())) {
            convertSubs = subscribeServerTick(convertSubs, this::convertEnergyTick);
        } else if (convertSubs != null) {
            convertSubs.unsubscribe();
            convertSubs = null;
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.energyOutputContainer = new EnergyContainerList(new ArrayList<>());
        this.wrapper = null;
        this.laserInContainer = null;
        getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
    }

    public static TraceabilityPredicate getHatchPredicates() {
        return abilities(PartAbility.INPUT_ENERGY).setPreviewCount(2)
                .or(abilities(PartAbility.OUTPUT_ENERGY).setPreviewCount(2))
                .or(abilities(PartAbility.INPUT_LASER).setMaxGlobalLimited(1))
                .or(abilities(PartAbility.OUTPUT_LASER).setMaxGlobalLimited(1))
                // Disallow the config maintenance hatch because that would probably break the conservation of energy
                .or(blocks(GTMachines.MAINTENANCE_HATCH.getBlock(),
                        GTMachines.AUTO_MAINTENANCE_HATCH.getBlock(), GTMachines.CLEANING_MAINTENANCE_HATCH.getBlock()).setExactLimit(1));
    }

    public ILaserContainer getWrapper() {
        if (wrapper != null) {
            return wrapper;
        } else if (isFormed) {
            var lasers = this.getCapabilitiesProxy().get(IO.IN, LaserRecipeCapability.CAP);
            if (lasers != null && lasers.size() == 1) {
                return lasers.stream().filter(ILaserContainer.class::isInstance).map(ILaserContainer.class::cast).findFirst().get();
            }
            lasers = this.getCapabilitiesProxy().get(IO.BOTH, LaserRecipeCapability.CAP);
            if (lasers != null && lasers.size() == 1) {
                return lasers.stream().filter(ILaserContainer.class::isInstance).map(ILaserContainer.class::cast).findFirst().get();
            }
        }
        return null;
    }
}
