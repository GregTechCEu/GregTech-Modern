package com.gregtechceu.gtceu.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

public class LegacyGameTestHelper extends GameTestHelper {

    private static final int LEGACY_Y_OFFSET = 1;

    public LegacyGameTestHelper(GameTestInfo testInfo) {
        super(testInfo);
    }

    @Override
    public BlockPos absolutePos(BlockPos relativePos) {
        return super.absolutePos(relativePos.offset(0, -LEGACY_Y_OFFSET, 0));
    }

    @Override
    public BlockPos relativePos(BlockPos absolutePos) {
        return super.relativePos(absolutePos).offset(0, LEGACY_Y_OFFSET, 0);
    }

    @Override
    public Vec3 absoluteVec(Vec3 relativeVec) {
        return super.absoluteVec(relativeVec.subtract(0.0, LEGACY_Y_OFFSET, 0.0));
    }

    @Override
    public Vec3 relativeVec(Vec3 absoluteVec) {
        return super.relativeVec(absoluteVec).add(0.0, LEGACY_Y_OFFSET, 0.0);
    }

    @Override
    public AABB getRelativeBounds() {
        return super.getRelativeBounds().move(0.0, LEGACY_Y_OFFSET, 0.0);
    }
}

final class LegacyExtendedGameTestHelper extends ExtendedGameTestHelper {

    private static final int LEGACY_Y_OFFSET = 1;

    LegacyExtendedGameTestHelper(GameTestInfo testInfo) {
        super(testInfo);
    }

    @Override
    public BlockPos absolutePos(BlockPos relativePos) {
        return super.absolutePos(relativePos.offset(0, -LEGACY_Y_OFFSET, 0));
    }

    @Override
    public BlockPos relativePos(BlockPos absolutePos) {
        return super.relativePos(absolutePos).offset(0, LEGACY_Y_OFFSET, 0);
    }

    @Override
    public Vec3 absoluteVec(Vec3 relativeVec) {
        return super.absoluteVec(relativeVec.subtract(0.0, LEGACY_Y_OFFSET, 0.0));
    }

    @Override
    public Vec3 relativeVec(Vec3 absoluteVec) {
        return super.relativeVec(absoluteVec).add(0.0, LEGACY_Y_OFFSET, 0.0);
    }

    @Override
    public AABB getRelativeBounds() {
        return super.getRelativeBounds().move(0.0, LEGACY_Y_OFFSET, 0.0);
    }
}
