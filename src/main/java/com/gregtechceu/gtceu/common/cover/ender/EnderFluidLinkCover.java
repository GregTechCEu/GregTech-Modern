package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.FluidFilter;
import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualTank;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandlers;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.slot.FluidSlot;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EnderFluidLinkCover extends AbstractEnderLinkCover<VirtualTank> {

    public static final int TRANSFER_RATE = 8000; // mB/t

    protected VirtualTank visualTank = new VirtualTank();

    @Getter
    @SaveField
    @SyncToClient
    protected final FilterHandler<FluidStack, FluidFilter> filterHandler;
    protected int mBLeftToTransferLastSecond;

    public EnderFluidLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        this.mBLeftToTransferLastSecond = TRANSFER_RATE * 20;
        filterHandler = FilterHandlers.fluid(this);
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

    @Override
    protected IWidget createVirtualEntryWidget(PanelSyncManager manager, VirtualEntry entry, int w, int h, int index) {
        if (!(entry instanceof VirtualTank tank)) return new ParentWidget<>().size(w, h);

        manager.getOrCreateSyncHandler("ender_link_cover_fluid_slot", index, FluidSlotSyncHandler.class,
                () -> SyncHandlers.fluidSlot(tank.getFluidTank()));

        return new FluidSlot()
                .syncHandler("ender_link_cover_fluid_slot", index)
                .marginLeft(3)
                .size(w, h);
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

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.put("filter", filterHandler.getFilterItem().serializeNBT());
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        filterHandler.setFilterItem(ItemStack.of(tag.getCompound("filter")));
        super.pasteConfig(player, tag);
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        if (!filterHandler.getFilterItem().isEmpty()) {
            list.add(filterHandler.getFilterItem());
        }
        return list;
    }
}
