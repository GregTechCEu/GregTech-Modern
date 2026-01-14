package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IObjectHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.mui.factory.PosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widget.Widget;
import com.gregtechceu.gtceu.api.mui.widgets.SlotGroupWidget;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ItemSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.ModularSlot;
import com.gregtechceu.gtceu.api.mui.widgets.slot.SlotGroup;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.client.mui.screen.ModularPanel;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.data.mui.GTMuiWidgets;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.syncsystem.annotations.SaveField;
import com.gregtechceu.gtceu.syncsystem.annotations.SyncToClient;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ObjectHolderMachine extends MultiblockPartMachine implements IObjectHolder, IMachineLife {

    // purposefully not exposed to automation or capabilities
    @SaveField
    private final ObjectHolderHandler heldItems;
    @Getter
    @SaveField
    @SyncToClient
    private boolean isLocked;

    public ObjectHolderMachine(BlockEntityCreationInfo info) {
        super(info);
        heldItems = new ObjectHolderHandler(this);
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
        syncDataHolder.markClientSyncFieldDirty("isLocked");
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
    public void onMachineRemoved() {
        clearInventory(this.heldItems.storage);
    }

    @Override
    public @NotNull ModularPanel buildUI(@NotNull PosGuiData data, @NotNull PanelSyncManager syncManager,
                                         @NotNull UISettings settings) {
        return new ModularPanel(this.getDefinition().getName())
                .size(176, 166)
                .child(GTMuiWidgets.createTitleBar(this.getDefinition(), 176))
                .child(new ParentWidget<>()
                        .widthRel(1)
                        .heightRel(0.5f)
                        .child(internalUI(syncManager)))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7));
    }

    private Widget<?> internalUI(PanelSyncManager syncManager) {
        SlotGroup objectGroup = new SlotGroup("object_slot", 1);
        SlotGroup orbGroup = new SlotGroup("orb_slot", 1);

        return Flow.row()
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .align(Alignment.CENTER)
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
                        .verticalCenter());
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
            super(metaTileEntity, 2, IO.IN, IO.BOTH, size -> new CustomItemStackHandler(size) {

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
