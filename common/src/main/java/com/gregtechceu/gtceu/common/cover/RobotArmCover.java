package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.CycleButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;

public class RobotArmCover extends ConveyorCover {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(RobotArmCover.class, ConveyorCover.MANAGED_FIELD_HOLDER);

    public static final TransferMode[] TRANSFER_MODES = Arrays.stream(TransferMode.values())
            .sorted(Comparator.comparingInt(Enum::ordinal))
            .toArray(TransferMode[]::new);

    @Persisted
    @Getter
    protected TransferMode transferMode;

    @Persisted
    @Getter
    protected int transferStackSize;

    private TextFieldWidget transferStackSizeField;
    //private WidgetGroup stackSizeGroup;
    private IntInputWidget stackSizeInput;

    public RobotArmCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide, int tier) {
        super(definition, coverHolder, attachedSide, tier);
        setTransferMode(TransferMode.TRANSFER_ANY);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected int doTransferItems(IItemTransfer itemHandler, IItemTransfer myItemHandler, int maxTransferAmount) {
        // TODO handle pipenet handlers once item pipes are implemented

        switch (transferMode) {
            case TRANSFER_ANY:
                return doTransferItemsAny(itemHandler, myItemHandler, maxTransferAmount);
            case TRANSFER_EXACT:
                return doTransferExact(itemHandler, myItemHandler, maxTransferAmount);
            case KEEP_EXACT:
                return doKeepExact(itemHandler, myItemHandler, maxTransferAmount);
            default:
                return 0;
        }
    }

    protected int doTransferExact(IItemTransfer itemHandler, IItemTransfer myItemHandler, int maxTransferAmount) {
        /*
        Map<ItemStack, TypeItemInfo> sourceItemAmount = doCountSourceInventoryItemsByType(itemHandler, myItemHandler);
        Iterator<ItemStack> iterator = sourceItemAmount.keySet().iterator();
        while (iterator.hasNext()) {
            TypeItemInfo sourceInfo = sourceItemAmount.get(iterator.next());
            int itemAmount = sourceInfo.totalCount;
            int itemToMoveAmount = itemFilterContainer.getSlotTransferLimit(sourceInfo.filterSlot);

            // if smart item filter
            if (itemFilterContainer.getFilterWrapper().getItemFilter() instanceof SmartItemFilter) {
                if (itemFilterContainer.getTransferStackSize() > 1 && itemToMoveAmount * 2 <= itemAmount) {
                    // get the max we can extract from the item filter variable
                    int maxMultiplier = Math.floorDiv(maxTransferAmount, itemToMoveAmount);

                    // multiply up to the total count of all the items
                    itemToMoveAmount *= Math.min(itemFilterContainer.getTransferStackSize(), maxMultiplier);
                }
            }

            if (itemAmount >= itemToMoveAmount) {
                sourceInfo.totalCount = itemToMoveAmount;
            } else {
                iterator.remove();
            }
        }

        int itemsTransferred = 0;
        int maxTotalTransferAmount = maxTransferAmount + itemsTransferBuffered;
        boolean notEnoughTransferRate = false;
        for (TypeItemInfo itemInfo : sourceItemAmount.values()) {
            if (maxTotalTransferAmount >= itemInfo.totalCount) {
                boolean result = doTransferItemsExact(itemHandler, myItemHandler, itemInfo);
                itemsTransferred += result ? itemInfo.totalCount : 0;
                maxTotalTransferAmount -= result ? itemInfo.totalCount : 0;
            } else {
                notEnoughTransferRate = true;
            }
        }
        //if we didn't transfer anything because of too small transfer rate, buffer it
        if (itemsTransferred == 0 && notEnoughTransferRate) {
            itemsTransferBuffered += maxTransferAmount;
        } else {
            //otherwise, if transfer succeed, empty transfer buffer value
            itemsTransferBuffered = 0;
        }
        return Math.min(itemsTransferred, maxTransferAmount);
         */
        return 0; // TODO
    }

    protected int doKeepExact(IItemTransfer itemHandler, IItemTransfer myItemHandler, int maxTransferAmount) {
        /*
        Map<Object, GroupItemInfo> currentItemAmount = doCountDestinationInventoryItemsByMatchIndex(itemHandler, myItemHandler);
        Map<Object, GroupItemInfo> sourceItemAmounts = doCountDestinationInventoryItemsByMatchIndex(myItemHandler, itemHandler);
        Iterator<Object> iterator = sourceItemAmounts.keySet().iterator();
        while (iterator.hasNext()) {
            Object filterSlotIndex = iterator.next();
            GroupItemInfo sourceInfo = sourceItemAmounts.get(filterSlotIndex);
            int itemToKeepAmount = itemFilterContainer.getSlotTransferLimit(sourceInfo.filterSlot);

            // only run multiplier for smart item
            if (itemFilterContainer.getFilterWrapper().getItemFilter() instanceof SmartItemFilter) {
                if (itemFilterContainer.getTransferStackSize() > 1 && itemToKeepAmount * 2 <= sourceInfo.totalCount) {
                    // get the max we can keep from the item filter variable
                    int maxMultiplier = Math.floorDiv(sourceInfo.totalCount, itemToKeepAmount);

                    // multiply up to the total count of all the items
                    itemToKeepAmount *= Math.min(itemFilterContainer.getTransferStackSize(), maxMultiplier);
                }
            }

            int itemAmount = 0;
            if (currentItemAmount.containsKey(filterSlotIndex)) {
                GroupItemInfo destItemInfo = currentItemAmount.get(filterSlotIndex);
                itemAmount = destItemInfo.totalCount;
            }
            if (itemAmount < itemToKeepAmount) {
                sourceInfo.totalCount = itemToKeepAmount - itemAmount;
            } else {
                iterator.remove();
            }
        }
        return doTransferItemsByGroup(itemHandler, myItemHandler, sourceItemAmounts, maxTransferAmount);
         */
        return 0; // TODO
    }


    //////////////////////////////////////
    //***********     GUI    ***********//
    //////////////////////////////////////

    @Override
    @NotNull
    protected String getUITitle() {
        return "cover.robotic_arm.title";
    }

    @Override
    protected void buildAdditionalUI(WidgetGroup group) {
        //this.stackSizeGroup = new WidgetGroup(91, 70, 75, 20);

        CycleButtonWidget transferModeSelector = new CycleButtonWidget(
                91, 45, 75, 20, 3,
                i -> new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture(TRANSFER_MODES[i].localeName)),
                i -> {
                    setTransferMode(TRANSFER_MODES[i]);
                }
        );
        transferModeSelector.setIndex(transferMode.ordinal());
        group.addWidget(transferModeSelector);

        this.stackSizeInput = new IntInputWidget(91, 70, 75, 20,
                transferStackSize, val -> transferStackSize = val
        );
        configureStackSizeInput();

        group.addWidget(this.stackSizeInput);
    }

    private void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;

        if (this.isRemote()) {
            configureStackSizeInput();
            return;
        }

        // Everything from here on only needs to happen on the server.

        initializeFilterHandler();

        if (this.transferStackSizeField != null)
            this.transferStackSizeField.setNumbersOnly(1, transferMode.maxStackSize);
    }

    private void configureStackSizeInput() {
        if (this.stackSizeInput == null)
            return;

        this.stackSizeInput.setVisible(this.transferMode != TransferMode.TRANSFER_ANY);
        this.stackSizeInput.setMin(1);
        this.stackSizeInput.setMax(this.transferMode.maxStackSize);
    }

    @Override
    protected void initializeFilterHandler() {
        if (this.filterHandler instanceof SimpleItemFilter filter) {
            filter.setMaxStackSize(transferMode.maxStackSize);
        }
    }
}
