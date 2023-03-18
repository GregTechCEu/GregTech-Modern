package com.lowdragmc.gtceu.api.machine.feature;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * @author KilaBash
 * @date 2023/3/15
 * @implNote ISteamVentMachine
 */
public interface ISteamVentMachine extends IMachineFeature{
    Direction getVentFacing();
    void setVentFacing(Direction ventFacing);

    default boolean isVentingStuck() {
        var machinePos = self().getPos();
        var ventingSide = getVentFacing();
        var ventingBlockPos = machinePos.relative(ventingSide);
        var blockOnPos = self().getLevel().getBlockState(ventingBlockPos);
        return blockOnPos.canOcclude() || Shapes.blockOccudes(blockOnPos.getCollisionShape(self().getLevel(), ventingBlockPos), Shapes.block(), ventingSide.getOpposite());
    }
}
