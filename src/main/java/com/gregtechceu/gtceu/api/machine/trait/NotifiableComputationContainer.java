package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.data.IComputationDataAccess;
import com.gregtechceu.gtceu.api.capability.data.IComputationProvider;
import com.gregtechceu.gtceu.api.capability.data.IComputationUser;
import com.gregtechceu.gtceu.api.capability.data.IDataAccess;
import com.gregtechceu.gtceu.api.capability.data.query.DataQueryObject;
import com.gregtechceu.gtceu.api.capability.data.query.IBridgeable;
import com.gregtechceu.gtceu.api.capability.data.query.IComputationQuery;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.reference.WeakHashSet;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.google.common.primitives.Ints;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotifiableComputationContainer extends NotifiableRecipeHandlerTrait<Long>
                                            implements IComputationProvider, IComputationUser, IComputationDataAccess {

    @Getter
    protected IO handlerIO;
    @Getter
    protected boolean transmitter;

    private final WeakHashSet<DataQueryObject> recentQueries = new WeakHashSet<>();

    protected long lastTimeStamp;
    private long currentOutputCwu = 0, lastOutputCwu = 0;

    public NotifiableComputationContainer(MetaMachine machine, IO handlerIO, boolean transmitter) {
        super(machine);
        this.handlerIO = handlerIO;
        this.transmitter = transmitter;

        this.lastTimeStamp = Long.MIN_VALUE;
    }

    @Override
    public boolean accessData(@NotNull DataQueryObject queryObject) {
        if (isTransmitter()) {
            MetaMachine machine = getMachine();
            if (machine instanceof IWorkable workable && !workable.isActive()) return false;

            List<IComputationDataAccess> accesses = new ArrayList<>();
            if (machine instanceof IComputationProvider provider &&
                    queryObject instanceof IComputationQuery cq) {
                cq.registerProvider(provider);
            }
            if (machine instanceof IComputationDataAccess dataAccess) {
                accesses.add(dataAccess);
            }
            if (machine instanceof IMultiPart part) {
                for (IMultiController controller : part.getControllers()) {
                    if (controller instanceof IComputationProvider provider &&
                            queryObject instanceof IComputationQuery cq) {
                        cq.registerProvider(provider);
                    }
                    if (controller instanceof IComputationDataAccess dataAccess) {
                        accesses.add(dataAccess);
                    }
                    for (MachineTrait trait : controller.self().getTraits()) {
                        if (trait instanceof IComputationDataAccess dataAccess) {
                            accesses.add(dataAccess);
                        }
                    }
                }
            }
            if (queryObject instanceof IBridgeable bridgeable && accesses.size() > 1) {
                bridgeable.setBridged();
            }
            return IDataAccess.accessData(accesses, queryObject);
        } else {
            Direction front = machine.getFrontFacing();
            IDataAccess access = GTCapabilityHelper.getDataAccess(machine.getLevel(), machine.getPos().relative(front),
                    front.getOpposite());
            if (queryObject.traverseTo(access)) return access.accessData(queryObject);
        }
        return false;
    }

    @Override
    public long supplyCWU(long requested, boolean simulate) {
        var latestTimeStamp = getMachine().getOffsetTimer();
        if (lastTimeStamp < latestTimeStamp) {
            lastOutputCwu = currentOutputCwu;
            currentOutputCwu = 0;
            lastTimeStamp = latestTimeStamp;
        }

        if (handlerIO == IO.IN) {
            if (isTransmitter()) {
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                if (machine instanceof IComputationProvider user) {
                    return user.supplyCWU(requested, simulate);
                } else if (machine instanceof IMultiPart part) {
                    if (part.getControllers().isEmpty()) {
                        return 0;
                    }
                    for (IMultiController controller : part.getControllers()) {
                        if (controller instanceof IComputationProvider provider) {
                            return provider.supplyCWU(requested, simulate);
                        }
                        for (MachineTrait trait : controller.self().getTraits()) {
                            if (trait instanceof IComputationProvider provider) {
                                return provider.supplyCWU(requested, simulate);
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
                IComputationProvider provider = getOpticalNetProvider();
                if (provider == null) return 0;
                return provider.supplyCWU(requested, simulate);
            }
        } else {
            lastOutputCwu = lastOutputCwu - requested;
            return Math.min(lastOutputCwu, requested);
        }
    }

    @Override
    public long requestCWU(long requested, boolean simulate) {
        var latestTimeStamp = getMachine().getOffsetTimer();
        if (lastTimeStamp < latestTimeStamp) {
            lastOutputCwu = currentOutputCwu;
            currentOutputCwu = 0;
            lastTimeStamp = latestTimeStamp;
        }

        if (handlerIO == IO.IN) {
            if (isTransmitter()) {
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                if (machine instanceof IComputationUser user) {
                    return user.requestCWU(requested, simulate);
                } else if (machine instanceof IMultiPart part) {
                    if (part.getControllers().isEmpty()) {
                        return 0;
                    }
                    for (IMultiController controller : part.getControllers()) {
                        if (controller instanceof IComputationUser provider) {
                            return provider.requestCWU(requested, simulate);
                        }
                        for (MachineTrait trait : controller.self().getTraits()) {
                            if (trait instanceof IComputationUser provider) {
                                return provider.requestCWU(requested, simulate);
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
                IComputationUser provider = getOpticalNetUser();
                if (provider == null) return 0;
                return provider.requestCWU(requested, simulate);
            }
        } else {
            lastOutputCwu = lastOutputCwu - requested;
            return Math.min(lastOutputCwu, requested);
        }
    }

    @Override
    public long maxCWUt() {
        if (handlerIO == IO.IN) {
            if (isTransmitter()) {
                // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
                if (machine instanceof IComputationProvider provider) {
                    return provider.maxCWUt();
                } else if (machine instanceof IMultiPart part) {
                    if (part.getControllers().isEmpty()) {
                        return 0;
                    }
                    for (IMultiController controller : part.getControllers()) {
                        if (!controller.isFormed()) {
                            continue;
                        }
                        if (controller instanceof IComputationProvider provider) {
                            return provider.maxCWUt();
                        }
                        for (MachineTrait trait : controller.self().getTraits()) {
                            if (trait instanceof IComputationProvider provider) {
                                return provider.maxCWUt();
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
                IComputationProvider provider = getOpticalNetProvider();
                if (provider == null) return 0;
                return provider.maxCWUt();
            }
        } else {
            return lastOutputCwu;
        }
    }

    @Override
    public boolean supportsBridging() {
        if (isTransmitter()) {
            // Ask the Multiblock controller, which *should* be an IOpticalComputationProvider
            if (machine instanceof IComputationProvider provider) {
                return provider.supportsBridging();
            } else if (machine instanceof IMultiPart part) {
                if (part.getControllers().isEmpty()) {
                    return false;
                }
                for (IMultiController controller : part.getControllers()) {
                    if (!controller.isFormed()) {
                        continue;
                    }
                    if (controller instanceof IComputationProvider provider) {
                        return provider.supportsBridging();
                    }
                    for (MachineTrait trait : controller.self().getTraits()) {
                        if (trait instanceof IComputationProvider provider) {
                            return provider.supportsBridging();
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
            IComputationProvider provider = getOpticalNetProvider();
            if (provider == null) return true; // nothing found, so don't report a problem, just pass quietly
            return provider.supportsBridging();
        }
    }

    @Override
    public List<Long> handleRecipeInner(IO io, GTRecipe recipe, List<Long> left, @Nullable String slotName,
                                        boolean simulate) {
        IComputationProvider provider = getOpticalNetProvider();
        if (provider == null) return left;

        long sum = left.stream().reduce(0L, Long::sum);
        if (io == IO.IN) {
            long availableCWUt = requestCWU(Integer.MAX_VALUE, true);
            if (availableCWUt >= sum) {
                if (recipe.data.getBoolean("duration_is_total_cwu")) {
                    int drawn = Ints.saturatedCast(provider.supplyCWU(availableCWUt, simulate));
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
                    sum -= provider.supplyCWU(sum, simulate);
                }
            }
        } else if (io == IO.OUT) {
            long canInput = this.maxCWUt() - this.lastOutputCwu;
            if (!simulate) {
                this.currentOutputCwu = Math.min(canInput, sum);
            }
            sum = sum - canInput;
        }
        return sum <= 0 ? null : Collections.singletonList(sum);
    }

    @Override
    public List<Object> getContents() {
        return List.of(lastOutputCwu);
    }

    @Override
    public double getTotalContentAmount() {
        return lastOutputCwu;
    }

    @Override
    public RecipeCapability<Long> getCapability() {
        return CWURecipeCapability.CAP;
    }

    @Nullable
    private IComputationProvider getOpticalNetProvider() {
        for (Direction direction : GTUtil.DIRECTIONS) {
            BlockEntity blockEntity = machine.getLevel().getBlockEntity(machine.getPos().relative(direction));
            return blockEntity.getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, direction.getOpposite())
                    .resolve().orElse(null);
        }
        return null;
    }

    @Nullable
    private IComputationUser getOpticalNetUser() {
        for (Direction direction : GTUtil.DIRECTIONS) {
            BlockEntity blockEntity = machine.getLevel().getBlockEntity(machine.getPos().relative(direction));
            return blockEntity.getCapability(GTCapability.CAPABILITY_COMPUTATION_USER, direction.getOpposite())
                    .resolve().orElse(null);
        }
        return null;
    }
}
