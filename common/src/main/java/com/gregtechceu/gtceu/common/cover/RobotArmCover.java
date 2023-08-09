package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.cover.filter.SimpleItemFilter;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.common.cover.data.TransferMode;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Iterator;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RobotArmCover extends ConveyorCover {
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(RobotArmCover.class, ConveyorCover.MANAGED_FIELD_HOLDER);

    @Persisted @DescSynced @Getter
    protected TransferMode transferMode;

    @Persisted @Getter
    protected int globalTransferLimit;
    protected int itemsTransferBuffered;

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
    protected int doTransferItems(IItemTransfer sourceInventory, IItemTransfer targetInventory, int maxTransferAmount) {
        return switch (transferMode) {
            case TRANSFER_ANY -> moveInventoryItems(sourceInventory, targetInventory, maxTransferAmount);
            case TRANSFER_EXACT -> doTransferExact(sourceInventory, targetInventory, maxTransferAmount);
            case KEEP_EXACT -> doKeepExact(sourceInventory, targetInventory, maxTransferAmount);
        };
    }

    protected int doTransferExact(IItemTransfer sourceInventory, IItemTransfer targetInventory, int maxTransferAmount) {
        Map<ItemStack, TypeItemInfo> sourceItemAmount = countInventoryItemsByType(sourceInventory);

        Iterator<ItemStack> iterator = sourceItemAmount.keySet().iterator();
        while (iterator.hasNext()) {
            TypeItemInfo sourceInfo = sourceItemAmount.get(iterator.next());
            int itemAmount = sourceInfo.totalCount;
            int itemToMoveAmount = getFilteredItemAmount(sourceInfo.itemStack);

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
                boolean result = moveInventoryItemsExact(sourceInventory, targetInventory, itemInfo);
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
    }

    protected int doKeepExact(IItemTransfer sourceInventory, IItemTransfer targetInventory, int maxTransferAmount) {
        Map<ItemStack, GroupItemInfo> targetItemAmounts = countInventoryItemsByMatchSlot(targetInventory);
        Map<ItemStack, GroupItemInfo> sourceItemAmounts = countInventoryItemsByMatchSlot(sourceInventory);

        Iterator<ItemStack> iterator = sourceItemAmounts.keySet().iterator();
        while (iterator.hasNext()) {
            ItemStack filteredItem = iterator.next();
            GroupItemInfo sourceInfo = sourceItemAmounts.get(filteredItem);
            int itemToKeepAmount = getFilteredItemAmount(sourceInfo.itemStack);

            int itemAmount = 0;
            if (targetItemAmounts.containsKey(filteredItem)) {
                GroupItemInfo destItemInfo = targetItemAmounts.get(filteredItem);
                itemAmount = destItemInfo.totalCount;
            }
            if (itemAmount < itemToKeepAmount) {
                sourceInfo.totalCount = itemToKeepAmount - itemAmount;
            } else {
                iterator.remove();
            }
        }

        return moveInventoryItems(sourceInventory, targetInventory, sourceItemAmounts, maxTransferAmount);
    }

    private int getFilteredItemAmount(ItemStack itemStack) {
        if (!filterHandler.isFilterPresent())
            return globalTransferLimit;

        ItemFilter filter = filterHandler.getFilter();
        return filter.isBlackList() ? globalTransferLimit : filter.testItemCount(itemStack);
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
        group.addWidget(new EnumSelectorWidget<>(146, 45, 20, 20, TransferMode.values(), transferMode, this::setTransferMode));

        this.stackSizeInput = new IntInputWidget(64, 45, 80, 20,
                () -> globalTransferLimit, val -> globalTransferLimit = val
        );
        configureStackSizeInput();

        group.addWidget(this.stackSizeInput);
    }

    private void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;

        configureStackSizeInput();

        if (!this.isRemote()) {
            configureFilter();
        }
    }

    @Override
    protected void configureFilter() {
        if (filterHandler.getFilter() instanceof SimpleItemFilter filter) {
            filter.setMaxStackSize(filter.isBlackList() ? 1 : transferMode.maxStackSize);
        }

        configureStackSizeInput();
    }

    private void configureStackSizeInput() {
        if (this.stackSizeInput == null)
            return;

        this.stackSizeInput.setVisible(shouldShowStackSize());
        this.stackSizeInput.setMin(1);
        this.stackSizeInput.setMax(this.transferMode.maxStackSize);
    }

    private boolean shouldShowStackSize() {
        if (this.transferMode == TransferMode.TRANSFER_ANY)
            return false;

        if (!this.filterHandler.isFilterPresent())
            return true;

        return this.filterHandler.getFilter().isBlackList();
    }
}
