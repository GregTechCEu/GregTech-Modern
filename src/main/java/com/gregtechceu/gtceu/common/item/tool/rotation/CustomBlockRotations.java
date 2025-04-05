package com.gregtechceu.gtceu.common.item.tool.rotation;

import com.gregtechceu.gtceu.api.capability.ICoverable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

public class CustomBlockRotations {

    private static final Map<Block, ICustomRotationBehavior> CUSTOM_BEHAVIOR_MAP = new Object2ObjectOpenHashMap<>();

    @ApiStatus.Internal
    public static void init() {
        // nice little way to initialize an inner-class enum
        CustomRotations.init();
    }

    public static void registerCustomRotation(Block block, ICustomRotationBehavior behavior) {
        CUSTOM_BEHAVIOR_MAP.put(block, behavior);
    }

    public static ICustomRotationBehavior getCustomRotation(Block block) {
        return CUSTOM_BEHAVIOR_MAP.get(block);
    }

    public static final ICustomRotationBehavior BLOCK_HORIZONTAL_BEHAVIOR = new ICustomRotationBehavior() {

        @Override
        public boolean customRotate(BlockState state, Level world, BlockPos pos, BlockHitResult hitResult) {
            Direction gridSide = ICoverable.determineGridSideHit(hitResult);
            if (gridSide == null) return false;
            if (gridSide.getAxis() == Direction.Axis.Y) return false;

            if (gridSide != state.getValue(HorizontalDirectionalBlock.FACING)) {
                state = state.setValue(HorizontalDirectionalBlock.FACING, gridSide);
                world.setBlockAndUpdate(pos, state);
                return true;
            }
            return false;
        }

        @Override
        public boolean showSideTip(BlockState state, Direction side) {
            return side.getAxis() != Direction.Axis.Y && state.getValue(HorizontalDirectionalBlock.FACING) != side;
        }
    };

    public static final ICustomRotationBehavior BLOCK_DIRECTIONAL_BEHAVIOR = new ICustomRotationBehavior() {

        @Override
        public boolean customRotate(BlockState state, Level world, BlockPos pos, BlockHitResult hitResult) {
            Direction gridSide = ICoverable.determineGridSideHit(hitResult);
            if (gridSide == null) return false;

            if (gridSide != state.getValue(DirectionalBlock.FACING)) {
                state = state.setValue(DirectionalBlock.FACING, gridSide);
                world.setBlockAndUpdate(pos, state);
                return true;
            }
            return false;
        }

        @Override
        public boolean showSideTip(BlockState state, Direction side) {
            return state.getValue(DirectionalBlock.FACING) != side;
        }
    };

    private enum CustomRotations {

        // DirectionalBlock
        PISTON(Blocks.PISTON, BLOCK_DIRECTIONAL_BEHAVIOR),
        STICKY_PISTON(Blocks.STICKY_PISTON, BLOCK_DIRECTIONAL_BEHAVIOR),
        DROPPER(Blocks.DROPPER, BLOCK_DIRECTIONAL_BEHAVIOR),
        DISPENSER(Blocks.DISPENSER, BLOCK_DIRECTIONAL_BEHAVIOR),
        OBSERVER(Blocks.OBSERVER, BLOCK_DIRECTIONAL_BEHAVIOR),

        // HorizontalDirectionalBlock
        FURNACE(Blocks.FURNACE, BLOCK_HORIZONTAL_BEHAVIOR),
        PUMPKIN(Blocks.CARVED_PUMPKIN, BLOCK_HORIZONTAL_BEHAVIOR),
        LIT_PUMPKIN(Blocks.JACK_O_LANTERN, BLOCK_HORIZONTAL_BEHAVIOR),
        CHEST(Blocks.CHEST, BLOCK_HORIZONTAL_BEHAVIOR),
        TRAPPED_CHEST(Blocks.TRAPPED_CHEST, BLOCK_HORIZONTAL_BEHAVIOR),
        ENDER_CHEST(Blocks.ENDER_CHEST, BLOCK_HORIZONTAL_BEHAVIOR),

        // Custom facings

        // Cannot face up, and uses a custom BlockState property key
        HOPPER(Blocks.HOPPER, new ICustomRotationBehavior() {

            @Override
            public boolean customRotate(BlockState state, Level world, BlockPos pos, BlockHitResult hitResult) {
                Direction gridSide = ICoverable.determineGridSideHit(hitResult);
                if (gridSide == null || gridSide == Direction.UP) return false;

                if (gridSide != state.getValue(HopperBlock.FACING)) {
                    state = state.setValue(HopperBlock.FACING, gridSide);
                    world.setBlockAndUpdate(pos, state);
                    return true;
                }
                return false;
            }

            @Override
            public boolean showSideTip(BlockState state, Direction side) {
                return side != Direction.UP && state.getValue(HopperBlock.FACING) != side;
            }
        }),

        ;

        CustomRotations(Block block, ICustomRotationBehavior behavior) {
            registerCustomRotation(block, behavior);
        }

        private static void init() {}
    }
}
