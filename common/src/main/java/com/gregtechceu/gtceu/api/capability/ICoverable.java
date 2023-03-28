package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.api.block.IAppearance;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.utils.RayTraceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public interface ICoverable extends ITickSubscription, IAppearance {

    Level getLevel();

    BlockPos getPos();

    long getOffsetTimer();

    void markDirty();

    boolean isInValid();

    void notifyBlockUpdate();

    void scheduleRenderUpdate();

    boolean placeCoverOnSide(Direction side, ItemStack itemStack, CoverDefinition coverDefinition, ServerPlayer player);

    boolean removeCover(Direction side);

    boolean canPlaceCoverOnSide(CoverDefinition definition, Direction side);

    CoverBehavior getCoverAtSide(Direction side);

    double getCoverPlateThickness();

    int getPaintingColorForRendering();
    Direction getFrontFacing();

    boolean shouldRenderBackSide();

    default List<CoverBehavior> getCovers() {
        return Arrays.stream(Direction.values()).map(this::getCoverAtSide).filter(Objects::nonNull).collect(Collectors.toList());
    }

    default void onLoad() {
        for (CoverBehavior cover : getCovers()) {
            cover.onLoad();
        }
    }

    default void onUnload() {
        for (CoverBehavior cover : getCovers()) {
            cover.onUnload();
        }
    }

    default void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving){
        for (CoverBehavior cover : getCovers()) {
            cover.onNeighborChanged(block, fromPos, isMoving);
        }
    }

    default boolean hasAnyCover() {
        for(Direction facing : Direction.values())
            if(getCoverAtSide(facing) != null)
                return true;
        return false;
    }

    default boolean hasCover(Direction facing) {
        return getCoverAtSide(facing) != null;
    }

    default boolean isRemote() {
        return getLevel() == null ? LDLib.isRemote() : getLevel().isClientSide;
    }

    default void addCoverCollisionBoundingBox(List<? super VoxelShape> collisionList) {
        double plateThickness = getCoverPlateThickness();
        if (plateThickness > 0.0) {
            for (Direction side : Direction.values()) {
                if (getCoverAtSide(side) != null) {
                    var coverBox = getCoverPlateBox(side, plateThickness);
                    collisionList.add(coverBox);
                }
            }
        }
    }

    static boolean doesCoverCollide(Direction side, List<VoxelShape> collisionBox, double plateThickness) {
        if (side == null) {
            return false;
        }
        
        if (plateThickness > 0.0) {
            var coverPlateBox = getCoverPlateBox(side, plateThickness);
            var aabbs = coverPlateBox.toAabbs();
            for (AABB aabb : aabbs) {
                if (Shapes.collide(side.getAxis(), aabb, collisionBox, plateThickness) < plateThickness) {
                    return true;
                }

            }
        }
        return false;
    }

    @Nullable
    static Direction rayTraceCoverableSide(ICoverable coverable, Player player) {
        var rayTrace = RayTraceHelper.rayTraceRange(coverable.getLevel(), player, 4);
        if (rayTrace.getType() == HitResult.Type.MISS) {
            return null;
        }
        return traceCoverSide(rayTrace);
    }


    class PrimaryBoxData {
        public final boolean usePlacementGrid;

        public PrimaryBoxData(boolean usePlacementGrid) {
            this.usePlacementGrid = usePlacementGrid;
        }
    }

    @Nullable
    static Direction traceCoverSide(BlockHitResult result) {
//        if (result instanceof CuboidRayTraceResult) {
//            CuboidRayTraceResult rayTraceResult = (CuboidRayTraceResult) result;
//            if (rayTraceResult.cuboid6.data == null) {
//                return determineGridSideHit(result);
//            } else if (rayTraceResult.cuboid6.data instanceof CoverSideData) {
//                return ((CoverSideData) rayTraceResult.cuboid6.data).side;
//            } else if (rayTraceResult.cuboid6.data instanceof BlockPipe.PipeConnectionData) {
//                return ((PipeConnectionData) rayTraceResult.cuboid6.data).side;
//            } else if (rayTraceResult.cuboid6.data instanceof PrimaryBoxData) {
//                PrimaryBoxData primaryBoxData = (PrimaryBoxData) rayTraceResult.cuboid6.data;
//                return primaryBoxData.usePlacementGrid ? determineGridSideHit(result) : result.sideHit;
//            } //unknown hit type, fall through
//        }
        //normal collision ray trace, return side hit
        return determineGridSideHit(result);
    }

    @Nullable
    static Direction determineGridSideHit(BlockHitResult result) {
        return GTUtil.determineWrenchingSide(result.getDirection(),
                (float) (result.getLocation().x - result.getBlockPos().getX()),
                (float) (result.getLocation().y - result.getBlockPos().getY()),
                (float) (result.getLocation().z - result.getBlockPos().getZ()));
    }

    static VoxelShape getCoverPlateBox(Direction side, double plateThickness) {
        return switch (side) {
            case UP -> Shapes.box(0.0, 1.0 - plateThickness, 0.0, 1.0, 1.0, 1.0);
            case DOWN -> Shapes.box(0.0, 0.0, 0.0, 1.0, plateThickness, 1.0);
            case NORTH -> Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, plateThickness);
            case SOUTH -> Shapes.box(0.0, 0.0, 1.0 - plateThickness, 1.0, 1.0, 1.0);
            case WEST -> Shapes.box(0.0, 0.0, 0.0, plateThickness, 1.0, 1.0);
            case EAST -> Shapes.box(1.0 - plateThickness, 0.0, 0.0, 1.0, 1.0, 1.0);
        };
    }

    static boolean canPlaceCover(CoverDefinition coverDef, ICoverable coverable) {
        for (Direction facing : Direction.values()) {
            if (coverable.canPlaceCoverOnSide(coverDef, facing)) {
                var cover = coverDef.createCoverBehavior(coverable, facing);
                if (cover.canAttach()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    default BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side, BlockState sourceState, BlockPos sourcePos) {
        if (hasCover(side)) {
            return getCoverAtSide(side).getAppearance(sourceState, sourcePos);
        }
        return null;
    }
}
