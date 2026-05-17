package com.gregtechceu.gtceu.common.cover.voiding;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.cover.data.VoidingMode;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AdvancedFluidVoidingCover extends FluidVoidingCover {

    @SaveField
    @SyncToClient
    @Getter
    private VoidingMode voidingMode = VoidingMode.VOID_ANY;

    @SaveField
    @SyncToClient
    @Getter
    @Setter
    protected int globalTransferSizeMillibuckets = 1;
    @SaveField
    @SyncToClient
    @Getter
    private BucketMode transferBucketMode = BucketMode.MILLI_BUCKET;

    public AdvancedFluidVoidingCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    //////////////////////////////////////////////
    // *********** COVER LOGIC ***********//
    //////////////////////////////////////////////

    @Override
    protected void doVoidFluids() {
        IFluidHandlerModifiable fluidHandler = getOwnFluidHandler();
        if (fluidHandler == null) {
            return;
        }

        switch (voidingMode) {
            case VOID_ANY -> voidAny(fluidHandler);
            case VOID_OVERFLOW -> voidOverflow(fluidHandler);
        }
    }

    private void voidOverflow(IFluidHandlerModifiable fluidHandler) {
        var fluidAmounts = enumerateDistinctFluids(fluidHandler, TransferDirection.EXTRACT);

        for (var entry : Object2LongMaps.fastIterable(fluidAmounts)) {
            var stack = entry.getKey();
            long presentAmount = entry.getLongValue();
            int targetAmount = getFilteredFluidAmount(stack);
            if (targetAmount <= 0L || targetAmount > presentAmount) continue;

            long diff = presentAmount - targetAmount;
            for (int op : GTMath.split(diff)) {
                var toDrain = new FluidStack(stack, op);
                fluidHandler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    private int getFilteredFluidAmount(FluidStack fluidStack) {
        if (!filterHandler.isFilterPresent())
            return globalTransferSizeMillibuckets;

        FluidFilter filter = filterHandler.getFilter();
        return filter.isBlackList() ? globalTransferSizeMillibuckets : filter.testFluidAmount(fluidStack);
    }

    public void setVoidingMode(VoidingMode voidingMode) {
        this.voidingMode = voidingMode;
        syncDataHolder.markClientSyncFieldDirty("voidingMode");

        if (!this.isRemote()) {
            configureFilter();
        }
    }

    private void setTransferBucketMode(BucketMode transferBucketMode) {
        this.transferBucketMode = transferBucketMode;
        syncDataHolder.markClientSyncFieldDirty("transferBucketMode");
    }

    //////////////////////////////////////
    // *********** GUI ***********//

    @Override
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                  UISettings settings) {
        super.createCoverUIRows(column, data, syncManager, settings);

        EnumSyncValue<VoidingMode> voidingMode = new EnumSyncValue<>(VoidingMode.class,
                this::getVoidingMode, this::setVoidingMode);
        IntSyncValue voidingLimit = new IntSyncValue(this::getGlobalTransferSizeMillibuckets,
                this::setGlobalTransferSizeMillibuckets);
        EnumSyncValue<BucketMode> bucketModeSync = new EnumSyncValue<>(BucketMode.class, this::getBucketMode,
                this::setBucketMode);

        syncManager.syncValue("voidingMode", voidingMode);
        syncManager.syncValue("voidingLimit", voidingLimit);

        column.child(new GTMuiWidgets.EnumRowBuilder<>(VoidingMode.class)
                .value(voidingMode)
                .overlay(16, GTGuiTextures.VOIDING_MODES)
                .lang(Text.dynamic(() -> Component.translatable(getVoidingMode().tooltip)))
                .build()
                .marginTop(2));

        column.child(
                GTMuiWidgets
                        .createIntInputWithBucketMode(voidingLimit, bucketModeSync, () -> getVoidingMode().maxStackSize)
                        .setEnabledIf($ -> shouldShowStackSize()));
    }

    private int getCurrentBucketModeTransferSize() {
        return this.globalTransferSizeMillibuckets / this.transferBucketMode.multiplier;
    }

    private void setCurrentBucketModeTransferSize(int transferSize) {
        this.globalTransferSizeMillibuckets = Math.max(transferSize * this.transferBucketMode.multiplier, 0);
        syncDataHolder.markClientSyncFieldDirty("globalTransferSizeMillibuckets");
    }

    @Override
    protected void configureFilter() {
        if (filterHandler.getFilter() instanceof SimpleFluidFilter filter) {
            filter.setMaxStackSize(voidingMode == VoidingMode.VOID_ANY ? 1 : Integer.MAX_VALUE);
        }
    }

    private boolean shouldShowStackSize() {
        if (this.voidingMode == VoidingMode.VOID_ANY)
            return false;

        if (!this.filterHandler.isFilterPresent())
            return true;

        return this.filterHandler.getFilter().isBlackList();
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("voidingMode", getVoidingMode().ordinal());
        tag.putInt("voidSize", getGlobalTransferSizeMillibuckets());
        tag.putInt("voidBucketMode", getTransferBucketMode().ordinal());
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setVoidingMode(VoidingMode.values()[tag.getInt("voidingMode")]);
        setTransferBucketMode(BucketMode.values()[tag.getInt("voidBucketMode")]);
        setCurrentBucketModeTransferSize(tag.getInt("voidSize"));
        super.pasteConfig(player, tag);
    }
}
