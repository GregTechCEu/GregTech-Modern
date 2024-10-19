package com.gregtechceu.gtceu.api.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * @author KilaBash
 * @date 2023/3/12
 * @implNote BlockProperties
 */
public final class BlockProperties {

    public static final BooleanProperty SERVER_TICK = BooleanProperty.create("server_tick");
    public static final DirectionProperty UPWARDS_FACING_PROPERTY = DirectionProperty.create("upwards_facing",
            Direction.Plane.HORIZONTAL);
}
