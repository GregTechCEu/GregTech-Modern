package com.gregtechceu.gtceu.integration.ae2.machines;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.helpers.externalstorage.GenericStackInv;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEItemGridWidget;
import com.gregtechceu.gtceu.integration.ae2.util.SerializableGenericStackInv;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * @Author GlodBlock
 * @Description The Output Bus that can directly send its contents to ME storage network.
 * @Date 2023/4/19-20:37
 */
public class MEOutputBusPartMachine extends MEBusPartMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEOutputBusPartMachine.class, MEBusPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private final SerializableGenericStackInv internalBuffer;
    private final InaccessibleInfiniteSlot inaccessibleInfiniteSlot;

    public MEOutputBusPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.OUT, args);
        this.workingEnabled = true;
        this.internalBuffer = new SerializableGenericStackInv(this::onChanged, 16);
        this.inaccessibleInfiniteSlot = new InaccessibleInfiniteSlot(this, this.internalBuffer);
    }

    @Override
    public void autoIO() {
        if (getLevel().isClientSide) return;
        this.meUpdateTick++;

        if (this.workingEnabled && this.shouldSyncME()) {
            if (this.updateMEStatus()) {
                if (!this.internalBuffer.isEmpty()) {
                    MEStorage aeNetwork = this.getMainNode().getGrid().getStorageService().getInventory();
                    for (int slot = 0; slot < this.internalBuffer.size(); ++slot) {
                        GenericStack item = this.internalBuffer.getStack(slot);
                        long notInserted = aeNetwork.insert(item.what(), item.amount(), Actionable.MODULATE, this.actionSource);
                        if (notInserted > 0) {
                            item = new GenericStack(item.what(), notInserted);
                        } else {
                            item = new GenericStack(item.what(), 0);
                        }
                        this.internalBuffer.setStack(slot, item);
                    }
                }
                this.updateInventorySubscription();
            }
        }
    }

    @Override
    public void onUnload() {
        if (this.getMainNode().getGrid() == null) return;
        MEStorage aeNetwork = this.getMainNode().getGrid().getStorageService().getInventory();
        for (int slot = 0; slot < this.internalBuffer.size(); ++slot) {
            GenericStack stack = this.internalBuffer.getStack(slot);
            if (stack == null) continue;
            aeNetwork.insert(stack.what(), stack.amount(), Actionable.MODULATE, this.actionSource);
        }
        super.onUnload();
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        ModularUI modularUI = new ModularUI(176, 18 + 18 * 4 + 94, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(10, 5, getDefinition().getName()));
        // ME Network status
        modularUI.widget(new LabelWidget(10, 15, () -> this.isOnline ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));
        modularUI.widget(new LabelWidget(10, 25, "gtceu.gui.waiting_list"));
        // Config slots
        modularUI.widget(new AEItemGridWidget(10, 35, 3, this.internalBuffer));

        modularUI.widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 18 + 18 * 4 + 12, true));
        return modularUI;
    }

    private static class InaccessibleInfiniteSlot extends MachineTrait implements IItemTransfer {
        protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(InaccessibleInfiniteSlot.class);

        private final GenericStackInv internalBuffer;

        public InaccessibleInfiniteSlot(MetaMachine holder, GenericStackInv internalBuffer) {
            super(holder);
            holder.attachTraits(this);
            this.internalBuffer = internalBuffer;
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            GenericStack stack1 = GenericStack.fromItemStack(stack);
            this.internalBuffer.insert(slot, stack1.what(), stack1.amount(), Actionable.MODULATE);
            this.machine.onChanged();
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (!simulate) {
                GenericStack stack1 = GenericStack.fromItemStack(stack);
                this.internalBuffer.insert(stack1.what(), stack1.amount(), Actionable.MODULATE, this.machine instanceof MEBusPartMachine host ? host.actionSource : IActionSource.empty());
                this.machine.onChanged();
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
            return null;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE - 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        @NotNull
        @Override
        public Object createSnapshot() {
            return null;
        }

        @Override
        public void restoreFromSnapshot(Object snapshot) {

        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }
    }

}
