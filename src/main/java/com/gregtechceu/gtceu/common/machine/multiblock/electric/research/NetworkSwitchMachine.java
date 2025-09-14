package com.gregtechceu.gtceu.common.machine.multiblock.electric.research;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableComputationContainer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkSwitchMachine extends DataBankMachine implements IOpticalComputationProvider {

    public static final int EUT_PER_HATCH = GTValues.VA[GTValues.IV];

    private final MultipleComputationHandler computationHandler = new MultipleComputationHandler(this);

    public NetworkSwitchMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
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
        List<IOpticalComputationHatch> receivers = new ArrayList<>();
        List<IOpticalComputationHatch> transmitters = new ArrayList<>();
        for (var part : this.getParts()) {
            Block block = part.self().getBlockState().getBlock();
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
    public void onStructureInvalid() {
        super.onStructureInvalid();
        computationHandler.reset();
    }

    @Override
    public int getEnergyUsage() {
        return isFormed() ? computationHandler.getEUt() : 0;
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return isActive() && !getRecipeLogic().isWaiting() ? computationHandler.requestCWUt(cwut, simulate, seen) : 0;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return isFormed() ? computationHandler.getMaxCWUt(seen) : 0;
    }

    // allows chaining Network Switches together
    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return true;
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
                .addComputationUsageLine(computationHandler.getMaxCWUtForDisplay())
                .addWorkingStatusLine();
    }

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

        private boolean tickSaturated;
        private long timerCWUt = -1;

        public MultipleComputationHandler(MetaMachine machine) {
            super(machine, IO.IN, false);
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
        public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
            if (seen.contains(this)) return 0;
            // The max CWU/t that this Network Switch can provide, combining all its inputs.
            seen.add(this);

            if (cwut == 0) return 0;

            // Exit early if this Network Switch has already provided all available CWUt on its subnetwork for this tick
            long timer = NetworkSwitchMachine.this.getOffsetTimer();
            if (timerCWUt == timer) {
                if (tickSaturated) {
                    return 0;
                }
            } else {
                // First call this tick, reset saturation
                timerCWUt = timer;
                tickSaturated = false;
            }

            Collection<IOpticalComputationProvider> bridgeSeen = new ArrayList<>(seen);
            int allocatedCWUt = 0;
            for (var provider : providers) {
                if (!provider.canBridge(bridgeSeen)) continue;
                int allocated = provider.requestCWUt(cwut, simulate, seen);
                allocatedCWUt += allocated;
                cwut -= allocated;
                if (cwut == 0) break;
            }

            if (!simulate && allocatedCWUt == 0) {
                // No computation left to give, remember this for subsequent calls this tick
                tickSaturated = true;
            }

            return allocatedCWUt;
        }

        public int getMaxCWUtForDisplay() {
            Collection<IOpticalComputationProvider> seen = new ArrayList<>();
            // The max CWU/t that this Network Switch can provide, combining all its inputs.
            seen.add(this);
            Collection<IOpticalComputationProvider> bridgeSeen = new ArrayList<>(seen);
            int maximumCWUt = 0;
            for (var provider : providers) {
                if (!provider.canBridge(bridgeSeen)) continue;
                maximumCWUt += provider.getMaxCWUt(seen);
            }
            return maximumCWUt;
        }

        public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
            if (seen.contains(this)) return 0;
            // The max CWU/t that this Network Switch can provide, combining all its inputs.
            seen.add(this);
            Collection<IOpticalComputationProvider> bridgeSeen = new ArrayList<>(seen);
            int maximumCWUt = 0;
            for (var provider : providers) {
                if (!provider.canBridge(bridgeSeen)) continue;
                maximumCWUt += provider.getMaxCWUt(seen);
            }
            return maximumCWUt;
        }

        @Override
        public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
            if (seen.contains(this)) return false;
            seen.add(this);
            for (var provider : providers) {
                if (provider.canBridge(seen)) {
                    return true;
                }
            }
            return false;
        }

        /** Test if any of the provider hatches do not allow bridging */
        private boolean hasNonBridgingConnections() {
            Collection<IOpticalComputationProvider> seen = new ArrayList<>();
            for (var provider : providers) {
                if (!provider.canBridge(seen)) {
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
