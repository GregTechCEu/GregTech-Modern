package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IObjectHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ObjectHolderPartMachine extends TieredIOPartMachine implements IUIMachine, IMachineModifyDrops, IObjectHolder {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ObjectHolderPartMachine.class, TieredIOPartMachine.MANAGED_FIELD_HOLDER);

    // purposefully not exposed to automation or capabilities
    @Persisted
    private final ObjectHolderHandler heldItems;
    @Getter @Setter
    @Persisted
    private boolean isLocked;

    public ObjectHolderPartMachine(IMachineBlockEntity holder) {
        super(holder, GTValues.ZPM, IO.IN);
        heldItems = new ObjectHolderHandler();
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(192, 168, this, entityPlayer)
                .widget(new LabelWidget(5, 5, getDefinition().getDescriptionId()))
                .widget(new ImageWidget(46, 18, 84, 60, GuiTextures.PROGRESS_BAR_RESEARCH_STATION_BASE))
                .widget(new SlotWidget(heldItems, 0, 79, 39)
                        .setCanPutItems(this.isSlotBlocked())
                        .setCanTakeItems(this.isSlotBlocked())
                        .setBackground(GuiTextures.SLOT, GuiTextures.RESEARCH_STATION_OVERLAY))
                .widget(new SlotWidget(heldItems, 1, 15, 39)
                        .setCanPutItems(this.isSlotBlocked())
                        .setCanTakeItems(this.isSlotBlocked())
                        .setBackground(GuiTextures.SLOT, GuiTextures.DATA_ORB_OVERLAY))
                .bindPlayerInventory(entityPlayer.inventory)
                .build(getHolder(), entityPlayer);
    }

    private boolean isSlotBlocked() {
        return isLocked;
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, heldItems);
    }

    @Override
    public void setFrontFacing(Direction frontFacing) {
        super.setFrontFacing(frontFacing);
        var controller = getControllers().get(0);
        if (controller != null && controller.isFormed()) {
            controller.checkPatternWithLock();
        }
    }

    @NotNull
    @Override
    public ItemStack getHeldItem(boolean remove) {
        return getHeldItem(0, remove);
    }

    @Override
    public void setHeldItem(@NotNull ItemStack heldItem) {
        heldItems.setStackInSlot(0, heldItem);
    }

    @NotNull
    @Override
    public ItemStack getDataItem(boolean remove) {
        return getHeldItem(1, remove);
    }

    @Override
    public void setDataItem(@NotNull ItemStack dataItem) {
        heldItems.setStackInSlot(1, dataItem);
    }

    @NotNull
    @Override
    public IItemTransfer getAsHandler() {
        return this.heldItems;
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
    public void addedToController(IMultiController controller) {
        super.addedToController(controller);
        heldItems.addNotifiableMetaTileEntity(controller);
        heldItems.addToNotifiedList(this, heldItems, false);
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        heldItems.removeNotifiableMetaTileEntity(controllerBase);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private class ObjectHolderHandler extends NotifiableItemStackHandler {

        public ObjectHolderHandler() {
            super(ObjectHolderPartMachine.this, 2, IO.BOTH, IO.NONE);
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
            if (!isSlotBlocked()) {
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
