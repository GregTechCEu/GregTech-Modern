package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.syncsystem.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AutoOutputTrait extends MachineTrait {

    public static MachineTraitType<AutoOutputTrait> TYPE = new MachineTraitType<>(AutoOutputTrait.class);

    @Override
    public MachineTraitType<AutoOutputTrait> getTraitType() {
        return TYPE;
    }
    @Getter
    protected final List<IItemHandler> itemHandlers;
    @Getter
    protected final List<IFluidHandler> fluidHandlers;
    @Setter
    @Getter
    protected int ticksPerCycle;

    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected @Nullable Direction itemOutputDirection;
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected @Nullable Direction fluidOutputDirection;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean autoOutputItems;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected boolean autoOutputFluids;
    @Getter
    @Setter
    @SaveField
    protected boolean allowItemInputFromOutputSide;
    @Getter
    @Setter
    @SaveField
    protected boolean allowFluidInputFromOutputSide;

    protected @Nullable TickableSubscription outputSub;

    public AutoOutputTrait(MetaMachine machine, List<IItemHandler> itemHandlers, List<IFluidHandler> fluidHandlers, int ticksPerCycle) {
        super(machine);
        this.itemHandlers = itemHandlers.stream().filter(h -> {
            if (h.getSlots() == 0) return false;
            if (h instanceof ICapabilityTrait cap) return cap.canCapOutput();
            return true;
        }).toList();
        this.fluidHandlers = fluidHandlers.stream().filter(h -> {
            if (h.getTanks() == 0) return false;
            if (h instanceof ICapabilityTrait cap) return cap.canCapOutput();
            return true;
        }).toList();
        this.ticksPerCycle = ticksPerCycle;
    }

    public AutoOutputTrait(MetaMachine machine, List<IItemHandler> itemHandlers, List<IFluidHandler> fluidHandlers) {
        this(machine, itemHandlers, fluidHandlers, 5);
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
        }
    }

    @Override
    public void onMachineUnload() {
        if (outputSub != null) {
            outputSub.unsubscribe();
            outputSub = null;
        }
        super.onMachineUnload();
    }

    public boolean supportsAutoOutputItems() {
        return !itemHandlers.isEmpty();
    }

    public boolean supportsAutoOutputFluids() {
        return !fluidHandlers.isEmpty();
    }

    public @Nullable Direction getItemOutputDirection() {
        return supportsAutoOutputItems() ? itemOutputDirection : null;
    }

    public @Nullable Direction getFluidOutputDirection() {
        return supportsAutoOutputFluids() ? fluidOutputDirection : null;
    }


    public void setAutoOutputItems(boolean allow) {
        if (supportsAutoOutputItems()) {
            this.autoOutputItems = allow;
            syncDataHolder.markClientSyncFieldDirty("autoOutputItems");
            updateAutoOutputSubscription();
        }
    }

    public void setAutoOutputFluids(boolean allow) {
        if (supportsAutoOutputFluids()) {
            this.autoOutputFluids = allow;
            syncDataHolder.markClientSyncFieldDirty("autoOutputFluids");
            updateAutoOutputSubscription();
        }
    }

    public void setFluidOutputDirection(@Nullable Direction outputFacing) {
        if (supportsAutoOutputFluids()) {
            this.fluidOutputDirection = outputFacing;
            syncDataHolder.markClientSyncFieldDirty("outputFacingFluids");
            updateAutoOutputSubscription();
        }
    }

    public void setItemOutputDirection(@Nullable Direction outputFacing) {
        if (supportsAutoOutputItems()) {
            this.itemOutputDirection = outputFacing;
            syncDataHolder.markClientSyncFieldDirty("outputFacingItems");
            updateAutoOutputSubscription();
        }
    }

    private boolean shouldKeepSubscription() {
        if (!supportsAutoOutputItems() && !supportsAutoOutputFluids()) return false;

        if (!isAutoOutputItems() || getItemOutputDirection() == null || !GTTransferUtils.hasAdjacentItemHandler(getLevel(), machine.getBlockPos(), getItemOutputDirection())) return false;
        if (!isAutoOutputFluids() || getFluidOutputDirection() == null || !GTTransferUtils.hasAdjacentFluidHandler(getLevel(), machine.getBlockPos(), getFluidOutputDirection())) return false;

        return true;
    }

    protected void updateAutoOutputSubscription() {
        if (shouldKeepSubscription()) {
            outputSub = machine.subscribeServerTick(outputSub, this::autoOutput);
        } else if (outputSub != null) {
            outputSub.unsubscribe();
            outputSub = null;
        }
    }

    protected void autoOutput() {
        if (machine.getOffsetTimer() % ticksPerCycle == 0) {
            if (isAutoOutputFluids() && getFluidOutputDirection() != null) {
                fluidHandlers.forEach(this::exportFluidToNearby);
            }
            if (isAutoOutputItems() && getItemOutputDirection() != null) {
                itemHandlers.forEach(this::exportItemToNearby);
            }
        }
        updateAutoOutputSubscription();
    }

    private void exportFluidToNearby(IFluidHandler handler) {
        var filter = getMachine().getFluidCapFilter(getFluidOutputDirection(), IO.OUT);
        GTTransferUtils.getAdjacentFluidHandler(getLevel(), machine.getBlockPos(), getFluidOutputDirection())
                .ifPresent(adj -> GTTransferUtils.transferFluidsFiltered(handler, adj, filter));
    }

    private void exportItemToNearby(IItemHandler handler) {
        var filter = getMachine().getItemCapFilter(getItemOutputDirection(), IO.OUT);
        GTTransferUtils.getAdjacentItemHandler(getLevel(), machine.getBlockPos(), getItemOutputDirection()).ifPresent(adj -> GTTransferUtils.transferItemsFiltered(handler, adj, filter));
    }
}
