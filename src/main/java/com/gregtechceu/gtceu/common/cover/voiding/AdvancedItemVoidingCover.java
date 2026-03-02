package com.gregtechceu.gtceu.common.cover.voiding;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.factory.SidedPosGuiData;
import com.gregtechceu.gtceu.api.mui.value.sync.EnumSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.IntSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.cover.data.VoidingMode;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AdvancedItemVoidingCover extends ItemVoidingCover {

    @SaveField
    @SyncToClient
    @Getter
    private VoidingMode voidingMode = VoidingMode.VOID_ANY;

    @SaveField
    @Getter
    @Setter
    protected int globalVoidingLimit = 1;

    private IntInputWidget stackSizeInput;

    public AdvancedItemVoidingCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    //////////////////////////////////////////////
    // *********** COVER LOGIC ***********//
    //////////////////////////////////////////////

    @Override
    protected void doVoidItems() {
        IItemHandler handler = getOwnItemHandler();
        if (handler == null) {
            return;
        }

        switch (voidingMode) {
            case VOID_ANY -> voidAny(handler);
            case VOID_OVERFLOW -> voidOverflow(handler);
        }
    }

    private void voidOverflow(IItemHandler handler) {
        Map<ItemStack, TypeItemInfo> sourceItemAmounts = countInventoryItemsByType(handler);

        for (TypeItemInfo itemInfo : sourceItemAmounts.values()) {
            int itemToVoidAmount = itemInfo.totalCount - getFilteredItemAmount(itemInfo.itemStack);

            if (itemToVoidAmount <= 0) {
                continue;
            }

            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack is = handler.getStackInSlot(slot);
                if (!is.isEmpty() && GTUtil.isSameItemSameTags(is, itemInfo.itemStack)) {
                    ItemStack extracted = handler.extractItem(slot, itemToVoidAmount, false);

                    if (!extracted.isEmpty()) {
                        itemToVoidAmount -= extracted.getCount();
                    }
                }
                if (itemToVoidAmount == 0) {
                    break;
                }
            }
        }
    }

    private int getFilteredItemAmount(ItemStack itemStack) {
        if (!filterHandler.isFilterPresent())
            return globalVoidingLimit;

        ItemFilter filter = filterHandler.getFilter();
        return filter.isBlackList() ? globalVoidingLimit : filter.testItemCount(itemStack);
    }

    public void setVoidingMode(VoidingMode voidingMode) {
        this.voidingMode = voidingMode;

        configureStackSizeInput();

        if (!this.isRemote()) {
            syncDataHolder.markClientSyncFieldDirty("voidingMode");
            configureFilter();
        }
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                  UISettings settings) {
        super.createCoverUIRows(column, data, syncManager, settings);

        EnumSyncValue<VoidingMode> voidingMode = new EnumSyncValue<>(VoidingMode.class,
                this::getVoidingMode, this::setVoidingMode);
        IntSyncValue voidingLimit = new IntSyncValue(this::getGlobalVoidingLimit, this::setGlobalVoidingLimit);

        syncManager.syncValue("voidingMode", voidingMode);
        syncManager.syncValue("voidingLimit", voidingLimit);

        column.child(new GTMuiWidgets.EnumRowBuilder<>(VoidingMode.class)
                .value(voidingMode)
                .overlay(16, GTGuiTextures.VOIDING_MODES)
                .lang(IKey.dynamic(() -> Component.translatable(getVoidingMode().tooltip)))
                .build()
                .marginTop(2));

        column.child(GTMuiWidgets.createIntInputWithButtons(voidingLimit, () -> 1, () -> getVoidingMode().maxStackSize)
                .setEnabledIf($ -> shouldShowStackSize()));
    }

    @Override
    protected void configureFilter() {
        if (filterHandler.getFilter() instanceof SimpleItemFilter filter) {
            filter.setMaxStackSize(this.voidingMode.maxStackSize);
        }

        configureStackSizeInput();
    }

    private void configureStackSizeInput() {
        if (this.stackSizeInput == null)
            return;

        this.stackSizeInput.setVisible(shouldShowStackSize());
        this.stackSizeInput.setMin(1);
        this.stackSizeInput.setMax(this.voidingMode.maxStackSize);
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
        tag.putInt("voidSize", getGlobalVoidingLimit());
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setVoidingMode(VoidingMode.values()[tag.getInt("voidingMode")]);
        globalVoidingLimit = tag.getInt("voidSize");
        super.pasteConfig(player, tag);
    }
}
