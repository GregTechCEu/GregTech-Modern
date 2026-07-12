package com.gregtechceu.gtceu.common.machine.multiblock.part.monitor;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import brachy.modularui.api.drawable.IDrawable;
import org.joml.Vector2d;

public class MonitorPartMachine extends MonitorComponentPartMachine {

    public MonitorPartMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public boolean isMonitor() {
        return true;
    }

    @Override
    public IDrawable getIcon() {
        return GTGuiTextures.MONITOR;
    }

    public Vector2d getMousePos(HitResult hitResult) {
        if (hitResult instanceof BlockHitResult hit) {
            Direction direction = RelativeDirection.RIGHT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                    false);
            double x = hit.getLocation().get(direction.getAxis());
            if (direction.getAxisDirection().getStep() == 1) {
                x = 1 - x;
            }
            double y = hit.getLocation()
                    .get(getFrontFacing().getAxis().isVertical() ? Direction.Axis.X : Direction.Axis.Y);
            x -= Math.floor(x);
            if (x < 0) x++;
            y -= Math.floor(y);
            if (y < 0) y++;
            return new Vector2d(x, y);
        } else return new Vector2d();
    }
}
