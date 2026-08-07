package com.gregtechceu.gtceu.api.data;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import java.util.function.Predicate;

public enum RotationState implements Predicate<Direction> {

    ALL(dir -> true, Direction.NORTH, BlockStateProperties.FACING),
    NONE(dir -> false, Direction.NORTH, GTBlockStateProperties.NORTH_ONLY_FACING),
    Y_AXIS(dir -> dir.getAxis() == Direction.Axis.Y, Direction.UP, GTBlockStateProperties.VERTICAL_FACING),
    NON_Y_AXIS(dir -> dir.getAxis() != Direction.Axis.Y, Direction.NORTH, BlockStateProperties.HORIZONTAL_FACING);

    final Predicate<Direction> predicate;
    public final Direction defaultDirection;
    public final DirectionProperty property;

    RotationState(Predicate<Direction> predicate, Direction defaultDirection, DirectionProperty property) {
        this.predicate = predicate;
        this.defaultDirection = defaultDirection;
        this.property = property;
    }

    @Override
    public boolean test(Direction dir) {
        return predicate.test(dir);
    }
}
