package com.gregtechceu.gtceu.integration.ae2.machines;

import appeng.api.networking.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.util.ExportOnlyAESlot;
import com.gregtechceu.gtceu.integration.ae2.util.SerializableManagedGridNode;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.integration.ae2.machines.MEBusPartMachine.ME_UPDATE_INTERVAL;

public abstract class MEHatchPartMachine extends FluidHatchPartMachine implements IInWorldGridNodeHost, IGridConnectedBlockEntity {
    protected final static int CONFIG_SIZE = 16;

    @Getter
    private final IManagedGridNode mainNode = createMainNode()
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setVisualRepresentation(getDefinition().getItem())
            .setInWorldNode(true)
            .setTagName("proxy");
    protected final IActionSource actionSource = IActionSource.ofMachine(mainNode::getNode);
    @DescSynced
    @RequireRerender
    protected boolean isOnline;
    protected int meUpdateTick;
    private IGrid aeProxy;

    public MEHatchPartMachine(IMachineBlockEntity holder, IO io, Object... args) {
        super(holder, GTValues.UHV, io, args);
    }

    protected boolean shouldSyncME() {
        return this.meUpdateTick % ME_UPDATE_INTERVAL == 0;
    }

    /**
     * Update me network connection status.
     * @return the updated status.
     */
    public boolean updateMEStatus() {
        if (this.aeProxy != null) {
            this.aeProxy = this.mainNode.getGrid();
            this.isOnline = this.mainNode.isOnline() && this.mainNode.isPowered();
        } else {
            this.isOnline = false;
        }
        return this.isOnline;
    }

    protected IManagedGridNode createMainNode() {
        return new SerializableManagedGridNode(this, BlockEntityNodeListener.INSTANCE);
    }

    protected void updateTankSubscription() {
        if (isWorkingEnabled() && ((io == IO.OUT && !tank.isEmpty()) || io == IO.IN)
                && GridHelper.getNodeHost(getLevel(), getPos().relative(getFrontFacing())) != null) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    @Override
    public void saveChanges() {
        this.onChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::createManagedNode));
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        mainNode.destroy();
    }

    @Override
    public void onChanged() {
        super.onChanged();
        this.updateTankSubscription();
    }

    protected void createManagedNode() {
        this.mainNode.create(this.getLevel(), this.getPos());
    }

}
