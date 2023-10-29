package com.gregtechceu.gtceu.integration.ae2.machine;

import appeng.api.networking.*;
import appeng.api.networking.security.IActionSource;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.ae2.util.SerializableManagedGridNode;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

import java.util.EnumSet;

import static com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine.ME_UPDATE_INTERVAL;

public abstract class MEHatchPartMachine extends FluidHatchPartMachine implements IInWorldGridNodeHost, IGridConnectedBlockEntity {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEHatchPartMachine.class, FluidHatchPartMachine.MANAGED_FIELD_HOLDER);
    protected final static int CONFIG_SIZE = 16;


    @Getter
    @Persisted @ReadOnlyManaged(onDirtyMethod = "onGridNodeDirty", serializeMethod = "serializeGridNode", deserializeMethod = "deserializeGridNode")
    private final SerializableManagedGridNode mainNode = (SerializableManagedGridNode) createMainNode()
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setVisualRepresentation(getDefinition().getItem())
            .setIdlePowerUsage(ConfigHolder.INSTANCE.compat.ae2.meHatchEnergyUsage)
            .setInWorldNode(true)
            .setExposedOnSides(this.hasFrontFacing() ? EnumSet.of(this.getFrontFacing()) : EnumSet.allOf(Direction.class))
            .setTagName("proxy");
    protected final IActionSource actionSource = IActionSource.ofMachine(mainNode::getNode);
    @DescSynced
    protected boolean isOnline;
    private IGrid aeProxy;

    public MEHatchPartMachine(IMachineBlockEntity holder, IO io, Object... args) {
        super(holder, GTValues.UHV, io, args);
    }

    protected boolean shouldSyncME() {
        return this.getOffsetTimer() % ME_UPDATE_INTERVAL == 0;
    }

    @Override
    public void setFrontFacing(Direction facing) {
        super.setFrontFacing(facing);
        if (isFacingValid(facing)) {
            this.mainNode.setExposedOnSides(this.hasFrontFacing() ? EnumSet.of(facing) : EnumSet.allOf(Direction.class));
        }
    }

    /**
     * Update me network connection status.
     * @return the updated status.
     */
    public boolean updateMEStatus() {
        if (this.aeProxy == null) {
            this.aeProxy = this.mainNode.getGrid();
        }
        if (this.aeProxy != null) {
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
        if (isWorkingEnabled() && ((io == IO.OUT && !tank.isEmpty()) || io == IO.IN) && getLevel() != null
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

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @SuppressWarnings("unused")
    public boolean onGridNodeDirty(SerializableManagedGridNode node) {
        return node != null && node.isActive() && node.isOnline();
    }

    @SuppressWarnings("unused")
    public CompoundTag serializeGridNode(SerializableManagedGridNode node) {
        return node.serializeNBT();
    }

    @SuppressWarnings("unused")
    public SerializableManagedGridNode deserializeGridNode(CompoundTag tag) {
        this.mainNode.deserializeNBT(tag);
        return this.mainNode;
    }
}
