package com.gregtechceu.gtceu.api.pattern;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;

import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.Builder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class MultiblockShapeInfo {

    private final BlockInfo[][][] blocks; // [z][y][x]

    public MultiblockShapeInfo(BlockInfo[][][] blocks) {
        this.blocks = blocks;
    }

    public BlockInfo[][][] getBlocks() {
        return blocks;
    }

    public static ShapeInfoBuilder builder() {
        return new ShapeInfoBuilder();
    }

    public static class ShapeInfoBuilder extends Builder<BlockInfo, ShapeInfoBuilder> {

        public ShapeInfoBuilder where(char symbol, BlockState blockState) {
            return where(symbol, BlockInfo.fromBlockState(blockState));
        }

        public ShapeInfoBuilder where(char symbol, Supplier<? extends Block> block) {
            return where(symbol, block.get());
        }

        public ShapeInfoBuilder where(char symbol, Block block) {
            return where(symbol, block.defaultBlockState());
        }

        public ShapeInfoBuilder where(char symbol, Supplier<? extends IMachineBlock> machine, Direction facing) {
            return where(symbol, machine.get(), facing);
        }

        public ShapeInfoBuilder where(char symbol, IMachineBlock machine, Direction facing) {
            return where(symbol, machine.getRotationState() == RotationState.NONE ?
                    machine.self().defaultBlockState() :
                    machine.self().defaultBlockState().setValue(machine.getRotationState().property, facing));
        }

        private BlockInfo[][][] bake() {
            return this.bakeArray(BlockInfo.class, BlockInfo.EMPTY);
        }

        public MultiblockShapeInfo build() {
            return new MultiblockShapeInfo(bake());
        }
    }
}
