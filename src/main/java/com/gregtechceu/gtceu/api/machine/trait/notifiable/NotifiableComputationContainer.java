package com.gregtechceu.gtceu.api.machine.trait.notifiable;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationProvider;
import com.gregtechceu.gtceu.api.capability.IOpticalComputationReceiver;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NotifiableComputationContainer extends NotifiableRecipeHandlerTrait<Integer>
                                            implements IOpticalComputationHatch, IOpticalComputationReceiver {

    @Getter
    protected IO handlerIO;
    @Getter
    protected boolean transmitter;

    private ComputationTransferContext transferContext = ComputationTransferContext.create();

    private record ComputationTransferContext(long lastTimeStamp, int currentOutputCwu, int lastOutputCwu) {

        public static ComputationTransferContext create() {
            return new ComputationTransferContext(Long.MIN_VALUE, 0, 0);
        }

        public ComputationTransferContext conditionalTimedUpdate(MetaMachine machine) {
            var latestTimeStamp = machine.getOffsetTimer();
            if (lastTimeStamp < latestTimeStamp) {
                return new ComputationTransferContext(latestTimeStamp, 0, currentOutputCwu());
            }
            return this;
        }

        public ComputationTransferContext subtractLastOutput(int transferred) {
            return new ComputationTransferContext(lastTimeStamp(), currentOutputCwu(), lastOutputCwu() - transferred);
        }

        public ComputationTransferContext setOutput(int newOutputCwu) {
            return new ComputationTransferContext(lastTimeStamp(), newOutputCwu, lastOutputCwu());
        }
    }

    public NotifiableComputationContainer(IO handlerIO, boolean transmitter) {
        super();
        this.handlerIO = handlerIO;
        this.transmitter = transmitter;
    }

    @Override
    public int requestCWUt(int cwut, boolean simulate, Map<IOpticalComputationProvider, Object> seenWithContext) {
        ComputationTransferContext localTransferContext = this.transferContext.conditionalTimedUpdate(getMachine());
        if (!simulate) {
            this.transferContext = localTransferContext;
        }

        seenWithContext.put(this, localTransferContext);
        if (handlerIO == IO.IN) {
            if (isTransmitter()) {
                var machine = getMachine();
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                if (machine instanceof IOpticalComputationProvider provider) {
                    return provider.requestCWUt(cwut, simulate, seenWithContext);
                } else if (machine instanceof MultiblockPartMachine part) {
                    if (!part.isFormed()) {
                        return 0;
                    }
                    for (MultiblockControllerMachine controller : part.getControllers()) {
                        if (controller instanceof IOpticalComputationProvider provider) {
                            return provider.requestCWUt(cwut, simulate, seenWithContext);
                        }
                        for (MachineTrait trait : controller.getAllTraits()) {
                            if (trait instanceof IOpticalComputationProvider provider) {
                                return provider.requestCWUt(cwut, simulate, seenWithContext);
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
                return provider.requestCWUt(cwut, simulate, seenWithContext);
            }
        } else {
            localTransferContext = localTransferContext.subtractLastOutput(cwut);
            if (!simulate) {
                this.transferContext = localTransferContext;
            }
            return Math.min(localTransferContext.lastOutputCwu(), cwut);
        }
    }

    @Override
    public int getMaxCWUt(Map<IOpticalComputationProvider, Object> seenWithContext) {
        seenWithContext.put(this, this.transferContext);
        if (handlerIO == IO.IN) {
            if (isTransmitter()) {
                var machine = getMachine();
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                if (machine instanceof IOpticalComputationProvider provider) {
                    return provider.getMaxCWUt(seenWithContext);
                } else if (machine instanceof MultiblockPartMachine part) {
                    if (!part.isFormed()) {
                        return 0;
                    }
                    for (MultiblockControllerMachine controller : part.getControllers()) {
                        if (!controller.isFormed()) {
                            continue;
                        }
                        if (controller instanceof IOpticalComputationProvider provider) {
                            return provider.getMaxCWUt(seenWithContext);
                        }
                        for (MachineTrait trait : controller.getAllTraits()) {
                            if (trait instanceof IOpticalComputationProvider provider) {
                                return provider.getMaxCWUt(seenWithContext);
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
                return provider.getMaxCWUt(seenWithContext);
            }
        } else {
            return Preconditions.checkNotNull((ComputationTransferContext) seenWithContext.get(this)).lastOutputCwu();
        }
    }

    @Override
    public boolean canBridge(Map<IOpticalComputationProvider, Object> seenWithContext) {
        seenWithContext.put(this, this.transferContext);
        if (handlerIO == IO.IN) {
            if (isTransmitter()) {
                var machine = getMachine();
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                if (machine instanceof IOpticalComputationProvider provider) {
                    return provider.canBridge(seenWithContext);
                } else if (machine instanceof MultiblockPartMachine part) {
                    if (!part.isFormed()) {
                        return false;
                    }
                    for (MultiblockControllerMachine controller : part.getControllers()) {
                        if (!controller.isFormed()) {
                            continue;
                        }
                        if (controller instanceof IOpticalComputationProvider provider) {
                            return provider.canBridge(seenWithContext);
                        }
                        for (MachineTrait trait : controller.getAllTraits()) {
                            if (trait instanceof IOpticalComputationProvider provider) {
                                return provider.canBridge(seenWithContext);
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
                return provider.canBridge(seenWithContext);
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
                        var machine = getMachine();
                        if (machine instanceof IRecipeLogicMachine rlm) {
                            // remove the progress the recipe logic adds.
                            rlm.getRecipeLogic().setProgressDelta(drawn - 1);
                        } else if (machine instanceof MultiblockPartMachine multiPart) {
                            for (MultiblockControllerMachine controller : multiPart.getControllers()) {
                                if (controller instanceof IRecipeLogicMachine rlm) {
                                    rlm.getRecipeLogic().setProgressDelta(drawn - 1);
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
            int canInput = this.getMaxCWUt() - this.transferContext.lastOutputCwu();
            if (!simulate) {
                int newOutputCwu = Math.min(canInput, sum);
                this.transferContext = this.transferContext.setOutput(newOutputCwu);
            }
            sum = sum - canInput;
        }
        return sum <= 0 ? Collections.emptyList() : Collections.singletonList(sum);
    }

    @Override
    public List<Object> getContents() {
        return List.of(this.transferContext.lastOutputCwu());
    }

    @Override
    public double getTotalContentAmount() {
        return this.transferContext.lastOutputCwu();
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
        var machine = getMachine();
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
            BlockEntity blockEntity = getLevel().getBlockEntity(getBlockPos().relative(direction));
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
            BlockEntity blockEntity = getLevel().getBlockEntity(getBlockPos().relative(direction));
            if (blockEntity instanceof OpticalPipeBlockEntity) {
                // noinspection DataFlowIssue can be null just fine.
                IOpticalComputationProvider provider = blockEntity
                        .getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, direction.getOpposite())
                        .orElse(null);
                // noinspection ConstantValue can be null because above.
                if (provider != null) return provider;
            }
        }
        return null;
    }
}
