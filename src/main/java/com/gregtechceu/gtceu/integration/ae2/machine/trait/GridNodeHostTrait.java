package com.gregtechceu.gtceu.integration.ae2.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.IGridConnectedBlockEntity;

public class GridNodeHostTrait extends MachineTrait implements IGridConnectedBlockEntity {

    protected final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(GridNodeHostTrait.class);
    private final IManagedGridNode proxy;

    public GridNodeHostTrait(MetaMachine machine) {
        super(machine);
        this.proxy = GridHelper.createManagedNode(this, BlockEntityNodeListener.INSTANCE)
                .setInWorldNode(true)
                .setVisualRepresentation(machine.getDefinition().getItem());
    }

    public void init() {
        this.proxy.create(machine.getLevel(), machine.getPos());
    }

    @Override
    public IManagedGridNode getMainNode() {
        return proxy;
    }

    @Override
    public void saveChanges() {
        machine.onChanged();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
