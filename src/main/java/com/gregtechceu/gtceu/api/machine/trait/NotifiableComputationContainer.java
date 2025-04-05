package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationReceiver;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NotifiableComputationContainer extends NotifiableRecipeHandlerTrait<Integer>
                                            implements IOpticalComputationHatch, IOpticalComputationReceiver {

    @Getter
    protected IO handlerIO;
    @Getter
    protected boolean transmitter;

    protected long lastTimeStamp;
    private int currentOutputCwu = 0, lastOutputCwu = 0;

    public NotifiableComputationContainer(MetaMachine machine, IO handlerIO, boolean transmitter) {
        super(machine);
        this.handlerIO = handlerIO;
        this.transmitter = transmitter;

        this.lastTimeStamp = Long.MIN_VALUE;
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        var latestTimeStamp = getMachine().getOffsetTimer();
        if (lastTimeStamp < latestTimeStamp) {
            lastOutputCwu = currentOutputCwu;
            currentOutputCwu = 0;
            lastTimeStamp = latestTimeStamp;
        }

        seen.add(this);
        if (handlerIO == IO.IN) {
            if (isTransmitter()) {
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                if (machine instanceof IOpticalComputationProvider provider) {
                    return provider.requestCWUt(cwut, simulate, seen);
                } else if (machine instanceof IMultiPart part) {
                    if (!part.isFormed()) {
                        return 0;
                    }
                    for (IMultiController controller : part.getControllers()) {
                        if (controller instanceof IOpticalComputationProvider provider) {
                            return provider.requestCWUt(cwut, simulate, seen);
                        }
                        for (MachineTrait trait : controller.self().getTraits()) {
                            if (trait instanceof IOpticalComputationProvider provider) {
                                return provider.requestCWUt(cwut, simulate, seen);
                            }
                        }
                    }
                    GTCEu.LOGGER
                            .error("NotifiableComputationContainer could request CWU/t from its machine's controller!");
                    return 0;
                } else {
                    GTCEu.LOGGER.error("NotifiableComputationContainer could request CWU/t from its machine!");
                    return 0;
                }
            } else {
                // Ask the attached Transmitter hatch, if it exists
                IOpticalComputationProvider provider = getOpticalNetProvider();
                if (provider == null) return 0;
                return provider.requestCWUt(cwut, simulate, seen);
            }
        } else {
            lastOutputCwu = lastOutputCwu - cwut;
            return Math.min(lastOutputCwu, cwut);
        }
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        if (handlerIO == IO.IN) {
            if (isTransmitter()) {
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                if (machine instanceof IOpticalComputationProvider provider) {
                    return provider.getMaxCWUt(seen);
                } else if (machine instanceof IMultiPart part) {
                    if (!part.isFormed()) {
                        return 0;
                    }
                    for (IMultiController controller : part.getControllers()) {
                        if (!controller.isFormed()) {
                            continue;
                        }
                        if (controller instanceof IOpticalComputationProvider provider) {
                            return provider.getMaxCWUt(seen);
                        }
                        for (MachineTrait trait : controller.self().getTraits()) {
                            if (trait instanceof IOpticalComputationProvider provider) {
                                return provider.getMaxCWUt(seen);
                            }
                        }
                    }
                    GTCEu.LOGGER.error(
                            "NotifiableComputationContainer could not get maximum CWU/t from its machine's controller!");
                    return 0;
                } else {
                    GTCEu.LOGGER.error("NotifiableComputationContainer could not get maximum CWU/t from its machine!");
                    return 0;
                }
            } else {
                // Ask the attached Transmitter hatch, if it exists
                IOpticalComputationProvider provider = getOpticalNetProvider();
                if (provider == null) return 0;
                return provider.getMaxCWUt(seen);
            }
        } else {
            return lastOutputCwu;
        }
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        if (handlerIO == IO.IN) {
            if (isTransmitter()) {
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                if (machine instanceof IOpticalComputationProvider provider) {
                    return provider.canBridge(seen);
                } else if (machine instanceof IMultiPart part) {
                    if (!part.isFormed()) {
                        return false;
                    }
                    for (IMultiController controller : part.getControllers()) {
                        if (!controller.isFormed()) {
                            continue;
                        }
                        if (controller instanceof IOpticalComputationProvider provider) {
                            return provider.canBridge(seen);
                        }
                        for (MachineTrait trait : controller.self().getTraits()) {
                            if (trait instanceof IOpticalComputationProvider provider) {
                                return provider.canBridge(seen);
                            }
                        }
                    }
                    GTCEu.LOGGER.error(
                            "NotifiableComputationContainer could not test bridge status of its machine's controller!");
                    return false;
                } else {
                    GTCEu.LOGGER.error("NotifiableComputationContainer could not test bridge status of its machine!");
                    return false;
                }
            } else {
                // Ask the attached Transmitter hatch, if it exists
                IOpticalComputationProvider provider = getOpticalNetProvider();
                if (provider == null) return true; // nothing found, so don't report a problem, just pass quietly
                return provider.canBridge(seen);
            }
        } else {
            return false;
        }
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left,
                                           boolean simulate) {
        IOpticalComputationProvider provider = getOpticalNetProvider();
        if (provider == null) return left;

        int sum = left.stream().mapToInt(Integer::intValue).sum();
        if (io == IO.IN) {
            int availableCWUt = requestCWUt(Integer.MAX_VALUE, true);
            if (availableCWUt >= sum) {
                if (recipe.data.getBoolean("duration_is_total_cwu")) {
                    int drawn = provider.requestCWUt(availableCWUt, simulate);
                    if (!simulate) {
                        if (machine instanceof IRecipeLogicMachine rlm) {
                            // first, remove the progress the recipe logic adds.
                            rlm.getRecipeLogic().progress -= 1;
                            rlm.getRecipeLogic().progress += drawn;
                        } else if (machine instanceof IMultiPart multiPart) {
                            for (IMultiController controller : multiPart.getControllers()) {
                                if (controller instanceof IRecipeLogicMachine rlm) {
                                    rlm.getRecipeLogic().progress -= 1;
                                    rlm.getRecipeLogic().progress += drawn;
                                }
                            }
                        }
                    }
                    sum -= drawn;
                } else {
                    sum -= provider.requestCWUt(sum, simulate);
                }
            }
        } else if (io == IO.OUT) {
            int canInput = this.getMaxCWUt() - this.lastOutputCwu;
            if (!simulate) {
                this.currentOutputCwu = Math.min(canInput, sum);
            }
            sum = sum - canInput;
        }
        return sum <= 0 ? null : Collections.singletonList(sum);
    }

    @Override
    public @NotNull List<Object> getContents() {
        return List.of(lastOutputCwu);
    }

    @Override
    public double getTotalContentAmount() {
        return lastOutputCwu;
    }

    @Override
    public RecipeCapability<Integer> getCapability() {
        return CWURecipeCapability.CAP;
    }

    @Nullable
    @Override
    public IOpticalComputationProvider getComputationProvider() {
        if (this.handlerIO.support(IO.OUT)) {
            return this;
        }
        if (machine instanceof IOpticalComputationReceiver receiver) {
            return receiver.getComputationProvider();
        } else if (machine instanceof IOpticalComputationProvider provider) {
            return provider;
        } else if (machine instanceof IRecipeCapabilityHolder recipeCapabilityHolder) {
            var cwuCap = recipeCapabilityHolder.getCapabilitiesFlat(IO.IN, CWURecipeCapability.CAP);
            if (!cwuCap.isEmpty()) {
                var provider = (IOpticalComputationProvider) cwuCap.get(0);
                if (provider != this) {
                    return provider;
                }
            }
        }
        for (Direction direction : GTUtil.DIRECTIONS) {
            BlockEntity blockEntity = machine.getLevel().getBlockEntity(machine.getPos().relative(direction));
            if (blockEntity == null) continue;

            // noinspection DataFlowIssue can be null just fine.
            IOpticalComputationProvider provider = blockEntity
                    .getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, direction.getOpposite()).orElse(null);
            // noinspection ConstantValue can be null because above.
            if (provider != null && provider != this) {
                return provider;
            }
        }
        return null;
    }

    @Nullable
    private IOpticalComputationProvider getOpticalNetProvider() {
        for (Direction direction : GTUtil.DIRECTIONS) {
            BlockEntity blockEntity = machine.getLevel().getBlockEntity(machine.getPos().relative(direction));
            if (blockEntity instanceof OpticalPipeBlockEntity) {
                return blockEntity.getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, direction.getOpposite())
                        .orElse(null);
            }
        }
        return null;
    }
}
