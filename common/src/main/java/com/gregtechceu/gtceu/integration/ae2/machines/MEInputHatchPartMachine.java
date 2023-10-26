package com.gregtechceu.gtceu.integration.ae2.machines;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEFluidConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEItemConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.util.ExportOnlyAESlot;
import com.gregtechceu.gtceu.integration.ae2.util.SerializableManagedGridNode;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MEInputHatchPartMachine extends MEHatchPartMachine implements IInWorldGridNodeHost, IGridConnectedBlockEntity {

    @Persisted
    private ExportOnlyAEFluid[] aeFluidTanks;

    public MEInputHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
    }

    @Override
    protected NotifiableFluidTank createTank(Object... args) {
        this.aeFluidTanks = new ExportOnlyAEFluid[CONFIG_SIZE];
        for (int i = 0; i < CONFIG_SIZE; i ++) {
            this.aeFluidTanks[i] = new ExportOnlyAEFluid(this, null, null);
        }
        return super.createTank(args);
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

        // Config slots
        modularUI.widget(new AEFluidConfigWidget(16, 25, this.aeFluidTanks));

        modularUI.widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 18 + 18 * 4 + 12, true));
        return modularUI;
    }

    @Override
    protected void autoIO() {
        if (getLevel().isClientSide) return;
        this.meUpdateTick++;

        if (this.workingEnabled && this.shouldSyncME()) {
            if (this.updateMEStatus()) {
                MEStorage aeNetwork = this.getMainNode().getGrid().getStorageService().getInventory();
                for (ExportOnlyAEFluid aeTank : this.aeFluidTanks) {
                    // Try to clear the wrong fluid
                    GenericStack exceedFluid = aeTank.exceedStack();
                    if (exceedFluid != null) {
                        long total = exceedFluid.amount();
                        long inserted = aeNetwork.insert(exceedFluid.what(), exceedFluid.amount(), Actionable.MODULATE, this.actionSource);
                        if (inserted > 0) {
                            aeTank.drain((int) (total - inserted), true);
                            continue;
                        } else {
                            aeTank.drain((int) total, true);
                        }
                    }
                    // Fill it
                    GenericStack reqFluid = aeTank.requestStack();
                    if (reqFluid != null) {
                        long extracted = aeNetwork.extract(reqFluid.what(), reqFluid.amount(), Actionable.MODULATE, this.actionSource);
                        if (extracted > 0) {
                            aeTank.addStack(new GenericStack(reqFluid.what(), extracted));
                        }
                    }
                }
                this.updateTankSubscription();
            }
        }
    }

    public static class ExportOnlyAEFluid extends ExportOnlyAESlot implements IFluidStorage, IFluidTransfer {
        private MetaMachine holder;

        public ExportOnlyAEFluid(MetaMachine holder, GenericStack config, GenericStack stock) {
            super(config, stock);
            this.holder = holder;
        }

        public ExportOnlyAEFluid() {
            super();
        }

        @Override
        public void addStack(GenericStack stack) {
            if (this.stock == null) {
                this.stock = stack;
            } else {
                this.stock = GenericStack.sum(this.stock, stack);
            }
            trigger();
        }

        @Override
        public FluidStack getFluid() {
            if (this.stock != null && this.stock.what() instanceof AEFluidKey fluidKey) {
                return FluidStack.create(fluidKey.getFluid(), this.stock.amount(), fluidKey.getTag());
            }
            return FluidStack.empty();
        }

        @Override
        public void setFluid(FluidStack fluid) {

        }

        @Override
        public long getFluidAmount() {
            return this.stock != null ? this.stock.amount() : 0;
        }

        @Override
        public long getCapacity() {
            // Its capacity is always 0.
            return 0;
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return false;
        }

        @Override
        public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            return 0;
        }

        @Override
        public boolean supportsFill(int tank) {
            return false;
        }

        @Override
        public boolean supportsDrain(int tank) {
            return tank == 0;
        }

        @Override
        public long fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain, boolean notifyChanges) {
            if (this.getFluid().isFluidEqual(resource)) {
                return this.drain(resource.getAmount(), doDrain);
            }
            return FluidStack.empty();
        }

        @Override
        public FluidStack drain(int tank, FluidStack maxDrain, boolean simulate, boolean notifyChanges) {
            if (this.stock == null || !(this.stock.what() instanceof AEFluidKey fluidKey)) {
                return FluidStack.empty();
            }
            int drained = (int) Math.min(this.stock.amount(), maxDrain.getAmount());
            FluidStack result = FluidStack.create(fluidKey.getFluid(), drained, fluidKey.getTag());
            if (!simulate) {
                this.stock = new GenericStack(this.stock.what(), this.stock.amount() - drained);
                if (this.stock.amount() == 0) {
                    this.stock = null;
                }
                if (notifyChanges) trigger();
            }
            return result;
        }

        @NotNull
        @Override
        public Object createSnapshot() {
            return Pair.of(this.config, this.stock);
        }

        @Override
        public void restoreFromSnapshot(Object snapshot) {
            if (snapshot instanceof Pair<?,?> pair) {
                this.config = (GenericStack) pair.getFirst();
                this.stock = (GenericStack) pair.getSecond();
            }
        }

        private void trigger() {
            if (holder != null) {
                holder.markDirty();
            }
        }

        @Override
        public ExportOnlyAEFluid copy() {
            return new ExportOnlyAEFluid(
                    this.holder,
                    this.config == null ? null : ExportOnlyAESlot.copy(this.config),
                    this.stock == null ? null : ExportOnlyAESlot.copy(this.stock)
            );
        }
    }
}
