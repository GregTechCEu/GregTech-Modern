package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.ILaserContainer;
import com.gregtechceu.gtceu.api.capability.impl.ActiveTransformerWrapper;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.LaserRecipeCapability;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
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
    @Nullable
    private ILaserContainer laserInContainer;
    protected ConditionalSubscriptionHandler converterSubscription;

    public ActiveTransformerMachine(IMachineBlockEntity holder) {
        super(holder);
        this.energyOutputContainer = new EnergyContainerList(new ArrayList<>());
        this.wrapper = null;
        this.laserInContainer = null;
        this.converterSubscription = new ConditionalSubscriptionHandler(this, this::convertEnergyTick, this::isSubscriptionActive);
    }

    public void convertEnergyTick() {
        getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
        if (wrapper == null || this.energyOutputContainer.getEnergyCapacity() == 0) {
            converterSubscription.updateSubscription();
            return;
        }

        if (isWorkingEnabled()) {
            wrapper.removeEnergy(energyOutputContainer.addEnergy(wrapper.getEnergyStored()));
        }
        converterSubscription.updateSubscription();
    }

    protected boolean isSubscriptionActive() {
        if (laserInContainer != null && laserInContainer.getEnergyStored() > 0)
            return true;

        if (energyOutputContainer == null)
            return false;

        if (energyOutputContainer.getEnergyStored() <= 0)
            return false;

        return energyOutputContainer.getEnergyStored() < energyOutputContainer.getEnergyCapacity();
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
            if (io == IO.NONE) continue;
            for (var handler : part.getRecipeHandlers()) {
                var handlerIO = handler.getHandlerIO();
                // If IO not compatible
                if (io != IO.BOTH && handlerIO != IO.BOTH && io != handlerIO) continue;
                if (handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer container) {
                    if (handlerIO == IO.IN) {
                        inputEnergy.add(container);
                    } else if (handlerIO == IO.OUT) {
                        outputEnergy.add(container);
                    }
                    traitSubscriptions.add(handler.addChangedListener(converterSubscription::updateSubscription));
                } else if (handler.getCapability() == LaserRecipeCapability.CAP && handler instanceof ILaserContainer container) {
                    if (handlerIO == IO.IN) {
                        inputLaser.add(container);
                    } else if (handlerIO == IO.OUT) {
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

        if (outputEnergy.isEmpty() && inputEnergy.isEmpty()) {
            return;
        }

        energyOutputContainer = new EnergyContainerList(outputEnergy);
        if (inputLaser.size() == 1) {
            laserInContainer = inputLaser.get(0);
        }

        wrapper = new ActiveTransformerWrapper(new EnergyContainerList(inputEnergy), laserInContainer);
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
