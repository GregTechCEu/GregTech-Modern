package com.gregtechceu.gtceu.integration.ae2.machine.trait;

import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.utils.SerializableManagedGridNode;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.ReadOnlyManaged;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

import appeng.api.networking.GridFlags;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;
import lombok.Getter;

import java.util.EnumSet;

/**
 * A MachineTrait that is only used for hosting grid node and does not provide grid node capability.
 * Because IGridConnectedMachine has already extended IInWorldGridNodeHost.
 *
 * @author GateGuardian
 * @date : 2024/7/14
 */
public class GridNodeHost extends MachineTrait {

    protected final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(GridNodeHost.class);

    @Getter
    @Persisted
    @ReadOnlyManaged(onDirtyMethod = "onGridNodeDirty",
                     serializeMethod = "serializeGridNode",
                     deserializeMethod = "deserializeGridNode")
    protected final SerializableManagedGridNode mainNode = createMainNode();

    public GridNodeHost(IGridConnectedMachine machine) {
        super(machine.self());
    }

    protected SerializableManagedGridNode createMainNode() {
        return (SerializableManagedGridNode) new SerializableManagedGridNode((IGridConnectedBlockEntity) machine,
                BlockEntityNodeListener.INSTANCE)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setVisualRepresentation(machine.getDefinition().getItem())
                .setIdlePowerUsage(ConfigHolder.INSTANCE.compat.ae2.meHatchEnergyUsage)
                .setInWorldNode(true)
                .setExposedOnSides(
                        machine.hasFrontFacing() ? EnumSet.of(machine.getFrontFacing()) :
                                EnumSet.allOf(Direction.class))
                .setTagName("proxy");
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        if (machine.getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::createManagedNode));
        }
    }

    @Override
    public void onMachineUnLoad() {
        super.onMachineUnLoad();
        mainNode.destroy();
    }

    protected void createManagedNode() {
        this.mainNode.create(machine.getLevel(), machine.getPos());
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
