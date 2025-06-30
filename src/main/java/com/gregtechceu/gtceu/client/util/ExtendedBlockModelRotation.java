package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.AccessLevel;
import lombok.Getter;
import org.joml.Quaternionf;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * All possible rotations for a fully orientable block.
 * <p>
 * This code is from
 * <a href=
 * "https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/forge/1.20.1/src/main/java/appeng/api/orientation/BlockOrientation.java">Applied
 * Energistics 2</a>,
 * licensed as LGPL 3.0.
 */
public enum ExtendedBlockModelRotation implements ModelState {

    // DUNSWE -> SWNE
    DOWN_SOUTH(90, 0, 180),
    DOWN_WEST(90, 0, 270),
    DOWN_NORTH(90, 0, 0),
    DOWN_EAST(90, 0, 90),

    UP_SOUTH(270, 0, 0),
    UP_WEST(270, 0, 90),
    UP_NORTH(270, 0, 180),
    UP_EAST(270, 0, 270),

    NORTH_SOUTH(0, 0, 180),
    NORTH_WEST(0, 0, 270),
    NORTH_NORTH(0, 0, 0), // Default
    NORTH_EAST(0, 0, 90),

    SOUTH_SOUTH(0, 180, 180),
    SOUTH_WEST(0, 180, 270),
    SOUTH_NORTH(0, 180, 0),
    SOUTH_EAST(0, 180, 90),

    WEST_SOUTH(0, 270, 180),
    WEST_WEST(0, 270, 270),
    WEST_NORTH(0, 270, 0),
    WEST_EAST(0, 270, 90),

    EAST_SOUTH(0, 90, 180),
    EAST_WEST(0, 90, 270),
    EAST_NORTH(0, 90, 0),
    EAST_EAST(0, 90, 90);

    public static final ExtendedBlockModelRotation[] VALUES = values();

    private static final int DEGREES = 360;
    private static final int DEGREES_SQ = DEGREES * DEGREES;
    private static final Int2ObjectMap<ExtendedBlockModelRotation> BY_INDEX = Arrays.stream(VALUES).collect(
            Collectors.toMap(ExtendedBlockModelRotation::getIndex, Function.identity(),
                    ExtendedBlockModelRotation::mergeError, Int2ObjectLinkedOpenHashMap::new));

    @Getter
    private final int angleX;
    @Getter
    private final int angleY;
    @Getter
    private final int angleZ;
    @Getter
    private final Transformation rotation;
    @Getter(AccessLevel.PRIVATE)
    private final int index;

    ExtendedBlockModelRotation(int angleX, int angleY, int angleZ) {
        this.index = getIndex(angleX, angleY, angleZ);

        this.angleX = angleX;
        this.angleY = angleY;
        this.angleZ = angleZ;

        if (angleX == 0 && angleY == 0 && angleZ == 0) {
            this.rotation = Transformation.identity();
        } else {
            // NOTE: Mojang's block model rotation rotates in the opposite direction
            Quaternionf quaternion = new Quaternionf().rotateYXZ(
                    -angleY * Mth.DEG_TO_RAD,
                    -angleX * Mth.DEG_TO_RAD,
                    -angleZ * Mth.DEG_TO_RAD);
            this.rotation = new Transformation(null, quaternion, null, null);
        }
    }

    /**
     * Gets the block orientation in which the block's front and top are facing the specified directions.
     */
    public static ExtendedBlockModelRotation get(Direction frontFacing, Direction upwardsFacing) {
        return VALUES[frontFacing.get3DDataValue() * 4 + upwardsFacing.get2DDataValue()];
    }

    private static int getIndex(int x, int y, int z) {
        return x * DEGREES_SQ + y * DEGREES + z;
    }

    public static ExtendedBlockModelRotation by(int x, int y, int z) {
        return BY_INDEX.get(getIndex(
                Math.floorMod(x, DEGREES),
                Math.floorMod(y, DEGREES),
                Math.floorMod(z, DEGREES)));
    }

    private static ExtendedBlockModelRotation mergeError(ExtendedBlockModelRotation o, ExtendedBlockModelRotation n) {
        throw new IllegalStateException(String.format(
                "Duplicate key %s (attempted merging values %s and %s)", o.getIndex(), o, n));
    }
}
