package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEnderRegistry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualItemStorage;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnderItemLinkCover extends AbstractEnderLinkCover<VirtualItemStorage> {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(EnderItemLinkCover.class,
            AbstractEnderLinkCover.MANAGED_FIELD_HOLDER);

    protected static final int TRANSFER_RATE = 8;

    @Persisted
    @DescSynced
    protected VirtualItemStorage storage;
    protected int itemsLeftToTransferLastSecond;
    @Getter
    @Persisted
    @DescSynced
    protected FilterHandler<ItemStack, ItemFilter> filterHandler;

    public EnderItemLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        itemsLeftToTransferLastSecond = TRANSFER_RATE * 20;
        filterHandler = FilterHandlers.item(this);
        if (!isRemote()) storage = VirtualEnderRegistry.getInstance().getOrCreateEntry(getOwner(),
                EntryTypes.ENDER_ITEM, getChannelName());
    }

    @Override
    public boolean canAttach() {
        return true;
    }

    @Override
    protected String identifier() {
        return "EILink#";
    }

    @Override
    protected VirtualItemStorage getEntry() {
        return storage;
    }

    @Override
    protected void setEntry(VirtualEntry entry) {
        storage = (VirtualItemStorage) entry;
    }

    @Override
    protected EntryTypes<VirtualItemStorage> getEntryType() {
        return EntryTypes.ENDER_ITEM;
    }

    @Override
    protected void transfer() {
        long timer = coverHolder.getOffsetTimer();
        if (itemsLeftToTransferLastSecond > 0) {
            itemsLeftToTransferLastSecond -= doTransferItems(itemsLeftToTransferLastSecond);
        }
        if (timer % 20 == 0) itemsLeftToTransferLastSecond = TRANSFER_RATE * 20;
    }

    private int doTransferItems(int max) {
        IItemHandler ownHandler = getOwnItemHandler();
        if (ownHandler == null) return 0;
        return switch (io) {
            case IN -> GTTransferUtils.transferItemsFiltered(ownHandler, storage.getHandler(),
                    filterHandler.getFilter(), max);
            case OUT -> GTTransferUtils.transferItemsFiltered(storage.getHandler(), ownHandler,
                    filterHandler.getFilter(), max);
            default -> 0;
        };
    }

    public @Nullable IItemHandler getOwnItemHandler() {
        return coverHolder.getItemHandlerCap(attachedSide, false);
    }

    @Override
    protected Widget addVirtualEntryWidget(VirtualEntry entry, int x, int y, int width, int height, boolean canClick) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);
        for (int i = 0; i < ((VirtualItemStorage) entry).getHandler().getSlots(); i++) {
            group.addWidget(new SlotWidget(((VirtualItemStorage) entry).getHandler(), i, 8 * i, 0, canClick, canClick));
        }
        return group;
    }

    @Override
    protected String getUITitle() {
        return "cover.ender_item_link.title";
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
