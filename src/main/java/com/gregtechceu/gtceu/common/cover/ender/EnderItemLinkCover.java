package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandlers;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualItemStorage;
import com.gregtechceu.gtceu.api.sync_system.SyncDataHolder;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnderItemLinkCover extends AbstractEnderLinkCover<VirtualItemStorage> {

    @Getter
    protected final SyncDataHolder syncDataHolder = new SyncDataHolder(this);

    protected static final int TRANSFER_RATE = 8;

    protected VirtualItemStorage storage = new VirtualItemStorage();
    protected int itemsLeftToTransferLastSecond;
    @Getter
    @SaveField
    @SyncToClient
    protected FilterHandler<ItemStack, ItemFilter> filterHandler;

    public EnderItemLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        itemsLeftToTransferLastSecond = TRANSFER_RATE * 20;
        filterHandler = FilterHandlers.item(this);
    }

    @Override
    public boolean canAttach() {
        return true;
    }

    @Override
    protected VirtualItemStorage getEntry() {
        return storage;
    }

    @Override
    protected void setEntry(VirtualEntry entry) {
        storage = (VirtualItemStorage) entry;
        syncDataHolder.markClientSyncFieldDirty("storage");
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

    @Override
    protected IWidget createVirtualEntryWidget(PanelSyncManager manager, VirtualEntry entry, int w, int h, int index) {
        if (!(entry instanceof VirtualItemStorage itemStorage)) return new ParentWidget<>().size(w, h);
        manager.getOrCreateSlot("ender_item_link_cover_" + index, 0,
                () -> new ModularSlot(itemStorage.getHandler(), 0));
        return new ItemSlot()
                .syncHandler("ender_item_link_cover_" + index)
                .marginLeft(3)
                .size(w, h);
    }

    public @Nullable IItemHandler getOwnItemHandler() {
        return coverHolder.getItemHandlerCap(attachedSide, false);
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
