package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEnderRegistry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualTank;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.value.sync.FluidSlotSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.SyncHandlers;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.slot.FluidSlot;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EnderFluidLinkCover extends AbstractEnderLinkCover<VirtualTank> {

    public static final int TRANSFER_RATE = 8000; // mB/t

    @SaveField
    @SyncToClient
    protected VirtualTank visualTank = new VirtualTank();

    // todo make this a proper class
    protected final IFluidTank tankSwitchShim = new IFluidTank() {

        private IFluidTank getDelegate() {
            return getEntry().getFluidTank();
        }

        @Override
        public @NotNull FluidStack getFluid() {
            return getDelegate().getFluid();
        }

        @Override
        public int getFluidAmount() {
            return getDelegate().getFluidAmount();
        }

        @Override
        public int getCapacity() {
            return getDelegate().getCapacity();
        }

        @Override
        public boolean isFluidValid(FluidStack fluidStack) {
            return getDelegate().isFluidValid(fluidStack);
        }

        @Override
        public int fill(FluidStack fluidStack, IFluidHandler.FluidAction fluidAction) {
            return getDelegate().fill(fluidStack, fluidAction);
        }

        @Override
        public @NotNull FluidStack drain(int i, IFluidHandler.FluidAction fluidAction) {
            return getDelegate().drain(i, fluidAction);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack fluidStack, IFluidHandler.FluidAction fluidAction) {
            return getDelegate().drain(fluidStack, fluidAction);
        }
    };

    @Getter
    @SaveField
    @SyncToClient
    protected final FilterHandler<FluidStack, FluidFilter> filterHandler;
    protected int mBLeftToTransferLastSecond;

    public EnderFluidLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        this.mBLeftToTransferLastSecond = TRANSFER_RATE * 20;
        filterHandler = FilterHandlers.fluid(this);
        if (!isRemote()) setEntry(VirtualEnderRegistry.getInstance()
                .getOrCreateEntry(getOwner(), EntryTypes.ENDER_FLUID, getChannelName()));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!coverHolder.isRemote()) visualTank = VirtualEnderRegistry.getInstance()
                .getOrCreateEntry(getOwner(), EntryTypes.ENDER_FLUID, getChannelName());
    }

    @Override
    protected VirtualTank getEntry() {
        return visualTank;
    }

    @Override
    protected void setEntry(VirtualEntry entry) {
        visualTank = (VirtualTank) entry;
        syncDataHolder.markClientSyncFieldDirty("visualTank");
    }

    @Override
    public boolean canAttach() {
        return FluidUtil.getFluidHandler(coverHolder.getLevel(), coverHolder.getBlockPos(), attachedSide).isPresent();
    }

    @Override
    protected EntryTypes<VirtualTank> getEntryType() {
        return EntryTypes.ENDER_FLUID;
    }

    @Override
    protected String identifier() {
        return "EFLink#";
    }

    @Override
    protected void transfer() {
        long timer = coverHolder.getOffsetTimer();
        if (mBLeftToTransferLastSecond > 0) {
            int platformTransferredFluid = doTransferFluids(mBLeftToTransferLastSecond);
            this.mBLeftToTransferLastSecond -= platformTransferredFluid;
        }

        if (timer % 20 == 0) {
            this.mBLeftToTransferLastSecond = TRANSFER_RATE * 20;
        }
    }

    protected @Nullable IFluidHandlerModifiable getOwnFluidHandler() {
        return coverHolder.getFluidHandlerCap(attachedSide, false);
    }

    private int doTransferFluids(int platformTransferLimit) {
        var ownFluidHandler = getOwnFluidHandler();

        if (ownFluidHandler != null) {
            return switch (io) {
                case IN -> GTTransferUtils.transferFluidsFiltered(ownFluidHandler, visualTank.getFluidTank(),
                        filterHandler.getFilter(), platformTransferLimit);
                case OUT -> GTTransferUtils.transferFluidsFiltered(visualTank.getFluidTank(), ownFluidHandler,
                        filterHandler.getFilter(), platformTransferLimit);
                default -> 0;
            };

        }
        return 0;
    }

    //////////////////////////////////////
    // ************ GUI ************ //
    //////////////////////////////////////

    @Override
    protected IWidget createVirtualEntryWidget(PanelSyncManager manager, VirtualEntry entry, int w, int h, int index) {
        if (!(entry instanceof VirtualTank tank)) return new ParentWidget<>().size(w, h);

        manager.getOrCreateSyncHandler("ender_link_cover_fluid_slot", index, FluidSlotSyncHandler.class,
                () -> SyncHandlers.fluidSlot(index == -1 ? tankSwitchShim : tank.getFluidTank()));

        return new FluidSlot()
                .syncHandler("ender_link_cover_fluid_slot", index)
                .marginLeft(3)
                .size(w, h)
        // return new FluidSlot()
        // .syncHandler(manager.getOrCreateSyncHandler(
        // ModularSyncManager.AUTO_SYNC_PREFIX + coverDefinition.getId().getPath(),
        // FluidSlotSyncHandler.class,
        // () -> new FluidSlotSyncHandler(((VirtualTank) entry).getFluidTank())))
        // .marginLeft(3)
        // .size(w, h)
        ;
    }
}
