package com.gregtechceu.gtceu.common.cover.voiding;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.common.cover.data.VoidingMode;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.CycleButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

public class AdvancedItemVoidingCover extends ItemVoidingCover {
    public static final VoidingMode[] VOIDING_MODES = Arrays.stream(VoidingMode.values())
            .sorted(Comparator.comparingInt(Enum::ordinal))
            .toArray(VoidingMode[]::new);

    @Persisted @DescSynced @Getter
    private VoidingMode voidingMode;

    @Persisted @Getter
    protected int globalVoidingLimit = 1;

    private IntInputWidget stackSizeInput;
    private CycleButtonWidget transferModeSelector;

    public AdvancedItemVoidingCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);

        voidingMode = VoidingMode.VOID_ANY;
    }


    //////////////////////////////////////////////
    //***********     COVER LOGIC    ***********//
    //////////////////////////////////////////////

    @Override
    protected void doVoidItems() {
        IItemTransfer itemTransfer = getItemTransfer();
        if (itemTransfer == null) {
            return;
        }

        switch (voidingMode) {
            case VOID_ANY -> voidAny(itemTransfer);
            case VOID_OVERFLOW -> voidOverflow(itemTransfer);
        }
    }

    private void voidOverflow(IItemTransfer itemTransfer) {
        Map<ItemStack, TypeItemInfo> sourceItemAmounts = countInventoryItemsByType(itemTransfer);

        for (TypeItemInfo itemInfo : sourceItemAmounts.values()) {
            int itemToVoidAmount = itemInfo.totalCount - getFilteredItemAmount(itemInfo.itemStack);

            if (itemToVoidAmount <= 0) {
                continue;
            }

            for (int slot = 0; slot < itemTransfer.getSlots(); slot++) {
                ItemStack is = itemTransfer.getStackInSlot(slot);
                if (!is.isEmpty() && ItemStack.isSameItemSameTags(is, itemInfo.itemStack)) {
                    ItemStack extracted = itemTransfer.extractItem(slot, itemToVoidAmount, false);

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

        if (transferModeSelector != null)
            transferModeSelector.setHoverTooltips(LocalizationUtils.format(voidingMode.localeName));

        configureStackSizeInput();

        if (!this.isRemote()) {
            configureFilterHandler();
        }
    }


    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////

    @Override
    protected @NotNull String getUITitle() {
        return "cover.item.voiding.advanced.title";
    }

    @Override
    protected void buildAdditionalUI(WidgetGroup group) {
        transferModeSelector = new CycleButtonWidget(
                146, 20, 20, 20, VOIDING_MODES.length,
                i -> new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, VOIDING_MODES[i].icon),
                i -> setVoidingMode(VOIDING_MODES[i])
        );
        transferModeSelector.setIndex(voidingMode.ordinal());
        transferModeSelector.setHoverTooltips(LocalizationUtils.format(voidingMode.localeName));

        group.addWidget(transferModeSelector);


        this.stackSizeInput = new IntInputWidget(64, 20, 80, 20,
                () -> globalVoidingLimit, val -> globalVoidingLimit = val
        );
        configureStackSizeInput();

        group.addWidget(this.stackSizeInput);
    }

    @Override
    protected void configureFilterHandler() {
        if (filterHandler.getFilter() instanceof SimpleItemFilter filter) {
            filter.setMaxStackSize(this.voidingMode.maxStackSize);
        }
    }

    private void configureStackSizeInput() {
        if (this.stackSizeInput == null)
            return;

        this.stackSizeInput.setVisible(this.voidingMode != VoidingMode.VOID_ANY);
        this.stackSizeInput.setMin(1);
        this.stackSizeInput.setMax(this.voidingMode.maxStackSize);
    }

    //////////////////////////////////////
    //*****     LDLib SyncData    ******//
    //////////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(AdvancedItemVoidingCover.class, ItemVoidingCover.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
