package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidRegulatorCover extends PumpCover {

    private static final int MAX_STACK_SIZE = 2_048_000_000; // Capacity of quantum tank IX

    @SaveField
    @SyncToClient
    @Getter
    private TransferMode transferMode = TransferMode.TRANSFER_ANY;

    @SaveField
    @SyncToClient
    @Getter
    private BucketMode transferBucketMode = BucketMode.MILLI_BUCKET;
    @SaveField
    @SyncToClient
    @Getter
    @Setter
    protected int globalTransferLimit;
    protected int fluidTransferBuffered = 0;

    public FluidRegulatorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier,
                               int maxTransferRate) {
        super(definition, coverHolder, attachedSide, tier, maxTransferRate);
    }

    public FluidRegulatorCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        this(definition, coverHolder, attachedSide, tier, PUMP_SCALING.applyAsInt(tier));
    }

    //////////////////////////////////////
    // ***** Transfer Logic ******//
    //////////////////////////////////////

    @Override
    protected int doTransferFluidsInternal(IFluidHandlerModifiable source, IFluidHandlerModifiable destination,
                                           int platformTransferLimit) {
        return switch (transferMode) {
            case TRANSFER_ANY -> transferAny(source, destination, platformTransferLimit);
            case TRANSFER_EXACT -> transferExact(source, destination, platformTransferLimit);
            case KEEP_EXACT -> keepExact(source, destination, platformTransferLimit);
        };
    }

    private int transferExact(IFluidHandler source, IFluidHandler destination, int platformTransferLimit) {
        int fluidLeftToTransfer = platformTransferLimit;

        for (int slot = 0; slot < source.getTanks(); slot++) {
            if (fluidLeftToTransfer <= 0)
                break;

            FluidStack sourceFluid = source.getFluidInTank(slot).copy();
            int supplyAmount = getFilteredFluidAmount(sourceFluid);

            // If the remaining transferrable amount in this operation is not enough to transfer the full stack size,
            // the remaining amount for this operation will be buffered and added to the next operation's maximum.
            if (fluidLeftToTransfer + fluidTransferBuffered < supplyAmount) {
                this.fluidTransferBuffered += fluidLeftToTransfer;
                fluidLeftToTransfer = 0;
                break;
            }

            if (sourceFluid.isEmpty() || supplyAmount <= 0)
                continue;

            sourceFluid.setAmount(supplyAmount);
            FluidStack drained = source.drain(sourceFluid, FluidAction.SIMULATE);

            if (drained.isEmpty() || drained.getAmount() < supplyAmount)
                continue;

            int insertableAmount = destination.fill(drained.copy(), FluidAction.SIMULATE);
            if (insertableAmount != supplyAmount)
                continue;

            drained.setAmount(insertableAmount);
            drained = source.drain(drained, FluidAction.EXECUTE);

            if (!drained.isEmpty()) {
                destination.fill(drained, FluidAction.EXECUTE);
                fluidLeftToTransfer -= (drained.getAmount() - fluidTransferBuffered);
            }

            fluidTransferBuffered = 0;
        }

        return platformTransferLimit - fluidLeftToTransfer;
    }

    private int keepExact(IFluidHandlerModifiable source, IFluidHandlerModifiable destination,
                          int platformTransferLimit) {
        int fluidLeftToTransfer = platformTransferLimit;

        var sourceAmounts = enumerateDistinctFluids(source, TransferDirection.EXTRACT);
        var destinationAmounts = enumerateDistinctFluids(destination, TransferDirection.INSERT);

        for (FluidStack fluidStack : sourceAmounts.keySet()) {
            if (fluidLeftToTransfer <= 0) break;

            int amountToKeep = getFilteredFluidAmount(fluidStack);
            long amountInDest = destinationAmounts.getOrDefault(fluidStack, 0);
            if (amountInDest >= amountToKeep) continue;

            FluidStack fluidToMove = fluidStack.copy();
            fluidToMove.setAmount(Math.min(fluidLeftToTransfer, (int) (amountToKeep - amountInDest)));
            if (fluidToMove.getAmount() <= 0) continue;

            FluidStack drained = source.drain(fluidToMove, FluidAction.SIMULATE);
            int fillableAmount = destination.fill(drained, FluidAction.SIMULATE);
            if (fillableAmount <= 0) continue;

            fluidToMove.setAmount(Math.min(fluidToMove.getAmount(), fillableAmount));

            drained = source.drain(fluidToMove, FluidAction.EXECUTE);
            int movedAmount = destination.fill(drained, FluidAction.EXECUTE);

            fluidLeftToTransfer -= movedAmount;
        }

        return platformTransferLimit - fluidLeftToTransfer;
    }

    private void setTransferBucketMode(BucketMode transferBucketMode) {
        this.transferBucketMode = transferBucketMode;
        syncDataHolder.markClientSyncFieldDirty("transferBucketMode");
    }

    private void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;

        if (!this.isRemote()) {
            syncDataHolder.markClientSyncFieldDirty("transferMode");
            configureFilter();
        }
    }

    @Override
    protected void configureFilter() {
        if (filterHandler.getFilter() instanceof SimpleFluidFilter filter) {
            filter.setMaxStackSize(transferMode == TransferMode.TRANSFER_ANY ? 1 : MAX_STACK_SIZE);
        }
    }

    private int getFilteredFluidAmount(FluidStack fluidStack) {
        if (!filterHandler.isFilterPresent())
            return globalTransferLimit;

        FluidFilter filter = filterHandler.getFilter();
        return (filter.supportsAmounts() ? filter.testFluidAmount(fluidStack) : globalTransferLimit);
    }

    ///////////////////////////
    // ***** GUI ******//

    @Override
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                  UISettings settings) {
        super.createCoverUIRows(column, data, syncManager, settings);

        var transferMode = new EnumSyncValue<>(TransferMode.class, this::getTransferMode, this::setTransferMode);
        var transferSize = new IntSyncValue(this::getGlobalTransferLimit, this::setGlobalTransferLimit);
        var transferBucketMode = new EnumSyncValue<>(BucketMode.class, this::getTransferBucketMode,
                this::setTransferBucketMode);

        syncManager.syncValue("transferMode", transferMode);
        syncManager.syncValue("transferSize", transferSize);

        column.child(new GTMuiWidgets.EnumRowBuilder<>(TransferMode.class)
                .value(transferMode)
                .overlay(16, GTGuiTextures.TRANSFER_MODE_OVERLAY)
                .lang(Text.dynamic(() -> Component.translatable(getTransferMode().tooltip)))
                .build());

        column.child(GTMuiWidgets.createIntInputWithBucketMode(transferSize, transferBucketMode,
                () -> maxFluidTransferRate));

        column.child(GTMuiWidgets.createIntInputWithButtons(transferSize, () -> 1, () -> MAX_STACK_SIZE)
                .setEnabledIf($ -> shouldShowTransferSize()));
    }

    private boolean shouldShowTransferSize() {
        if (this.transferMode == TransferMode.TRANSFER_ANY)
            return false;

        if (!this.filterHandler.isFilterPresent())
            return true;

        return !this.filterHandler.getFilter().supportsAmounts();
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("transferMode", transferMode.ordinal());
        tag.putInt("transferLimit", globalTransferLimit);
        tag.putInt("transferBucket", transferBucketMode.ordinal());
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setTransferMode(TransferMode.values()[tag.getInt("transferMode")]);
        globalTransferLimit = (tag.getInt("transferLimit"));
        setTransferBucketMode(BucketMode.values()[tag.getInt("transferBucket")]);
        super.pasteConfig(player, tag);
    }
}
