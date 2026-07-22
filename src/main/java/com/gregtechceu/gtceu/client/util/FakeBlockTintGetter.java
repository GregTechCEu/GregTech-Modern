package com.gregtechceu.gtceu.client.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class FakeBlockTintGetter implements BlockAndTintGetter {

    @Setter
    public BlockAndTintGetter parent;
    @Setter
    public BlockPos pos;
    @Setter
    public BlockState state;
    @Setter
    public BlockEntity blockEntity;

    @Override
    public float getShade(Direction direction, boolean b) {
        return parent.getShade(direction, b);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return parent.getLightEngine();
    }

    @Override
    public int getBlockTint(BlockPos blockPos, ColorResolver colorResolver) {
        return parent.getBlockTint(blockPos, colorResolver);
    }

    @Override
    public int getBrightness(LightLayer lightType, BlockPos blockPos) {
        return parent.getBrightness(lightType, blockPos);
    }

    @Override
    public int getRawBrightness(BlockPos blockPos, int amount) {
        return parent.getRawBrightness(blockPos, amount);
    }

    @Override
    public boolean canSeeSky(BlockPos blockPos) {
        return parent.canSeeSky(blockPos);
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos blockPos) {
        return blockPos.equals(pos) ? blockEntity : parent.getBlockEntity(blockPos);
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return blockPos.equals(pos) ? state : parent.getBlockState(blockPos);
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return parent.getFluidState(blockPos);
    }

    @Override
    public int getLightEmission(BlockPos pos) {
        return parent.getLightEmission(pos);
    }

    @Override
    public int getMaxLightLevel() {
        return parent.getMaxLightLevel();
    }

    @Override
    public int getHeight() {
        return parent.getHeight();
    }

    @Override
    public int getMinBuildHeight() {
        return parent.getMinBuildHeight();
    }

    @Override
    public int getMaxBuildHeight() {
        return parent.getMaxBuildHeight();
    }

    @Override
    public int getSectionsCount() {
        return parent.getSectionsCount();
    }

    @Override
    public int getMinSection() {
        return parent.getMinSection();
    }

    @Override
    public int getMaxSection() {
        return parent.getMaxSection();
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos pos) {
        return parent.isOutsideBuildHeight(pos);
    }

    @Override
    public boolean isOutsideBuildHeight(int y) {
        return parent.isOutsideBuildHeight(y);
    }

    @Override
    public int getSectionIndex(int y) {
        return parent.getSectionIndex(y);
    }

    @Override
    public int getSectionIndexFromSectionY(int sectionIndex) {
        return parent.getSectionIndexFromSectionY(sectionIndex);
    }

    @Override
    public int getSectionYFromSectionIndex(int sectionIndex) {
        return parent.getSectionYFromSectionIndex(sectionIndex);
    }

    @Override
    public Stream<BlockState> getBlockStates(AABB area) {
        return parent.getBlockStates(area);
    }

    @Override
    public BlockHitResult isBlockInLine(ClipBlockStateContext context) {
        return parent.isBlockInLine(context);
    }

    @Override
    public BlockHitResult clip(ClipContext context) {
        return parent.clip(context);
    }

    @Override
    public @Nullable BlockHitResult clipWithInteractionOverride(Vec3 startVec, Vec3 endVec, BlockPos pos,
                                                                VoxelShape shape, BlockState state) {
        return parent.clipWithInteractionOverride(startVec, endVec, pos, shape, state);
    }

    @Override
    public double getBlockFloorHeight(VoxelShape shape, Supplier<VoxelShape> belowShapeSupplier) {
        return parent.getBlockFloorHeight(shape, belowShapeSupplier);
    }

    @Override
    public double getBlockFloorHeight(BlockPos pos) {
        return parent.getBlockFloorHeight(pos);
    }
}
