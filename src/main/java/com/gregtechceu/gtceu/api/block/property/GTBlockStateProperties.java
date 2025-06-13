package com.gregtechceu.gtceu.api.block.property;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class GTBlockStateProperties {

    public static final DirectionProperty UPWARDS_FACING = DirectionProperty.create("upwards_facing",
            Direction.Plane.HORIZONTAL);
    public static final DirectionProperty NORTH_ONLY_FACING = DirectionProperty.create("facing", Direction.NORTH);
    public static final DirectionProperty VERTICAL_FACING = DirectionProperty.create("facing",
            Direction.Plane.VERTICAL);

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty BLOOM = BooleanProperty.create("bloom");
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");
}
