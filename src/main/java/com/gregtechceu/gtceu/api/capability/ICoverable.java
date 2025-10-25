package com.gregtechceu.gtceu.api.capability;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.blockentity.ITickSubscription;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.utils.GTUtil;

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
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface ICoverable extends ITickSubscription {

    Level getLevel();

    BlockPos getPos();

    long getOffsetTimer();

    void markDirty();

    boolean isInValid();

    void notifyBlockUpdate();

    void scheduleRenderUpdate();

    void scheduleNeighborShapeUpdate();

    boolean canPlaceCoverOnSide(CoverDefinition definition, Direction side);

    double getCoverPlateThickness();

    Direction getFrontFacing();

    boolean shouldRenderBackSide();

    IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability);

    IFluidHandlerModifiable getFluidHandlerCap(@Nullable Direction side, boolean useCoverCapability);

    /**
     * Its an internal method, you should never call it yourself.
     * <br>
     * Use {@link ICoverable#removeCover(boolean, Direction, Player)} and
     * {@link ICoverable#placeCoverOnSide(Direction, ItemStack, CoverDefinition, ServerPlayer)} instead
     * 
     * @param coverBehavior
     * @param side
     */
    void setCoverAtSide(@Nullable CoverBehavior coverBehavior, Direction side);

    @Nullable
    CoverBehavior getCoverAtSide(Direction side);

    default boolean placeCoverOnSide(Direction side, ItemStack itemStack, CoverDefinition coverDefinition,
                                     @Nullable ServerPlayer player) {
        CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, side);
        if (!canPlaceCoverOnSide(coverDefinition, side) || !coverBehavior.canAttach()) {
            return false;
        }
        if (getCoverAtSide(side) != null) {
            removeCover(side, player);
        }
        coverBehavior.onAttached(itemStack, player);
        coverBehavior.onLoad();
        setCoverAtSide(coverBehavior, side);
        notifyBlockUpdate();
        markDirty();
        scheduleNeighborShapeUpdate();
        // TODO achievement
        // AdvancementTriggers.FIRST_COVER_PLACE.trigger((PlayerMP) player);
        return true;
    }

    default boolean removeCover(boolean dropItself, Direction side, @Nullable Player player) {
        CoverBehavior coverBehavior = getCoverAtSide(side);
        if (coverBehavior == null) {
            return false;
        }
        List<ItemStack> drops = coverBehavior.getAdditionalDrops();
        if (dropItself) {
            drops.add(coverBehavior.getPickItem());
        }
        coverBehavior.onRemoved();
        setCoverAtSide(null, side);
        for (ItemStack dropStack : drops) {
            if (player != null && player.getInventory().add(dropStack))
                continue;

            Block.popResource(getLevel(), getPos(), dropStack);

        }
        notifyBlockUpdate();
        markDirty();
        scheduleNeighborShapeUpdate();
        return true;
    }

    /**
     * Drop all attached covers on the ground
     */
    default void dropAllCovers() {
        for (Direction side : GTUtil.DIRECTIONS) {
            removeCover(side, null);
        }
    }

    default boolean removeCover(Direction side, @Nullable Player player) {
        return removeCover(true, side, player);
    }

    default List<CoverBehavior> getCovers() {
        return Arrays.stream(GTUtil.DIRECTIONS).map(this::getCoverAtSide).filter(Objects::nonNull)
                .collect(Collectors.toList());
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

    default void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        for (CoverBehavior cover : getCovers()) {
            cover.onNeighborChanged(block, fromPos, isMoving);
        }
    }

    default boolean hasAnyCover() {
        for (Direction facing : GTUtil.DIRECTIONS)
            if (getCoverAtSide(facing) != null)
                return true;
        return false;
    }

    default boolean hasCover(Direction facing) {
        return getCoverAtSide(facing) != null;
    }

    default boolean isRemote() {
        return getLevel() == null ? GTCEu.isClientThread() : getLevel().isClientSide;
    }

    default VoxelShape[] addCoverCollisionBoundingBox() {
        double plateThickness = getCoverPlateThickness();
        List<VoxelShape> shapes = new ArrayList<>();
        if (plateThickness > 0.0) {
            for (Direction side : GTUtil.DIRECTIONS) {
                if (getCoverAtSide(side) != null) {
                    var coverBox = getCoverPlateBox(side, plateThickness);
                    shapes.add(coverBox);
                }
            }
        }
        return shapes.toArray(VoxelShape[]::new);
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
        BlockHitResult rayTrace = (BlockHitResult) player.pick(player.getBlockReach(), 0, false);
        if (rayTrace.getType() == HitResult.Type.MISS) {
            return null;
        }
        return traceCoverSide(rayTrace);
    }

    default boolean hasDynamicCovers() {
        for (Direction face : GTUtil.DIRECTIONS) {
            CoverBehavior cover = this.getCoverAtSide(face);
            if (cover != null && cover.getDynamicRenderer().get() != null) return true;
        }
        return false;
    }

    class PrimaryBoxData {

        public final boolean usePlacementGrid;

        public PrimaryBoxData(boolean usePlacementGrid) {
            this.usePlacementGrid = usePlacementGrid;
        }
    }

    @Nullable
    static Direction traceCoverSide(@Nullable BlockHitResult result) {
        return determineGridSideHit(result);
    }

    @Nullable
    static Direction determineGridSideHit(@Nullable BlockHitResult result) {
        if (result == null) return null;
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
        for (Direction facing : GTUtil.DIRECTIONS) {
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
    default BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                          BlockState sourceState, BlockPos sourcePos) {
        if (hasCover(side)) {
            return getCoverAtSide(side).getAppearance(sourceState, sourcePos);
        }
        return null;
    }
}
