package com.gregtechceu.gtceu.client.instance;

import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.kinetic.IKineticMachine;
import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityInstance;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingData;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;

/**
 * @author KilaBash
 * @date 2023/4/1
 * @implNote SplitShaftInstance
 */
public class SplitShaftInstance extends KineticBlockEntityInstance<KineticMachineBlockEntity> {

    protected final ArrayList<RotatingData> keys;

    public SplitShaftInstance(MaterialManager modelManager, KineticMachineBlockEntity tile) {
        super(modelManager, tile);

        keys = new ArrayList<>(2);

        float speed = tile.getSpeed();

        Material<RotatingData> rotatingMaterial = getRotatingMaterial();

        for (Direction dir : Iterate.directionsInAxis(getRotationAxis())) {

            Instancer<RotatingData> half = rotatingMaterial.getModel(AllPartialModels.SHAFT_HALF, blockState, dir);

            float splitSpeed = speed * (tile.getMetaMachine() instanceof IKineticMachine kineticMachine ? kineticMachine.getRotationSpeedModifier(dir) : 1);

            keys.add(setup(half.createInstance(), splitSpeed));
        }
    }

    @Override
    public void update() {
        Block block = blockState.getBlock();
        final Direction.Axis boxAxis = ((IRotate) block).getRotationAxis(blockState);

        Direction[] directions = Iterate.directionsInAxis(boxAxis);

        for (int i : Iterate.zeroAndOne) {
            updateRotation(keys.get(i), blockEntity.getSpeed() * (blockEntity.getMetaMachine() instanceof IKineticMachine kineticMachine ? kineticMachine.getRotationSpeedModifier(directions[i]) : 1));
        }
    }

    @Override
    public void updateLight() {
        relight(pos, keys.stream());
    }

    @Override
    public void remove() {
        keys.forEach(InstanceData::delete);
        keys.clear();
    }

}
