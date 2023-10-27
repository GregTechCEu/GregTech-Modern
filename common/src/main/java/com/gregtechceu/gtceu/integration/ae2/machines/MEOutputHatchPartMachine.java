package com.gregtechceu.gtceu.integration.ae2.machines;

import appeng.api.config.Actionable;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEFluidConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEFluidGridWidget;
import com.gregtechceu.gtceu.integration.ae2.util.ExportOnlyAESlot;
import com.gregtechceu.gtceu.integration.ae2.util.SerializableGenericStackInv;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MEOutputHatchPartMachine extends MEHatchPartMachine implements IInWorldGridNodeHost, IGridConnectedBlockEntity {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEOutputHatchPartMachine.class, MEHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private final SerializableGenericStackInv internalBuffer;

    public MEOutputHatchPartMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
        this.internalBuffer = new SerializableGenericStackInv(this::onChanged, 16);
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
        modularUI.widget(new AEFluidGridWidget(16, 25, 3, this.internalBuffer));

        modularUI.widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 18 + 18 * 4 + 12, true));
        return modularUI;
    }

    @Override
    protected void autoIO() {
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
                this.updateTankSubscription();
            }
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
