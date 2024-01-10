package com.gregtechceu.gtceu.api.data;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import java.util.function.Predicate;

public enum RotationState implements Predicate<Direction> {
    ALL(dir -> true, Direction.NORTH, BlockStateProperties.FACING),
    NONE(dir -> false, Direction.NORTH, DirectionProperty.create("facing", Direction.NORTH)),
    Y_AXIS(dir -> dir.getAxis() == Direction.Axis.Y, Direction.UP, DirectionProperty.create("facing", Direction.Plane.VERTICAL)),
    NON_Y_AXIS(dir -> dir.getAxis() != Direction.Axis.Y, Direction.NORTH, BlockStateProperties.HORIZONTAL_FACING);

    final Predicate<Direction> predicate;
    public final Direction defaultDirection;
    public final DirectionProperty property;

    RotationState(Predicate<Direction> predicate, Direction defaultDirection, DirectionProperty property){
        this.predicate = predicate;
        this.defaultDirection = defaultDirection;
        this.property = property;
    }

    static final ThreadLocal<RotationState> STATE = new ThreadLocal<>();
    public static RotationState get() {
        return STATE.get();
    }

    public static void set(RotationState state) {
        STATE.set(state);
    }

    public static void clear() {
        STATE.remove();
    }

    @Override
    public boolean test(Direction dir) {
        return predicate.test(dir);
    }
}