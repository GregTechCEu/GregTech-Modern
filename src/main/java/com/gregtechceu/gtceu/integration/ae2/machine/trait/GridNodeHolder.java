package com.gregtechceu.gtceu.integration.ae2.machine.trait;

import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.utils.SerializableManagedGridNode;

import net.minecraft.core.Direction;

import appeng.api.networking.GridFlags;
import appeng.me.helpers.BlockEntityNodeListener;
import lombok.Getter;

import java.util.EnumSet;

/**
 * A MachineTrait that is only used for hosting a grid node and does not provide grid node capability,
 * because {@link IGridConnectedMachine} already extends {@link appeng.api.networking.IInWorldGridNodeHost}.
 *
 * @see appeng.api.networking.IInWorldGridNodeHost
 * @see IGridConnectedMachine
 */
public class GridNodeHolder extends MachineTrait {

    public static final MachineTraitType<GridNodeHolder> TYPE = new MachineTraitType<>(GridNodeHolder.class);

    @Override
    public MachineTraitType<GridNodeHolder> getTraitType() {
        return TYPE;
    }

    @Getter
    @SaveField
    protected final SerializableManagedGridNode mainNode;

    public GridNodeHolder(IGridConnectedMachine machine) {
        super();
        this.mainNode = createManagedNode(machine);
    }

    protected SerializableManagedGridNode createManagedNode(IGridConnectedMachine machine) {
        return (SerializableManagedGridNode) new SerializableManagedGridNode(machine,
                BlockEntityNodeListener.INSTANCE)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .setVisualRepresentation(machine.self().getDefinition().getItem())
                .setIdlePowerUsage(ConfigHolder.INSTANCE.compat.ae2.meHatchEnergyUsage)
                .setInWorldNode(true)
                .setExposedOnSides(
                        machine.self().hasFrontFacing() ? EnumSet.of(machine.self().getFrontFacing()) :
                                EnumSet.allOf(Direction.class))
                .setTagName("proxy");
    }

    protected void createMainNode() {
        this.mainNode.create(getLevel(), getBlockPos());
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        getMachine().scheduleForNextServerTick(this::createMainNode);
    }

    @Override
    public void onMachineUnload() {
        super.onMachineUnload();
        mainNode.destroy();
    }
}
