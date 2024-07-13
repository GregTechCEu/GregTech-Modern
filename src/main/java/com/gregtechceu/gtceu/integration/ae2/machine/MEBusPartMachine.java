package com.gregtechceu.gtceu.integration.ae2.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.ae2.util.SerializableManagedGridNode;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

import appeng.api.networking.*;
import appeng.api.networking.security.IActionSource;
import appeng.me.helpers.BlockEntityNodeListener;
import lombok.Getter;

import java.util.EnumSet;

public abstract class MEBusPartMachine extends ItemBusPartMachine
                                       implements IInWorldGridNodeHost, IGridConnectedMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEBusPartMachine.class,
            ItemBusPartMachine.MANAGED_FIELD_HOLDER);
    public final static int ME_UPDATE_INTERVAL = ConfigHolder.INSTANCE.compat.ae2.updateIntervals;

    @Getter
    @Persisted
    @ReadOnlyManaged(onDirtyMethod = "onGridNodeDirty",
                     serializeMethod = "serializeGridNode",
                     deserializeMethod = "deserializeGridNode")
    private final SerializableManagedGridNode mainNode = (SerializableManagedGridNode) createMainNode()
            .setFlags(GridFlags.REQUIRE_CHANNEL)
            .setVisualRepresentation(getDefinition().getItem())
            .setIdlePowerUsage(ConfigHolder.INSTANCE.compat.ae2.meHatchEnergyUsage)
            .setInWorldNode(true)
            .setExposedOnSides(
                    this.hasFrontFacing() ? EnumSet.of(this.getFrontFacing()) : EnumSet.allOf(Direction.class))
            .setTagName("proxy");
    protected final IActionSource actionSource = IActionSource.ofMachine(mainNode::getNode);

    @DescSynced
    @Getter @Setter
    protected boolean isOnline;

    private IGrid aeProxy;

    public MEBusPartMachine(IMachineBlockEntity holder, IO io, Object... args) {
        super(holder, GTValues.UHV, io, args);
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        getMainNode().setExposedOnSides(EnumSet.of(newFacing));
    }

    protected void updateInventorySubscription() {
        if (isWorkingEnabled() && ((io == IO.OUT && !getInventory().isEmpty()) || io == IO.IN) &&
                this.getLevel() != null &&
                GridHelper.getNodeHost(getLevel(), getPos().relative(getFrontFacing())) != null) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    protected IManagedGridNode createMainNode() {
        return new SerializableManagedGridNode(this, BlockEntityNodeListener.INSTANCE);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::createManagedNode));
            inventorySubs = getInventory().addChangedListener(this::updateInventorySubscription);
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
        updateInventorySubscription();
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
