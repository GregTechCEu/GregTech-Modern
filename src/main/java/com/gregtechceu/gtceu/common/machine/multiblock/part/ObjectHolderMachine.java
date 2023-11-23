package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.IObjectHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.BlockableSlotWidget;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ObjectHolderMachine extends MultiblockPartMachine implements IObjectHolder {

    // purposefully not exposed to automation or capabilities
    private final ObjectHolderHandler heldItems;
    @Getter @Setter
    @DescSynced
    private boolean isLocked;

    public ObjectHolderMachine(IMachineBlockEntity holder) {
        super(holder);
        heldItems = new ObjectHolderHandler(this);
    }

    @Override
    public @NotNull ItemStack getHeldItem(boolean remove) {
        return getHeldItem(0, remove);
    }

    @Override
    public void setHeldItem(@NotNull ItemStack heldItem) {
        heldItems.setStackInSlot(0, heldItem);
    }

    @Override
    public @NotNull ItemStack getDataItem(boolean remove) {
        return getHeldItem(1, remove);
    }

    @Override
    public void setDataItem(@NotNull ItemStack dataItem) {
        heldItems.setStackInSlot(1, dataItem);
    }

    @Override
    public @NotNull NotifiableItemStackHandler getAsHandler() {
        return heldItems;
    }

    @NotNull
    private ItemStack getHeldItem(int slot, boolean remove) {
        ItemStack stackInSlot = heldItems.getStackInSlot(slot);
        if (remove && stackInSlot != ItemStack.EMPTY) {
            heldItems.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return stackInSlot;
    }

    @Override
    public Widget createUIWidget() {
        return new WidgetGroup(new Position(0, 0)).addWidget(new LabelWidget(5, 5, getDefinition().getDescriptionId()))
                .addWidget(new ImageWidget(46, 18, 84, 60, GuiTextures.PROGRESS_BAR_RESEARCH_STATION_BASE))
                .addWidget(new BlockableSlotWidget(heldItems, 0, 79, 39)
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GuiTextures.RESEARCH_STATION_OVERLAY))
                .addWidget(new BlockableSlotWidget(heldItems, 1, 15, 39)
                        .setIsBlocked(this::isLocked)
                        .setBackground(GuiTextures.SLOT, GuiTextures.DATA_ORB_OVERLAY));
    }

    @Override
    public void setFrontFacing(Direction frontFacing) {
        super.setFrontFacing(frontFacing);
        var controllers = getControllers();
        for (var controller : controllers) {
            if (controller != null && controller.isFormed()) {
                controller.checkPatternWithLock();
            }
        }
    }

    private class ObjectHolderHandler extends NotifiableItemStackHandler {

        public ObjectHolderHandler(MetaMachine metaTileEntity) {
            super(metaTileEntity, 2, IO.NONE, IO.IN);
        }

        // only allow a single item, no stack size
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        // prevent extracting the item while running
        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!isLocked()) {
                return super.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        // only allow data items in the second slot
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (stack.isEmpty()) {
                return true;
            }

            boolean isDataItem = false;
            if (stack.getItem() instanceof ComponentItem metaItem) {
                for (IItemComponent behaviour : metaItem.getComponents()) {
                    if (behaviour instanceof IDataItem) {
                        isDataItem = true;
                        break;
                    }
                }
            }

            if (slot == 0 && !isDataItem) {
                return true;
            } else return slot == 1 && isDataItem;
        }
    }
}
