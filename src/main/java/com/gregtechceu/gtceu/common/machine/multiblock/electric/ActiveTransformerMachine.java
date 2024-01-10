package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.api.pattern.Predicates.abilities;

public class ActiveTransformerMachine extends WorkableElectricMultiblockMachine implements IControllable {


    private IEnergyContainer powerOutput;
    private IEnergyContainer powerInput;
    protected ConditionalSubscriptionHandler converterSubscription;

    public ActiveTransformerMachine(IMachineBlockEntity holder) {
        super(holder);
        this.powerOutput = new EnergyContainerList(new ArrayList<>());
        this.powerInput = new EnergyContainerList(new ArrayList<>());
        this.converterSubscription = new ConditionalSubscriptionHandler(this, this::convertEnergyTick, this::isSubscriptionActive);
    }

    public void convertEnergyTick() {
        getRecipeLogic().setStatus(RecipeLogic.Status.WORKING);
        if (isWorkingEnabled()) {
            long canDrain = powerInput.getEnergyStored();
            long totalDrained = powerOutput.changeEnergy(canDrain);
            powerInput.removeEnergy(totalDrained);
        }
        converterSubscription.updateSubscription();
    }

    protected boolean isSubscriptionActive() {
        if (powerInput != null && powerInput.getEnergyStored() > 0)
            return true;

        if (powerOutput == null)
            return false;

        if (powerOutput.getEnergyStored() <= 0)
            return false;

        return powerOutput.getEnergyStored() < powerOutput.getEnergyCapacity();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        // capture all energy containers
        List<IEnergyContainer> powerInput = new ArrayList<>();
        List<IEnergyContainer> powerOutput = new ArrayList<>();
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
                        powerInput.add(container);
                    } else if (handlerIO == IO.OUT) {
                        powerOutput.add(container);
                    }
                    traitSubscriptions.add(handler.addChangedListener(converterSubscription::updateSubscription));
                }
            }
        }

        // Invalidate the structure if there is not at least one output and one input
        if (powerInput.isEmpty() || powerOutput.isEmpty()) {
            this.onStructureInvalid();
        }

        this.powerOutput = new EnergyContainerList(powerOutput);
        this.powerInput = new EnergyContainerList(powerInput);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.powerOutput = new EnergyContainerList(new ArrayList<>());
        this.powerInput = new EnergyContainerList(new ArrayList<>());
        getRecipeLogic().setStatus(RecipeLogic.Status.IDLE);
    }

    public static TraceabilityPredicate getHatchPredicates() {
        return abilities(PartAbility.INPUT_ENERGY).setPreviewCount(1)
                .or(abilities(PartAbility.OUTPUT_ENERGY).setPreviewCount(2))
                .or(abilities(PartAbility.INPUT_LASER).setPreviewCount(1))
                .or(abilities(PartAbility.OUTPUT_LASER).setPreviewCount(1));
    }
}
