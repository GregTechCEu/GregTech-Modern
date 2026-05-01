package com.gregtechceu.gtceu.common.cover.voiding;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleFluidFilter;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.cover.data.CoverModeTextures;
import com.gregtechceu.gtceu.common.cover.data.VoidingMode;
import com.gregtechceu.gtceu.utils.GTMath;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AdvancedFluidVoidingCover extends FluidVoidingCover {

    @SaveField
    @SyncToClient
    @Getter
    private VoidingMode voidingMode = VoidingMode.VOID_ANY;

    @SaveField
    @SyncToClient
    @Getter
    protected int globalTransferSizeMillibuckets = 1;
    @SaveField
    @SyncToClient
    @Getter
    private BucketMode transferBucketMode = BucketMode.MILLI_BUCKET;

    @Nullable
    IntInputWidget stackSizeInput;
    @Nullable
    EnumSelectorWidget<BucketMode> stackSizeBucketModeInput;

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
                var toDrain = stack.copyWithAmount(op);
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
        configureStackSizeInput();

        if (!this.isRemote()) {
            configureFilter();
        }
    }

    void setTransferBucketMode(BucketMode transferBucketMode) {
        this.transferBucketMode = transferBucketMode;
        syncDataHolder.markClientSyncFieldDirty("transferBucketMode");

        if (stackSizeInput != null) {
            stackSizeInput.setValue(getCurrentBucketModeTransferSize());
        }
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    protected @NotNull String getUITitle() {
        return "cover.fluid.voiding.advanced.title";
    }

    int getCurrentBucketModeTransferSize() {
        return this.globalTransferSizeMillibuckets / this.transferBucketMode.multiplier;
    }

    void setCurrentBucketModeTransferSize(int transferSize) {
        this.globalTransferSizeMillibuckets = Math.max(transferSize * this.transferBucketMode.multiplier, 0);
        syncDataHolder.markClientSyncFieldDirty("globalTransferSizeMillibuckets");
    }

    @Override
    protected void configureFilter() {
        if (filterHandler.getFilter() instanceof SimpleFluidFilter filter) {
            filter.setMaxStackSize(voidingMode == VoidingMode.VOID_ANY ? 1 : Integer.MAX_VALUE);
        }

        configureStackSizeInput();
    }

    void configureStackSizeInput() {
        if (stackSizeInput == null || stackSizeBucketModeInput == null) return;
        stackSizeInput.setVisible(shouldShowStackSize());
        stackSizeBucketModeInput.setVisible(shouldShowStackSize());
    }

    void buildAdditionalUI(WidgetGroup group) {
        group.addWidget(
                new EnumSelectorWidget<>(146, 20, 20, 20, VoidingMode.values(), getVoidingMode(),
                        this::setVoidingMode,
                        VoidingMode::getTooltip, CoverModeTextures::getVoidingModeIcon));

        stackSizeInput = new IntInputWidget(35, 20, 84, 20,
                this::getCurrentBucketModeTransferSize, this::setCurrentBucketModeTransferSize);
        stackSizeInput.setMin(1);
        stackSizeInput.setMax(Integer.MAX_VALUE);
        configureStackSizeInput();
        group.addWidget(stackSizeInput);

        stackSizeBucketModeInput = new EnumSelectorWidget<>(121, 20, 20, 20, BucketMode.values(),
                getTransferBucketMode(), this::setTransferBucketMode, BucketMode::getTooltip,
                CoverModeTextures::getBucketModeIcon);
        group.addWidget(stackSizeBucketModeInput);
    }

    boolean shouldShowStackSize() {
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
        setVoidingMode(VoidingMode.values()[tag.getIntOr("voidingMode", 0)]);
        setTransferBucketMode(BucketMode.values()[tag.getIntOr("voidBucketMode", 0)]);
        setCurrentBucketModeTransferSize(tag.getIntOr("voidSize", 0));
        super.pasteConfig(player, tag);
    }
}
