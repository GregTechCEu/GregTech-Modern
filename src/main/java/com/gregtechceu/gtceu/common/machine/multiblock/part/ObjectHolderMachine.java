package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.capability.IObjectHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.BlockableSlotWidget;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IDataItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.item.ItemIngredient;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ObjectHolderMachine extends MultiblockPartMachine implements IObjectHolder, IMachineLife {

    @Persisted
    private final NotifiableItemStackHandler inputItemHandler = new NotifiableItemStackHandler(this, 1, IO.IN, IO.BOTH);
    @Persisted
    private final DataItemHandler dataItemHandler;

    @Persisted
    @DescSynced
    private boolean isLocked;

    public ObjectHolderMachine(IMachineBlockEntity holder) {
        super(holder);
        dataItemHandler = new DataItemHandler(this);
        inputItemHandler.setCapabilityValidator(direction -> !isDataItemFacing(direction));
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(this.dataItemHandler.storage);
    }

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return super.getItemHandlerCap(side, useCoverCapability);
    }

    @Override
    public Widget createUIWidget() {
        return new WidgetGroup(new Position(0, 0))
                .addWidget(new ImageWidget(46, 15, 84, 60, GuiTextures.PROGRESS_BAR_RESEARCH_STATION_BASE))
                .addWidget(new BlockableSlotWidget(inputItemHandler, 0, 79, 36)
                        .setIsBlocked(() -> isLocked)
                        .setBackground(GuiTextures.SLOT, GuiTextures.RESEARCH_STATION_OVERLAY))
                .addWidget(new BlockableSlotWidget(dataItemHandler, 0, 15, 36)
                        .setIsBlocked(() -> isLocked)
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

    @MustBeInvokedByOverriders
    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        isLocked = false;
    }

    private boolean isDataItemFacing(@Nullable Direction direction) {
        return direction == getFrontFacing() || direction == getFrontFacing().getOpposite();
    }

    private class DataItemHandler extends NotifiableItemStackHandler {

        public DataItemHandler(MetaMachine metaTileEntity) {
            super(metaTileEntity, 1, IO.BOTH, IO.BOTH, size -> new CustomItemStackHandler(size) {

                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }
            });
            capabilityValidator = ObjectHolderMachine.this::isDataItemFacing;
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
            if (!isLocked) {
                return super.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isLocked) {
                return super.insertItem(slot, stack, simulate);
            }
            return stack;
        }

        // only allow data items
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

            return isDataItem;
        }

        @Override
        public boolean handleRecipe(IO io, GTRecipe recipe, List<ItemIngredient> left, boolean simulate) {
            if (io == IO.OUT && simulate) {
                return true;
            }
            boolean result = super.handleRecipe(io, recipe, left, simulate);
            if (result && !simulate) {
                isLocked = (io == IO.IN);
            }
            return result;
        }
    }
}
