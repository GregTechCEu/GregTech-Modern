package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.*;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerDelegate;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.api.transfer.fluid.ModifiableFluidHandlerWrapper;
import com.gregtechceu.gtceu.common.cover.data.BucketMode;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.EnumSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PumpCover extends CoverBehavior implements IIOCover, IMuiCover, IControllable {

    // .5b 2b 8b
    public static final Int2IntFunction PUMP_SCALING = tier -> 64 * (int) Math.pow(4, Math.min(tier - 1, GTValues.IV));

    public final int tier;
    public final int maxFluidTransferRate;
    @SaveField
    @SyncToClient
    @Getter
    protected int transferRate;
    @SaveField
    @SyncToClient
    @Getter
    @RerenderOnChanged
    protected IO io = IO.OUT;
    @SaveField
    @SyncToClient
    @Getter
    protected BucketMode bucketMode = BucketMode.MILLI_BUCKET;
    @SaveField
    @SyncToClient
    @Getter
    protected ManualIOMode manualIOMode = ManualIOMode.DISABLED;

    @SaveField
    @SyncToClient
    @Getter
    protected boolean isWorkingEnabled = true;
    protected int mBLeftToTransferLastSecond;

    @SaveField
    @SyncToClient
    protected final FilterHandler<FluidStack, FluidFilter> filterHandler;
    protected final ConditionalSubscriptionHandler subscriptionHandler;

    public PumpCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier,
                     int maxTransferRate) {
        super(definition, coverHolder, attachedSide);
        this.tier = tier;

        this.maxFluidTransferRate = maxTransferRate;
        this.transferRate = maxFluidTransferRate;
        this.mBLeftToTransferLastSecond = transferRate * 20;

        subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isSubscriptionActive);
        filterHandler = FilterHandlers.fluid(this)
                .onFilterLoaded(f -> configureFilter())
                .onFilterUpdated(f -> configureFilter())
                .onFilterRemoved(this::configureFilter);
    }

    public PumpCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        this(definition, coverHolder, attachedSide, tier, PUMP_SCALING.applyAsInt(tier));
    }

    protected boolean isSubscriptionActive() {
        return isWorkingEnabled() && getAdjacentFluidHandler() != null;
    }

    protected @Nullable IFluidHandlerModifiable getOwnFluidHandler() {
        return coverHolder.getFluidHandlerCap(attachedSide, false);
    }

    protected @Nullable IFluidHandler getAdjacentFluidHandler() {
        return GTTransferUtils.getAdjacentFluidHandler(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide)
                .resolve()
                .orElse(null);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public boolean canAttach() {
        return super.canAttach() && getOwnFluidHandler() != null;
    }

    public void setIo(IO io) {
        if (io == IO.IN || io == IO.OUT) {
            this.io = io;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscriptionHandler.initialize(coverHolder.getLevel());
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        subscriptionHandler.unsubscribe();
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        if (!filterHandler.getFilterItem().isEmpty()) {
            list.add(filterHandler.getFilterItem());
        }
        return list;
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        subscriptionHandler.updateSubscription();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.isWorkingEnabled != isWorkingAllowed) {
            this.isWorkingEnabled = isWorkingAllowed;
            syncDataHolder.markClientSyncFieldDirty("isWorkingEnabled");
            subscriptionHandler.updateSubscription();
        }
    }

    //////////////////////////////////////
    // ***** Transfer Logic *****//
    //////////////////////////////////////

    public void setTransferRate(int milliBucketsPerTick) {
        this.transferRate = Math.min(Math.max(milliBucketsPerTick, 0), maxFluidTransferRate);
    }

    public void setBucketMode(BucketMode bucketMode) {
        this.bucketMode = bucketMode;
        syncDataHolder.markClientSyncFieldDirty("bucketMode");
    }

    protected void setManualIOMode(ManualIOMode manualIOMode) {
        this.manualIOMode = manualIOMode;
        syncDataHolder.markClientSyncFieldDirty("manualIOMode");
    }

    protected void update() {
        long timer = coverHolder.getOffsetTimer();
        if (timer % 5 != 0)
            return;

        if (mBLeftToTransferLastSecond > 0) {
            int platformTransferredFluid = doTransferFluids(mBLeftToTransferLastSecond);
            this.mBLeftToTransferLastSecond -= platformTransferredFluid;
        }

        if (timer % 20 == 0) {
            this.mBLeftToTransferLastSecond = transferRate * 20;
        }

        subscriptionHandler.updateSubscription();
    }

    private int doTransferFluids(int platformTransferLimit) {
        var adjacent = getAdjacentFluidHandler();
        var adjacentModifiable = adjacent instanceof IFluidHandlerModifiable modifiable ? modifiable :
                new ModifiableFluidHandlerWrapper(adjacent);
        var ownFluidHandler = getOwnFluidHandler();

        if (adjacent != null && ownFluidHandler != null) {
            return switch (io) {
                case IN -> doTransferFluidsInternal(adjacentModifiable, ownFluidHandler, platformTransferLimit);
                case OUT -> doTransferFluidsInternal(ownFluidHandler, adjacentModifiable, platformTransferLimit);
                default -> 0;
            };
        }
        return 0;
    }

    protected int doTransferFluidsInternal(IFluidHandlerModifiable source, IFluidHandlerModifiable destination,
                                           int platformTransferLimit) {
        return transferAny(source, destination, platformTransferLimit);
    }

    protected int transferAny(IFluidHandlerModifiable source, IFluidHandlerModifiable destination,
                              int platformTransferLimit) {
        return GTTransferUtils.transferFluidsFiltered(source, destination, filterHandler.getFilter(),
                platformTransferLimit);
    }

    protected enum TransferDirection {
        INSERT,
        EXTRACT
    }

    protected Object2LongMap<FluidStack> enumerateDistinctFluids(IFluidHandlerModifiable fluidHandler,
                                                                 TransferDirection direction) {
        // Long map because we could have multiple tanks of the same fluid summing up to > Integer.MAX_VALUE
        var summedFluids = new Object2LongOpenHashMap<FluidStack>();
        for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
            if (!canTransfer(fluidHandler, direction, tank)) continue;

            FluidStack fluidStack = fluidHandler.getFluidInTank(tank);
            if (fluidStack.isEmpty()) continue;

            summedFluids.addTo(fluidStack, fluidStack.getAmount());
        }

        return summedFluids;
    }

    private static boolean canTransfer(IFluidHandlerModifiable fluidHandler, TransferDirection direction, int tank) {
        return switch (direction) {
            case INSERT -> fluidHandler.supportsFill(tank);
            case EXTRACT -> fluidHandler.supportsDrain(tank);
        };
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                  UISettings settings) {
        IntSyncValue transferRateSync = new IntSyncValue(this::getTransferRate, this::setTransferRate);
        EnumSyncValue<BucketMode> bucketModeSync = new EnumSyncValue<>(BucketMode.class, this::getBucketMode,
                this::setBucketMode);
        EnumSyncValue<ManualIOMode> manualIOModeSync = new EnumSyncValue<>(ManualIOMode.class, this::getManualIOMode,
                this::setManualIOMode);
        EnumSyncValue<IO> ioSync = new EnumSyncValue<>(IO.class, this::getIo, this::setIo);

        syncManager.syncValue("io", ioSync);
        syncManager.syncValue("transferRate", transferRateSync);
        syncManager.syncValue("manualIO", manualIOModeSync);

        column.child(GTMuiWidgets.createIntInputWithBucketMode(transferRateSync, bucketModeSync,
                () -> maxFluidTransferRate));

        column.child(GTMuiWidgets.createFilterRow(filterHandler, data, syncManager, settings)
                .child(0, GTMuiWidgets.createIOCycleButton(ioSync, false)));

        column.child(new GTMuiWidgets.EnumRowBuilder<>(ManualIOMode.class)
                .value(manualIOModeSync)
                .overlay(16, GTGuiTextures.MANUAL_IO_OVERLAY_IN)
                .lang(Text.dynamic(() -> Component.translatable(manualIOMode.localeName)))
                .build());
    }

    protected void configureFilter() {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    /////////////////////////////////////
    // *** CAPABILITY OVERRIDE ***//
    /////////////////////////////////////

    private @Nullable CoverableFluidHandlerWrapper fluidHandlerWrapper;

    @Nullable
    @Override
    public IFluidHandlerModifiable getFluidHandlerCap(@Nullable IFluidHandlerModifiable defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        if (fluidHandlerWrapper == null || fluidHandlerWrapper.delegate != defaultValue) {
            this.fluidHandlerWrapper = new CoverableFluidHandlerWrapper(defaultValue);
        }
        return fluidHandlerWrapper;
    }

    private class CoverableFluidHandlerWrapper extends FluidHandlerDelegate {

        public CoverableFluidHandlerWrapper(IFluidHandlerModifiable delegate) {
            super(delegate);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (io == IO.OUT) {
                if (manualIOMode == ManualIOMode.DISABLED) {
                    return 0;
                }
                if (manualIOMode == ManualIOMode.UNFILTERED) {
                    return super.fill(resource, action);
                }
            }
            if (!filterHandler.test(resource)) {
                return 0;
            }
            return super.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (io == IO.IN) {
                if (manualIOMode == ManualIOMode.DISABLED) {
                    return FluidStack.EMPTY;
                }
                if (manualIOMode == ManualIOMode.UNFILTERED) {
                    return super.drain(resource, action);
                }
            }
            if (!filterHandler.test(resource)) {
                return FluidStack.EMPTY;
            }
            return super.drain(resource, action);
        }
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("transferRate", getTransferRate());
        tag.putInt("io", getIo().ordinal());
        tag.putInt("manualIO", getManualIOMode().ordinal());
        tag.put("filter", filterHandler.getFilterItem().serializeNBT());
        tag.putInt("bucketMode", getBucketMode().ordinal());
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setTransferRate(tag.getInt("transferRate"));
        setIo(IO.values()[tag.getInt("io")]);
        setManualIOMode(ManualIOMode.values()[tag.getInt("manualIO")]);
        filterHandler.setFilterItem(ItemStack.of(tag.getCompound("filter")));
        setBucketMode(BucketMode.values()[tag.getInt("bucketMode")]);
        super.pasteConfig(player, tag);
    }
}
