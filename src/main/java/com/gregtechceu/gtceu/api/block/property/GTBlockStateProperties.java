package com.gregtechceu.gtceu.api.block.property;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class GTBlockStateProperties {

    public static final EnumProperty<Direction> UPWARDS_FACING = EnumProperty.create("upwards_facing",
            Direction.class, Direction.Plane.HORIZONTAL);
    public static final EnumProperty<Direction> NORTH_ONLY_FACING = EnumProperty.create("facing",
            Direction.class, Direction.NORTH);
    public static final EnumProperty<Direction> VERTICAL_FACING = EnumProperty.create("facing",
            Direction.class, Direction.Plane.VERTICAL);

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty BLOOM = BooleanProperty.create("bloom");
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");
}
