package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.trait.notifiable.NotifiableComputationContainer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkSwitchMachine extends DataBankMachine implements IOpticalComputationProvider {

    public static final int EUT_PER_HATCH = GTValues.VA[GTValues.IV];

    private final MultipleComputationHandler computationHandler;

    public NetworkSwitchMachine(BlockEntityCreationInfo info) {
        super(info);
        computationHandler = attachTrait(new MultipleComputationHandler());
    }

    @Override
    protected int calculateEnergyUsage() {
        int receivers = 0;
        int transmitters = 0;
        for (var part : this.getParts()) {
            Block block = part.getBlockState().getBlock();
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
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        List<IOpticalComputationHatch> receivers = new ArrayList<>();
        List<IOpticalComputationHatch> transmitters = new ArrayList<>();
        for (var part : this.getParts()) {
            Block block = part.getBlockState().getBlock();
            List<IOpticalComputationHatch> list;
            if (PartAbility.COMPUTATION_DATA_RECEPTION.isApplicable(block)) {
                list = receivers;
            } else if (PartAbility.COMPUTATION_DATA_TRANSMISSION.isApplicable(block)) {
                list = transmitters;
            } else {
                continue;
            }
            if (part instanceof IOpticalComputationHatch hatch) {
                list.add(hatch);
            } else {
                var handlerLists = part.getRecipeHandlers();
                for (var handlerList : handlerLists) {
                    for (var cwu : handlerList.getCapability(CWURecipeCapability.CAP)) {
                        if (cwu instanceof IOpticalComputationHatch hatch) {
                            list.add(hatch);
                        }
                    }
                }
            }
        }
        computationHandler.onStructureForm(receivers, transmitters);
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        computationHandler.reset();
    }

    @Override
    public int getEnergyUsage() {
        return isFormed() ? computationHandler.getEUt() : 0;
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, Set<IOpticalComputationProvider> seen,
                           Map<IOpticalComputationProvider, Object> simulationState) {
        seen.add(this);
        return isActive() && !getRecipeLogic().isWaiting() ?
                computationHandler.requestCWUt(cwut, simulate, seen, simulationState) : 0;
    }

    @Override
    public int getMaxCWUt(Set<IOpticalComputationProvider> seen,
                          Map<IOpticalComputationProvider, Object> simulationState) {
        seen.add(this);
        return isFormed() ? computationHandler.getMaxCWUt(seen, simulationState) : 0;
    }

    // allows chaining Network Switches together
    @Override
    public boolean canBridge(Set<IOpticalComputationProvider> seen,
                             Map<IOpticalComputationProvider, Object> simulationState) {
        seen.add(this);
        return true;
    }

    // @Override
    // public void addDisplayText(List<Component> textList) {
    // MultiblockDisplayText.builder(textList, getDefaultPatternState())
    // .setWorkingStatus(true, isActive() && isWorkingEnabled()) // transform into two-state system for display
    // .setWorkingStatusKeys(
    // "gtceu.multiblock.idling",
    // "gtceu.multiblock.idling",
    // "gtceu.multiblock.data_bank.providing")
    // .addEnergyUsageExactLine(getEnergyUsage())
    // .addComputationUsageLine(computationHandler.getMaxCWUtForDisplay())
    // .addWorkingStatusLine();
    // }

    /*
     * @Override
     * protected void addWarningText(List<Component> textList) {
     * super.addWarningText(textList);
     * if (isFormed() && computationHandler.hasNonBridgingConnections()) {
     * textList.add(Component.translatable("gtceu.multiblock.computation.non_bridging.detailed").withStyle(
     * ChatFormatting.YELLOW));
     * }
     * }
     */

    /** Handles computation load across multiple receivers and to multiple transmitters. */
    private class MultipleComputationHandler extends NotifiableComputationContainer {

        // providers in the NS provide distributable computation to the NS
        private final Set<IOpticalComputationHatch> providers = new ObjectOpenHashSet<>();
        // transmitters in the NS give computation to other multis
        private final Set<IOpticalComputationHatch> transmitters = new ObjectOpenHashSet<>();

        /** The EU/t cost of this Network Switch given the attached providers and transmitters. */
        @Getter(value = AccessLevel.PRIVATE)
        private int EUt;

        private SwitchTransferContext transferContext = SwitchTransferContext.create();

        private record SwitchTransferContext(long timerCWUt, boolean tickSaturated) {

            public static SwitchTransferContext create() {
                return new SwitchTransferContext(-1, false);
            }

            public SwitchTransferContext conditionalTimedUpdate(MetaMachine machine) {
                long timer = machine.getOffsetTimer();
                if (timerCWUt != timer) {
                    return new SwitchTransferContext(timer, false);
                }
                return this;
            }

            public SwitchTransferContext markSaturated() {
                return new SwitchTransferContext(timerCWUt, true);
            }
        }

        public MultipleComputationHandler() {
            super(IO.IN, false);
        }

        private void onStructureForm(Collection<IOpticalComputationHatch> providers,
                                     Collection<IOpticalComputationHatch> transmitters) {
            reset();
            this.providers.addAll(providers);
            this.transmitters.addAll(transmitters);
            this.EUt = (providers.size() + transmitters.size()) * EUT_PER_HATCH;
        }

        private void reset() {
            providers.clear();
            transmitters.clear();
            EUt = 0;
        }

        @Override
        public int requestCWUt(int cwut, boolean simulate, Set<IOpticalComputationProvider> seen,
                               Map<IOpticalComputationProvider, Object> simulationState) {
            if (seen.contains(this)) return 0;
            seen.add(this);

            // The max CWU/t that this Network Switch can provide, combining all its inputs.
            SwitchTransferContext localTransferContext = (SwitchTransferContext) simulationState.getOrDefault(this,
                    this.transferContext);
            localTransferContext = localTransferContext.conditionalTimedUpdate(getMachine());
            if (!simulate) {
                this.transferContext = localTransferContext;
            } else {
                simulationState.put(this, localTransferContext);
            }

            // Exit early if this Network Switch has already provided all available CWUt on its subnetwork for this tick
            if (cwut == 0 || localTransferContext.tickSaturated()) {
                if (!simulate) {
                    this.transferContext = localTransferContext;
                } else {
                    simulationState.put(this, localTransferContext);
                }
                return 0;
            }

            Set<IOpticalComputationProvider> bridgeSeen = new HashSet<>(seen);
            int allocatedCWUt = 0;
            for (var provider : providers) {
                if (!provider.canBridge(bridgeSeen, simulationState)) continue;
                int allocated = provider.requestCWUt(cwut, simulate, seen, simulationState);
                allocatedCWUt += allocated;
                cwut -= allocated;
                if (cwut == 0) break;
            }

            if (allocatedCWUt == 0) {
                // No computation left to give, remember this for subsequent calls this tick
                localTransferContext = localTransferContext.markSaturated();
                if (!simulate) {
                    this.transferContext = localTransferContext;
                } else {
                    simulationState.put(this, localTransferContext);
                }
            }

            return allocatedCWUt;
        }

        public int getMaxCWUtForDisplay() {
            Set<IOpticalComputationProvider> seen = new HashSet<>();
            Map<IOpticalComputationProvider, Object> simulationState = new HashMap<>();
            // The max CWU/t that this Network Switch can provide, combining all its inputs.
            seen.add(this);
            Set<IOpticalComputationProvider> bridgeSeen = new HashSet<>(seen);
            int maximumCWUt = 0;
            for (var provider : providers) {
                if (!provider.canBridge(bridgeSeen, simulationState)) continue;
                maximumCWUt += provider.getMaxCWUt(seen, simulationState);
            }
            return maximumCWUt;
        }

        public int getMaxCWUt(Set<IOpticalComputationProvider> seen,
                              Map<IOpticalComputationProvider, Object> simulationState) {
            if (seen.contains(this)) return 0;
            // The max CWU/t that this Network Switch can provide, combining all its inputs.
            seen.add(this);
            Set<IOpticalComputationProvider> bridgeSeen = new HashSet<>(seen);
            int maximumCWUt = 0;
            for (var provider : providers) {
                if (!provider.canBridge(bridgeSeen, simulationState)) continue;
                maximumCWUt += provider.getMaxCWUt(seen, simulationState);
            }
            return maximumCWUt;
        }

        @Override
        public boolean canBridge(Set<IOpticalComputationProvider> seen,
                                 Map<IOpticalComputationProvider, Object> simulationState) {
            if (seen.contains(this)) return false;
            seen.add(this);
            for (var provider : providers) {
                if (provider.canBridge(seen, simulationState)) {
                    return true;
                }
            }
            return false;
        }

        /** Test if any of the provider hatches do not allow bridging */
        private boolean hasNonBridgingConnections() {
            Set<IOpticalComputationProvider> seen = new HashSet<>();
            Map<IOpticalComputationProvider, Object> simulationState = new HashMap<>();
            for (var provider : providers) {
                if (!provider.canBridge(seen, simulationState)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public IOpticalComputationProvider getComputationProvider() {
            return this;
        }
    }
}
