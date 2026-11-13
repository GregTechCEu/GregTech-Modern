package com.gregtechceu.gtceu.common.datafixer.fixes;

import net.minecraft.core.Direction;
import net.minecraft.util.datafix.fixes.References;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class UpwardsFacingPropertyFix extends DataFix {

    public UpwardsFacingPropertyFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("UpwardsFacingPropertyFix",
                this.getInputSchema().getType(References.BLOCK_STATE),
                typed -> typed.update(DSL.remainderFinder(), UpwardsFacingPropertyFix::upgradeBlockStateTag));
    }

    private static <T> Dynamic<T> fixUpwardsFacingProperty(Direction frontFacing, Dynamic<T> property) {
        // we can ignore the remainder from this because it'll always be empty
        // (Direction.CODEC only accepts strings and those can't have 'unread'/leftover fields)
        Optional<Direction> upwardsFacingOpt = property.read(Direction.CODEC).result();
        if (upwardsFacingOpt.isEmpty()) {
            return property;
        }
        Direction upwardsFacing = upwardsFacingOpt.get();
        upwardsFacing = fixUpwardsFacing(frontFacing, upwardsFacing);

        return Direction.CODEC.encodeStart(property.getOps(), upwardsFacing)
                .resultOrPartial(str -> {})
                .map(value -> new Dynamic<>(property.getOps(), value))
                .orElse(property);
    }

    private static <T> Dynamic<T> upgradeBlockStateTag(Dynamic<T> blockState) {
        return blockState.update("Properties", properties -> {
            OptionalDynamic<?> frontFacingProperty = properties.get("facing");
            Optional<Direction> frontFacingOpt = frontFacingProperty.read(Direction.CODEC).resultOrPartial(str -> {});
            if (frontFacingOpt.isEmpty()) {
                return properties;
            }
            Direction frontFacing = frontFacingOpt.get();

            return properties.update("upwards_facing", property -> fixUpwardsFacingProperty(frontFacing, property));
        });
    }

    public static Direction fixUpwardsFacing(Direction frontFacing, @Nullable Direction upwardsFacing) {
        if (upwardsFacing == null) {
            // needed if previous machine did not have an upwards facing
            if (frontFacing.getAxis() == Direction.Axis.Y) {
                // if the machine is facing up or down, make its upward face default to NORTH
                return Direction.NORTH;
            } else {
                // in all other cases, default to UP
                return Direction.UP;
            }
        }

        return switch (frontFacing) {
            case NORTH -> switch (upwardsFacing) {
                case NORTH -> Direction.UP;
                case EAST -> Direction.WEST;
                case SOUTH -> Direction.DOWN;
                case WEST -> Direction.EAST;
                default -> Direction.UP;
            };
            case EAST -> switch (upwardsFacing) {
                case NORTH -> Direction.UP;
                case EAST -> Direction.NORTH;
                case SOUTH -> Direction.DOWN;
                case WEST -> Direction.SOUTH;
                default -> Direction.UP;
            };
            case SOUTH -> switch (upwardsFacing) {
                case NORTH -> Direction.UP;
                case EAST -> Direction.EAST;
                case SOUTH -> Direction.DOWN;
                case WEST -> Direction.WEST;
                default -> Direction.UP;
            };
            case WEST -> switch (upwardsFacing) {
                case NORTH -> Direction.UP;
                case EAST -> Direction.SOUTH;
                case SOUTH -> Direction.DOWN;
                case WEST -> Direction.NORTH;
                default -> Direction.UP;
            };
            case UP, DOWN -> upwardsFacing;
        };
    }
}
