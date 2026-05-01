package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.SmartItemFilter;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.ItemHandlerDelegate;
import com.gregtechceu.gtceu.common.cover.data.FilterMode;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemFilterCover extends CoverBehavior implements IUICover {

    protected ItemFilter itemFilter;
    @SaveField
    @SyncToClient
    @Getter
    protected FilterMode filterMode = FilterMode.FILTER_INSERT;
    private FilteredItemHandlerWrapper itemFilterWrapper;
    @SaveField
    @Setter
    @Getter
    protected ManualIOMode allowFlow = ManualIOMode.DISABLED;

    public ItemFilterCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    public ItemFilter getItemFilter() {
        if (itemFilter == null) {
            itemFilter = ItemFilter.loadFilter(attachItem);
            if (itemFilter instanceof SmartItemFilter smart && coverHolder instanceof MachineCoverContainer mcc) {
                var machine = MetaMachine.getMachine(mcc.getLevel(), mcc.getBlockPos());
                if (machine != null) smart.setModeFromMachine(machine.getDefinition().getName());
            }
        }
        return itemFilter;
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
        syncDataHolder.markClientSyncFieldDirty("filterMode");
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && coverHolder.getItemHandlerCap(attachedSide, false) != null;
    }

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(IItemHandlerModifiable defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        if (itemFilterWrapper == null || itemFilterWrapper.delegate != defaultValue) {
            this.itemFilterWrapper = new FilteredItemHandlerWrapper(defaultValue);
        }
        return itemFilterWrapper;
    }

    @Override
    public void onAttached(ItemStack itemStack, @Nullable ServerPlayer player) {
        super.onAttached(itemStack, player);
    }

    @Override
    public Widget createUIWidget() {
        return ItemFilterCoverUI.createUIWidget(this);
    }

    private class FilteredItemHandlerWrapper extends ItemHandlerDelegate {

        public FilteredItemHandlerWrapper(IItemHandlerModifiable delegate) {
            super(delegate);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (filterMode == FilterMode.FILTER_EXTRACT) {
                if (allowFlow == ManualIOMode.DISABLED) {
                    return stack;
                }
                if (allowFlow == ManualIOMode.UNFILTERED) {
                    return super.insertItem(slot, stack, simulate);
                }
            }
            if (!getItemFilter().test(stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (filterMode == FilterMode.FILTER_INSERT) {
                if (allowFlow == ManualIOMode.DISABLED) {
                    return ItemStack.EMPTY;
                }
                if (allowFlow == ManualIOMode.UNFILTERED) {
                    return super.extractItem(slot, amount, simulate);
                }
            }
            ItemStack result = super.extractItem(slot, amount, true);
            if (result.isEmpty() || !getItemFilter().test(result)) {
                return ItemStack.EMPTY;
            }
            return simulate ? result : super.extractItem(slot, amount, false);
        }
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putInt("manualIO", getAllowFlow().ordinal());
        tag.putInt("filterMode", getFilterMode().ordinal());
        tag.put("filter", ItemStack.OPTIONAL_CODEC
                .encodeStart(coverHolder.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE),
                        attachItem)
                .getOrThrow());
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setAllowFlow(ManualIOMode.values()[tag.getIntOr("manualIO", 0)]);
        setFilterMode(FilterMode.values()[tag.getIntOr("filterMode", 0)]);
        itemFilter = ItemFilter.loadFilter(ItemStack.OPTIONAL_CODEC
                .parse(coverHolder.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE),
                        tag.getCompoundOrEmpty("filter"))
                .result()
                .orElse(ItemStack.EMPTY));
        super.pasteConfig(player, tag);
    }
}
