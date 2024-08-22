package com.gregtechceu.gtceu.common.pipelike.net.item;

import com.gregtechceu.gtceu.api.graphnet.pipenet.transfer.TransferControl;
import com.gregtechceu.gtceu.api.graphnet.pipenet.transfer.TransferControlProvider;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.ItemTestObject;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IItemTransferController {

    TransferControl<IItemTransferController> CONTROL = new TransferControl<>("Item") {

        @Override
        public @NotNull IItemTransferController get(@Nullable Object potentialHolder) {
            if (!(potentialHolder instanceof TransferControlProvider holder)) return DEFAULT;
            IItemTransferController found = holder.getControllerForControl(CONTROL);
            return found == null ? DEFAULT : found;
        }

        @Override
        public @NotNull IItemTransferController getNoPassage() {
            return NO_PASSAGE;
        }
    };

    IItemTransferController DEFAULT = new IItemTransferController() {};

    IItemTransferController NO_PASSAGE = new IItemTransferController() {

        @Override
        public int insertToHandler(@NotNull ItemTestObject testObject, int amount, @NotNull IItemTransfer destHandler,
                                   boolean simulate) {
            return amount;
        }

        @Override
        public int extractFromHandler(@NotNull ItemTestObject testObject, int amount,
                                      @NotNull IItemTransfer sourceHandler, boolean simulate) {
            return 0;
        }
    };

    /**
     * @return the amount left uninserted; aka the remainder
     */
    default int insertToHandler(@NotNull ItemTestObject testObject, int amount,
                                @NotNull IItemTransfer destHandler, boolean simulate) {
        int available = amount;
        for (int i = 0; i < destHandler.getSlots(); i++) {
            int allowed = Math.min(available, Math.min(destHandler.getSlotLimit(i), testObject.getStackLimit()));
            available -= allowed - destHandler.insertItem(i, testObject.recombine(allowed), simulate).getCount();
        }
        return available;
    }

    /**
     * @return the amount extracted
     */
    default int extractFromHandler(@NotNull ItemTestObject testObject, int amount,
                                   @NotNull IItemTransfer sourceHandler, boolean simulate) {
        int extracted = 0;
        for (int i = 0; i < sourceHandler.getSlots(); i++) {
            ItemStack stack = sourceHandler.extractItem(i, amount - extracted, true);
            if (testObject.test(stack)) {
                if (simulate) {
                    extracted += stack.getCount();
                } else {
                    extracted += sourceHandler.extractItem(i, amount - extracted, false).getCount();
                }

            }
        }
        return extracted;
    }
}
