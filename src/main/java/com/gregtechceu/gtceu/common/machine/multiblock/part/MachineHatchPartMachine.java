package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.BlockableSlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineHatchMultiblock;
import com.gregtechceu.gtceu.api.machine.feature.IMachineModifyDrops;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MachineHatchPartMachine extends MultiblockPartMachine implements IMachineModifyDrops, IUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MachineHatchPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);
    @Getter
    @Persisted
    private final NotifiableItemStackHandler machineStorage;

    public MachineHatchPartMachine(IMachineBlockEntity holder) {
        super(holder);
        this.machineStorage = createMachineStorage();
    }

    protected NotifiableItemStackHandler createMachineStorage(Object... args) {
        return new LimitedImportHandler(this);
    }

    @Override
    public void onDrops(List<ItemStack> drops, Player entity) {
        clearInventory(drops, machineStorage.storage);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        ModularUI builder = new ModularUI(176, 18 + 18 + 94,
            this, entityPlayer)
            .widget(new LabelWidget(10, 5, getDefinition().getDescriptionId()));
        builder.background(GuiTextures.BACKGROUND);

        builder.widget(new BlockableSlotWidget(machineStorage, 0, 81, 18, true, true)
            .setIsBlocked(this::isSlotBlocked)
            .setBackgroundTexture(GuiTextures.SLOT));

        return builder.widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 18 + 18 + 12, true));
    }

    @Override
    public boolean canShared() {
        return false;
    }

    private int getMachineLimit() {
        if (!getControllers().isEmpty() && getControllers().get(0) instanceof IMachineHatchMultiblock controller) {
            return controller.getMachineLimit();
        }
        return 64;
    }

    private boolean isSlotBlocked() {
        if (!getControllers().isEmpty() && getControllers().get(0) instanceof IRecipeLogicMachine controller) {
            return controller.isActive();
        }
        return false;
    }


    private class LimitedImportHandler extends NotifiableItemStackHandler {

        public LimitedImportHandler(MetaMachine metaTileEntity) {
            super(metaTileEntity, 1, IO.BOTH, IO.NONE);
        }

        @NotNull
        @Override
        // Insert item returns the remainder stack that was not inserted
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            // If the item was not valid, nothing from the stack can be inserted
            if (!isItemValid(slot, stack)) {
                return stack;
            }

            // Return Empty if passed Empty
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            // If the stacks do not match, nothing can be inserted
            if (!ItemStackHashStrategy.comparingAllButCount().equals(stack, this.getStackInSlot(slot)) &&
                !this.getStackInSlot(slot).isEmpty()) {
                return stack;
            }

            int amountInSlot = this.getStackInSlot(slot).getCount();
            int slotLimit = getSlotLimit(slot);

            // If the current stack size in the slot is greater than the limit of the Multiblock, nothing can be
            // inserted
            if (amountInSlot >= slotLimit) {
                return stack;
            }

            // This will always be positive and greater than zero if reached
            int spaceAvailable = slotLimit - amountInSlot;

            // Insert the minimum amount between the amount of space available and the amount being inserted
            int amountToInsert = Math.min(spaceAvailable, stack.getCount());

            // The remainder that was not inserted
            int remainderAmount = stack.getCount() - amountToInsert;

            // Handle any remainder
            ItemStack remainder = ItemStack.EMPTY;

            if (remainderAmount > 0) {
                remainder = stack.copy();
                remainder.setCount(remainderAmount);
            }

            if (!simulate) {
                // Perform the actual insertion
                ItemStack temp = stack.copy();
                temp.setCount(amountInSlot + amountToInsert);
                this.setStackInSlot(slot, temp);
            }

            return remainder;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            boolean slotMatches = this.getStackInSlot(slot).isEmpty() ||
                ItemStackHashStrategy.comparingAllButCount().equals(this.getStackInSlot(slot), stack);

            List<IMultiController> controllers = getControllers();
            if (!controllers.isEmpty() && controllers.get(0) instanceof IMachineHatchMultiblock controller)
                return slotMatches && GTUtil.isMachineValidForMachineHatch(stack,
                    controller.getBlacklist());

            // If the controller is null, this part is not attached to any Multiblock
            return slotMatches;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (isSlotBlocked()) {
                return ItemStack.EMPTY;
            }
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public void onContentsChanged() {
            super.onContentsChanged();
            List<IMultiController> controllers = getControllers();
            if (!controllers.isEmpty() && controllers.get(0) instanceof IMachineHatchMultiblock controller)
                controller.notifyMachineChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return MachineHatchPartMachine.this.getMachineLimit();
        }
    }
}
