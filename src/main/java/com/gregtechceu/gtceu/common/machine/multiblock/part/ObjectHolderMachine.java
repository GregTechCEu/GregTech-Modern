package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.machine.feature.IMuiMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.slot.SlotGroup;
import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ObjectHolderMachine extends MultiblockPartMachine implements IMuiMachine {

    // purposefully not exposed to automation or capabilities
    @SaveField
    private final ObjectHolderHandler heldItems;
    @Getter
    @SaveField
    @SyncToClient
    private boolean isLocked;

    @Getter
    private NotifiableItemStackHandler handler;

    public ObjectHolderMachine(BlockEntityCreationInfo info) {
        super(info);
        heldItems = attachTrait(new ObjectHolderHandler());
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
        syncDataHolder.markClientSyncFieldDirty("isLocked");
    }

    public ItemStack getHeldItem(boolean remove) {
        return getHeldItem(0, remove);
    }

    public void setHeldItem(ItemStack heldItem) {
        heldItems.setStackInSlot(0, heldItem);
    }

    public ItemStack getDataItem(boolean remove) {
        return getHeldItem(1, remove);
    }

    public void setDataItem(ItemStack dataItem) {
        heldItems.setStackInSlot(1, dataItem);
    }

    private ItemStack getHeldItem(int slot, boolean remove) {
        ItemStack stackInSlot = heldItems.getStackInSlot(slot);
        if (remove && stackInSlot != ItemStack.EMPTY) {
            heldItems.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return stackInSlot;
    }

    @Override
    public void buildMainUI(ParentWidget<?> mainWidget, PosGuiData guiData, PanelSyncManager syncManager,
                            UISettings settings) {
        SlotGroup objectGroup = new SlotGroup("object_slot", 1);
        SlotGroup orbGroup = new SlotGroup("orb_slot", 1);

        mainWidget.child(Flow.row()
                .center()
                .coverChildren()
                .child(new ItemSlot()
                        .slot(new ModularSlot(heldItems, 1).slotGroup(orbGroup))
                        .background(GTGuiTextures.SLOT, GTGuiTextures.DATA_ORB_OVERLAY)
                        .marginLeft(30)
                        .marginRight(30)
                        .verticalCenter())

                .child(GTGuiTextures.PROGRESS_BAR_RESEARCH_STATION_BASE.asWidget()
                        .size(84, 60)
                        .pos(75, 0))

                .child(new ItemSlot()
                        .slot(new ModularSlot(heldItems, 0).slotGroup(objectGroup))
                        .background(GTGuiTextures.SLOT, GTGuiTextures.RESEARCH_STATION_OVERLAY)
                        .marginLeft(30)
                        .marginRight(30)
                        .verticalCenter()));
    }

    @Override
    public void setFrontFacing(Direction frontFacing) {
        super.setFrontFacing(frontFacing);
        var controllers = getControllers();
        for (var controller : controllers) {
            if (controller.isFormed()) {
                PatternState patternState = controller.getPatternState(MultiblockControllerMachine.DEFAULT_STRUCTURE);
                if (!patternState.getState().isValid()) {
                    controller.checkDefaultStructurePattern();
                }
            }
        }
    }

    private class ObjectHolderHandler extends NotifiableItemStackHandler {

        public ObjectHolderHandler() {
            super(2, IO.IN, IO.BOTH, size -> new CustomItemStackHandler(size) {

                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }
            });
        }

        // only allow a single item, no stack size
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        // prevent extracting the item while running
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!isLocked()) {
                return super.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        // only allow data items in the second slot
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (stack.isEmpty()) {
                return true;
            }

            boolean isDataItem = false;
            if (stack.getItem() instanceof IComponentItem metaItem) {
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
